package com.rose.ggpo;

public class InputQueue {
    private static final int INPUT_QUEUE_LENGTH = 128;
    private int head;
    private int tail;
    private int length;
    private int frame_delay;
    private boolean first_frame;
    private int last_user_added_frame;
    private int first_incorrect_frame;
    private int last_frame_requested;
    private int last_added_frame;
    private GameInput prediction;
    private final GameInput[] inputs;
    private GameInput currentInput;

    public InputQueue() {
        head = 0;
        tail = 0;
        length = 0;
        frame_delay = 2;
        first_frame = true;
        last_user_added_frame   = GameInput.NULL_FRAME;
        first_incorrect_frame   = GameInput.NULL_FRAME;
        last_frame_requested    = GameInput.NULL_FRAME;
        last_added_frame        = GameInput.NULL_FRAME;

        prediction = new GameInput(GameInput.NULL_FRAME, 0);
        inputs = new GameInput[INPUT_QUEUE_LENGTH];
        for(int i = 0; i < inputs.length; i++) {
            inputs[i] = new GameInput(0,0);
        }
        currentInput = null;
    }

    public GameInput[] getInputList() {
        return inputs;
    }

    public GameInput getCurrentInput() { return currentInput; }

    public void addInput(GameInput gameInput) {
        System.out.println("adding input frame number: " + gameInput.getFrame() + " to queue");
        System.out.println("last user added frame " + last_user_added_frame + " game input frame: " + gameInput.getFrame());
//        assert(last_user_added_frame == GameInput.NULL_FRAME || gameInput.getFrame() == last_user_added_frame + 1);
        last_user_added_frame = gameInput.getFrame();

        int new_frame = advanceQueueHead(gameInput.getFrame());
        if(new_frame != GameInput.NULL_FRAME) {
            addDelayedInputToQueue(gameInput, new_frame);
        }

        gameInput.setFrame(new_frame);
        currentInput = gameInput;
    }

    private void addDelayedInputToQueue(GameInput gameInput, int frame_number) {
        System.out.println("Adding delayed input frame " + frame_number + " to queue");

        assert(last_added_frame == GameInput.NULL_FRAME || frame_number == (last_added_frame + 1));

        assert(frame_number == 0 || inputs[getPreviousFrame(head)].getFrame() == frame_number - 1);

        inputs[head] = new GameInput(frame_number, gameInput.getInput());
        head = (head + 1) % INPUT_QUEUE_LENGTH;
        length++;
        first_frame = false;

        last_added_frame = frame_number;

        if(prediction.getFrame() != GameInput.NULL_FRAME) {
            if(frame_number != prediction.getFrame()) {
                System.out.println("WARNING: frame " + frame_number +
                    " does not match prediction frame " + prediction.getFrame());
            }

            if( first_incorrect_frame == GameInput.NULL_FRAME &&
                !prediction.equals(gameInput)) {
                System.out.println("frame " + frame_number +
                    " does not match. Marking error.");
                first_incorrect_frame = frame_number;
            }

            if( prediction.getFrame() == last_frame_requested &&
                first_incorrect_frame == GameInput.NULL_FRAME) {
                System.out.println("Prediction is correct. Dropping out of prediction mode");
                prediction.setFrame(GameInput.NULL_FRAME);
            } else {
                prediction.setFrame(prediction.getFrame() + 1);
            }
        } else {
            System.out.println("Prediction frame: " + prediction.getFrame());
        }

        if(length > INPUT_QUEUE_LENGTH) {
            System.out.println("WARNING: length of queue exceeded!");
        }
    }

    private int advanceQueueHead(int frame) {
        int expected_frame = first_frame ? 0 : inputs[getPreviousFrame(head)].getFrame() + 1;
        frame += frame_delay;

        if(expected_frame > frame) {
            System.out.println("Dropping input frame: " + frame + ". Expected next frame: " + expected_frame);
            return GameInput.NULL_FRAME;
        }

        while(expected_frame < frame) {
            System.out.println("Adding padding frame " + expected_frame + " to account or change in frame delay");
            GameInput last_input = inputs[getPreviousFrame(head)];
            addDelayedInputToQueue(last_input, expected_frame);
            expected_frame++;
        }

        assert(frame == 0 || frame == inputs[getPreviousFrame(head)].getFrame() + 1);
        return frame;
    }

    private int getPreviousFrame(int offset) {
        return  offset == 0 ? INPUT_QUEUE_LENGTH - 1 : offset - 1;
    }

    public void discardConfirmedFrames(int frame) {
//        assert (frame >= 0);

        if(last_frame_requested != GameInput.NULL_FRAME) {
            frame = Math.min(frame, last_frame_requested);
        }

        if(frame >= last_added_frame) {
            tail = head;
        } else {
            int offset = frame - inputs[tail].getFrame();

            tail = (tail + offset) % INPUT_QUEUE_LENGTH;
            if(tail < 0) {
                tail = INPUT_QUEUE_LENGTH-1;
            }
            length -= offset;
        }

//        assert(length >= 0);
    }

    public GameInput getInput(int requested_frame) {
        System.out.println("requesting input from frame " + requested_frame);
//        assert (first_incorrect_frame == GameInput.NULL_FRAME);

        last_frame_requested = requested_frame;

        if(requested_frame < inputs[tail].getFrame()) {
            System.out.println("WARNING: requested frame: " +requested_frame +
                " less than input at tail: " + inputs[tail].getFrame());
        }

        if(prediction.getFrame() == GameInput.NULL_FRAME) {
            int offset = requested_frame - inputs[tail].getFrame();
            if(offset < length) {
                offset = (offset + tail) % INPUT_QUEUE_LENGTH;
                assert(inputs[offset].getFrame() == requested_frame);
                System.out.println("returning confirmed frame: " + inputs[offset].getFrame());
                return inputs[offset];
            }

            if(requested_frame == 0) {
                System.out.println("basing prediction from nothing. client wants frame 0");
                prediction.setInput(0);
            } else if(last_added_frame == GameInput.NULL_FRAME) {
                System.out.println("basing prediction from nothing, since we have no frames");
                prediction.setInput(0);
            } else {
                System.out.println("basing prediction from previously added frame");
                System.out.println("(queue: " + inputs[getPreviousFrame(head)] +
                    ", frame: " + inputs[getPreviousFrame(head)].getFrame());
                prediction = inputs[getPreviousFrame(head)];
            }
            prediction.setFrame(prediction.getFrame() + 1);
        }

        if(prediction.getFrame() < 0) {
            System.out.println("WARNING: prediction frame is " + prediction.getFrame());
        }
        GameInput input = new GameInput(prediction.getFrame(), prediction.getInput());
        input.setFrame(requested_frame);
        return input;
    }

    public int getFirstIncorrectFrame() {
        return first_incorrect_frame;
    }

    public void resetPrediction(int frame) {
//        assert( first_incorrect_frame == GameInput.NULL_FRAME ||
//                frame <= first_incorrect_frame);
        prediction.setFrame(GameInput.NULL_FRAME);
        first_incorrect_frame = GameInput.NULL_FRAME;
        last_frame_requested = GameInput.NULL_FRAME;
    }

    public void setFrameDelay(int frameDelay) {
        this.frame_delay = frameDelay;
    }
}
