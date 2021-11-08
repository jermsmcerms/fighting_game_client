package com.rose.tests;

import static com.rose.ggpo.GGPOEventCode.GGPO_EVENTCODE_RUNNING;

import com.rose.ggpo.GGPOErrorCode;
import com.rose.ggpo.GameInput;
import com.rose.ggpo.GgpoEvent;
import com.rose.ggpo.InputQueue;
import com.rose.ggpo.Sync;

import java.util.ArrayList;

public class InputQueueTest {
    private static final int[] p1_inputs = {
        1,  1,  3,  2,  4,  2,  1,  4,
        1,  3,  3,  4,  1,  3,  4,  2,
        2,  4,  4,  4,  2,  2,  4,  1,
        2,  3,  2,  2,  4,  1,  2,  1,
        2,  1,  3,  4,  3,  1,  4,  2,
        4,  1,  4,  4,  2,  2,  3,  2,
        3,  2,  2,  4,  4,  1,  4,  3,
        2,  2,  3,  1,  1,  2,  2,  3,
    };

    private static final int[] p1_delayed_inputs = {
        0,  0,  1,  1,  3,  2,  4,  2,
        1,  4,  1,  3,  3,  4,  1,  3,
        4,  2,  2,  4,  4,  4,  2,  2,
        4,  1,  2,  3,  2,  2,  4,  1,
        2,  1,  2,  1,  3,  4,  3,  1,
        4,  2,  4,  1,  4,  4,  2,  2,
        3,  2,  3,  2,  2,  4,  4,  1,
        4,  3,  2,  2,  3,  1,  1,  2
    };

    private static final int[] p2_inputs = {
        1,	4,	2,	4,	1,	4,	2,	1,
        1,	4,	3,	3,	1,	4,	4,	3,
        4,	1,	2,	1,	1,	3,	2,	3,
        4,	3,	1,	4,	2,	2,	2,	1,
        2,	3,	2,	4,	3,	3,	4,	4,
        3,	3,	3,	2,	1,	2,	2,	2,
        3,	3,	2,	2,	1,	2,	1,	1,
        2,	3,	1,	4,	2,	4,	4,	2
    };

    /*
        CAUTION! if this number is adjusted (while still greater than zero),
        you will need to add or remove input padding to both players expected results.
        Example:
            if your inputs look like this:
                P1: 1, 2, 3, 4, 5, 6, 7, 8
            and your frame delay is 3, then your expected output should be:
                P1: 0, 0, 0, 1, 2, 3, 4, 5
        Note:   Player 2's inputs are not delayed as they have already been sent over the network
                after being delayed on their system.
     */
    private static final int DEFAULT_FRAME_DELAY = 3;

    private final Sync sync;
    private int current_frame;
    private boolean running;

    private final TestGame callbacks;

    private final StringBuilder report;
    private int num_p1_success;
    private int num_p1_fail;

    private int num_p2_success;
    private int num_p2_fail;

    public InputQueueTest(TestGame callbacks) {
        this.callbacks = callbacks;
        current_frame = 0;
        running = false;
        report = new StringBuilder();
        sync = new Sync();
        sync.setFrameDelay(0, DEFAULT_FRAME_DELAY);
        sync.setCallbacks(callbacks);

        callbacks.beginGame("input_queue_test");
    }

