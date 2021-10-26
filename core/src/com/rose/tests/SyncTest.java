package com.rose.tests;

import static com.rose.ggpo.GGPOEventCode.GGPO_EVENTCODE_RUNNING;

import com.badlogic.gdx.Gdx;
import com.rose.ggpo.GGPOErrorCode;
import com.rose.ggpo.GameInput;
import com.rose.ggpo.GgpoCallbacks;
import com.rose.ggpo.GgpoEvent;
import com.rose.ggpo.RingBuffer;
import com.rose.ggpo.SavedState;
import com.rose.ggpo.Sync;
import com.rose.network.SavedInfo;

import java.util.Arrays;

/*
    This test simulates a single player triggering a rollback every check_distance number of frames.
    The test passes if every frame matches the expected frame, and if the checksum for the given
    frames game state matches the one that is expected.
    The test will fail if a single frame/checksum check fails.
    The test can run for any number of frames, determined by maxTestFrames.
 */
public class SyncTest {
    private static final int check_distance = 1;
    private final Sync sync;
    private int last_verified;
    private boolean rolling_back;
    private boolean running;

    private GameInput current_input;
    private GameInput last_input;
    private final RingBuffer<SavedInfo> savedFrames;
    private final TestGame callbacks;

    public SyncTest(TestGame callbacks) {
        this.callbacks = callbacks;
        last_verified = 0;
        rolling_back = false;
        running = false;
        current_input = new GameInput(-1,-1);
        last_input = new GameInput(-1, -1);
        String gameName = "Rose";
        savedFrames = new RingBuffer<>(32);
        sync = new Sync();
        sync.setCallbacks(callbacks);

        callbacks.beginGame(gameName);
    }

    public void runSyncTest(int maxTestFrames) {
        long now, next;
        int currentFrame = 0;
        next = System.nanoTime();

        System.out.println("Begin sync test now...");
        while(currentFrame <= maxTestFrames) {
            now = System.nanoTime();
            doPoll();
            if(now >= next) {
                callbacks.runSyncFrame();
                next = now + (1000000000L / 60);
                currentFrame++;
            }
        }
    }

    public void doPoll() {
        if(!running) {
            GgpoEvent event = new GgpoEvent(GGPO_EVENTCODE_RUNNING);
            callbacks.onEvent(event);
            running = true;
            System.out.println("The system is now running");
        }
    }

    public GGPOErrorCode addLocalInput(int input) {
        if(!running) {
            return GGPOErrorCode.GGPO_ERRORCODE_NOT_SYNCHRONIZED;
        }

        current_input.setInput(input);

        return GGPOErrorCode.GGPO_OK;
    }

    public int[] syncInput() {
        System.out.println(
            "SyncLog:\n" +
            "frame: " + sync.frame_count + " play type: " +
            (rolling_back ? "replay" : "original")
        );
        if(rolling_back) {
            last_input = savedFrames.front().input;
        } else {
            if(sync.frame_count == 0) {
                sync.saveCurrentFrame();
            }
            last_input = new GameInput(current_input.getFrame(), current_input.getInput());
        }

        return new int[]{last_input.getInput()};
    }

    public void incrementFrame() {
//        sync.incrementFrame();
        current_input = new GameInput(-1, -1);
        System.out.printf("End of frame(%d)\n", sync.frame_count);

        if(rolling_back) {
            return;
        }

        int frame = sync.frame_count;
        SavedInfo info = new SavedInfo();
        info.frame = frame;
        info.input = last_input;
        info.cbuf = sync.getLastSavedFrame().cbuf;
        info.buf = new byte[info.cbuf];
        System.arraycopy(sync.getLastSavedFrame().buf, 0,
            info.buf, 0, info.cbuf);
        info.checksum = sync.getLastSavedFrame().checkSum;
        savedFrames.push(info);

        if(frame - last_verified == check_distance) {
            sync.loadFrame(last_verified);
            rolling_back = true;
            while(!savedFrames.empty()) {
                callbacks.advanceFrame(0);
                info = savedFrames.front();

                if(info.frame != sync.frame_count) {
                    System.out.printf("Frame number %d does not match saved frame number %d\n", info.frame, sync.frame_count);
                    SavedState.SavedFrame[] frames = sync.getAllSavedFrames();
                    System.out.println("printing sync saved checksums");
                    for(int i = 0; i < frames.length; i++) {
                        System.out.println("index: " + i + " frame: " + frames[i].frame + " checksum: " + frames[i].checkSum);
                    }
                    System.out.println("printing local saved checksums");
                    for(int i = 0; i < savedFrames.size(); i++) {
                        SavedInfo local_frame = savedFrames.item(i);
                        System.out.println("index: " + i + " frame: " + local_frame.frame + " checksum: " + local_frame.checksum);
                    }
                    Gdx.app.exit();
                } else {
                    System.out.print("Frame numbers match\n");
                }

                String checksum = sync.getLastSavedFrame().checkSum;
                if(info.checksum == checksum) {
                    logSaveStates(info);
                    System.out.printf("Checksum for frame %d does not match saved (%s != %s)\n", frame, checksum, info.checksum);
                    SavedState.SavedFrame[] frames = sync.getAllSavedFrames();
                    System.out.println("printing sync saved checksums");
                    for(int i = 0; i < frames.length; i++) {
                        System.out.println("index: " + i + " frame: " + frames[i].frame + " checksum: " + frames[i].checkSum);
                    }
                    System.out.println("printing local saved checksums");
                    for(int i = 0; i < savedFrames.size(); i++) {
                        SavedInfo local_frame = savedFrames.item(i);
                        System.out.println("index: " + i + " frame: " + local_frame.frame + " checksum: " + local_frame.checksum);
                    }
                    Gdx.app.exit();
                } else {
                    System.out.printf("Checksum %s for frame %d matches.\n", checksum, info.frame);
                }
                savedFrames.pop();
            }

            last_verified = frame;
            rolling_back = false;
        }

    }

    private void logSaveStates(SavedInfo info) {
        System.out.println("Game state buf: " + Arrays.toString(info.buf));
        // Use the callbacks function
    }
}

