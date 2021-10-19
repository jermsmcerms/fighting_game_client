package com.rose.ggpo;

public class InputQueue {
    private static final int INPUT_QUEUE_LENGTH = 128;
    private static final int DEFAULT_INPUT_SIZE = 4;
    private int id;
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
    private GameInput[] inputs;
    private GameInput currentInput;

    public InputQueue(int id) {
        this.id = id;
        head = 0;
        tail = 0;
        length = 0;
        frame_delay = 2;
        first_frame = true;
        last_user_added_frame   = GameInput.NULL_FRAME;
        first_incorrect_frame   = GameInput.NULL_FRAME;
        last_frame_requested    = GameInput.NULL_FRAME;
        last_added_frame        = GameInput.NULL_FRAME;

        prediction = new GameInput(GameInput.NULL_FRAME, -1);
        inputs = new GameInput[INPUT_QUEUE_LENGTH];
        for(int i = 0; i < inputs.length; i++) {
            inputs[i] = new GameInput(-1, -1);
        }
        currentInput = null;
    }

    public GameInput getCurrentInput() { return currentInput; }

    public void addInput(GameInput gameInput) {
//        assert( last_user_added_frame == GameInput.NULL_FRAME ||
//                gameInput.getFrame() == last_user_added_frame + 1);
        last_user_added_frame = gameInput.getFrame();

        int new_frame = advanceQueueHead(gameInput.getFrame());
        if(new_frame != GameInput.NULL_FRAME) {
            addDelayedInputToQueue(gameInput, new_frame);
        }

        gameInput.setFrame(new_frame);
        currentInput = gameInput;
    }

    private void addDelayedInputToQueue(GameInput gameInput, int frame_number) {
//        assert( last_added_frame == GameInput.NULL_FRAME ||
//                frame_number == last_added_frame + 1);
//        assert(frame_number == 0 || inputs[getPreviousFrame(head)].getFrame() == frame_number -1);

        inputs[head] = new GameInput(frame_number, gameInput.getInput());
        head = (head + 1) % INPUT_QUEUE_LENGTH;
        length++;
        first_frame = false;

        last_added_frame = frame_number;

        if(prediction.getFrame() != GameInput.NULL_FRAME) {
//            assert(frame_number == prediction.getFrame());

            if( first_incorrect_frame == GameInput.NULL_FRAME &&
                !prediction.equals(gameInput)) {
                first_incorrect_frame = frame_number;
            }

            if( prediction.getFrame() == last_frame_requested &&
                first_incorrect_frame == GameInput.NULL_FRAME) {
                prediction.setFrame(GameInput.NULL_FRAME);
            } else {
                prediction.setFrame(prediction.getFrame() + 1);
            }
        }

//        assert(length <= INPUT_QUEUE_LENGTH);
    }

    private int advanceQueueHead(int frame) {
        int expected_frame = first_frame ? 0 : inputs[getPreviousFrame(head)].getFrame() + 1;
        frame += frame_delay;

        if(expected_frame > frame) {
            return GameInput.NULL_FRAME;
        }

        while(expected_frame < frame) {
            GameInput last_input = inputs[getPreviousFrame(head)];
            addDelayedInputToQueue(last_input, expected_frame);
            expected_frame++;
        }

//        assert( frame == 0 ||
//                frame == inputs[getPreviousFrame(head)].getFrame() + 1);
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
//            if(offset < 0) {
//                offset = INPUT_QUEUE_LENGTH;
//            }

            tail = (tail + offset) % INPUT_QUEUE_LENGTH;
            if(tail < 0) {
                tail = INPUT_QUEUE_LENGTH-1;
            }
            length -= offset;
        }

//        assert(length >= 0);
    }


    int queue_count;
    public GameInput getInput(int requested_frame) {

//        assert(first_incorrect_frame == GameInput.NULL_FRAME);

        last_frame_requested = requested_frame;

//        assert(requested_frame >= inputs[tail].getFrame());

        if(prediction.getFrame() == GameInput.NULL_FRAME) {
            int offset = requested_frame - inputs[tail].getFrame();
            if(offset < length) {
                offset = (offset + tail) % INPUT_QUEUE_LENGTH;
//                assert(inputs[offset].getFrame() == requested_frame);
                return inputs[offset];
            }

            if(requested_frame == 0) {
                prediction.setInput(0);
            } else if(last_added_frame == GameInput.NULL_FRAME) {
                prediction.setInput(0);
            } else {
                prediction = inputs[getPreviousFrame(head)];
            }
            prediction.setFrame(prediction.getFrame() + 1);
        }

//        assert(prediction.getFrame() >= 0);
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
}