    public void runTest(int maxTestFrames) {
        long now, next;
        next = System.currentTimeMillis();

        System.out.println("Begin input queue test now...");
        doPoll();
        while(current_frame < maxTestFrames) {
            now = System.currentTimeMillis();
            if(now >= next) {
                runFrame();
                next = now + (1000 / 60);
                current_frame++;
            }
        }
        System.out.println("The test has finished. Printing report.");
        System.out.println(report);
        System.out.println("Player 1 number of success: " + num_p1_success + ". Number of failures: " + num_p1_fail);
        System.out.println("Player 2 number of success: " + num_p2_success + ". Number of failures: " + num_p2_fail);

        ArrayList<InputQueue> inputQueues = sync.getInputQueue();
        if(num_p1_fail > 0) {
            GameInput[] input_array = inputQueues.get(0).getInputList();
            System.out.println("queue " + 0);
            System.out.println("-------------------------");
            for (GameInput gameInput : input_array) {
                System.out.println(
                    " input: " + gameInput.getInput() +
                    " frame: " + gameInput.getFrame());
            }
        } else {
            System.out.println("all of player one's inputs were correct after syncing.");
        }

        if(num_p2_fail > 0) {
            GameInput[] input_array = inputQueues.get(1).getInputList();
            System.out.println("queue " + 1);
            System.out.println("-------------------------");
            for (GameInput gameInput : input_array) {
                System.out.println(
                        " input: " + gameInput.getInput() +
                                " frame: " + gameInput.getFrame());
            }
        } else {
            System.out.println("all of player two's inputs were correct after syncing.");
        }
    }

    public void doPoll() {
        if(!running) {
            GgpoEvent event = new GgpoEvent(GGPO_EVENTCODE_RUNNING);
            callbacks.onEvent(event);
            running = true;
            System.out.println("The system is now running");
        }
        check_simulation();
    }

    private void check_simulation() {
        int first_incorrect = GameInput.NULL_FRAME;
        ArrayList<InputQueue> queues = sync.getInputQueue();
        for(int i = 0; i < queues.size(); i++) {
            int incorrect = queues.get(i).getFirstIncorrectFrame();
            System.out.println("Considering incorrect frame: " + incorrect + " reported by player: " + (i+1));
            if(incorrect != GameInput.NULL_FRAME && (first_incorrect == GameInput.NULL_FRAME) || incorrect < first_incorrect) {
                first_incorrect = incorrect;
            }
        }

        if(first_incorrect == GameInput.NULL_FRAME) {
            System.out.println("prediction ok. proceeding.");
            return;
        }

        System.out.println("firs incorrect: " + first_incorrect);
    }

    private void runFrame() {
        GGPOErrorCode result = addLocalInput(p1_inputs[current_frame]);
        addRemoteInput(p2_inputs[current_frame], current_frame);
        if(GGPOErrorCode.GGPOSucceeded(result)) {
            int[] syncedInputs = sync.syncInputs();
            if(syncedInputs != null) {
                testInputs(syncedInputs);
                incrementFrame();
            }
        }
    }

    private void incrementFrame() {
        doPoll();
//        sync.incrementFrame();
    }

    private GGPOErrorCode addLocalInput(int input) {
        GameInput gameInput;
        if(sync.isInRollback()) {
            return GGPOErrorCode.GGPO_ERRORCODE_IN_ROLLBACK;
        }

        gameInput = new GameInput(-1, input);
//        if(!sync.addLocalInput(0, gameInput)) {
//            return GGPOErrorCode.GGPO_ERRORCODE_PREDICTION_THRESHOLD;
//        }

        return GGPOErrorCode.GGPO_OK;
    }

    private void addRemoteInput(int input, int frame) {
        GameInput remoteInput = new GameInput(frame, input);
        sync.addRemoteInput(1, remoteInput);
    }

    private void testInputs(int[] inputs) {
        if(inputs[0] != p1_delayed_inputs[current_frame]) {
            String errorMsg = "p1 inputs: " + inputs[0] +
                " do not match the expected: " + p1_inputs[current_frame] +
                " for frame: " + current_frame + "\n";
            report.append(errorMsg);
            num_p1_fail++;
        } else {
            num_p1_success++;
        }

        if(inputs[1] != p2_inputs[current_frame]) {
            String errorMsg = "p2 inputs: " + inputs[1] +
                    " do not match the expected: " + p2_inputs[current_frame] +
                    " for frame: " + current_frame + "\n";
            report.append(errorMsg);
            num_p2_fail++;
        } else {
            num_p2_success++;
        }
    }
}
