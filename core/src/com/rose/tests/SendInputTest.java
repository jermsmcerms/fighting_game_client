package com.rose.tests;

import com.badlogic.gdx.Gdx;
import com.rose.ggpo.GgpoCallbacks;
import com.rose.ggpo.GgpoEvent;
import com.rose.management.SaveGameState;
import com.rose.network.ConnectState;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class SendInputTest implements GgpoCallbacks {
    private int current_frame;
    private DummyClient dc;
    private int input;
    private int frame_timer;

    public SendInputTest() {
        input = getRandomInput();
        System.out.println("Begin network sync test now...");
        try {
            dc = new DummyClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runTest(int max_frames) {
        long now, next;
        next = System.nanoTime();
        while (current_frame < max_frames) {
            now = System.nanoTime();
            dc.doPoll();
            if (now >= next) {
                runFrame();
                next = now + (1000000000L / 60);
            }
        }
        System.out.println("test completed. Exiting.");
        Gdx.app.exit();
    }

    public void runFrame() {
        if(dc.getCurrentStatus() == ConnectState.Running) {
            if(frame_timer < 5) {
                input = getRandomInput();
                frame_timer = 0;
            }
            dc.addLocalInput(current_frame, input);
//            dc.syncInputs();
            current_frame++;
            frame_timer++;
        }

        dc.incrementFrame();
    }

    private int getRandomInput() {
        int retval = 1;
        int randomInt = ThreadLocalRandom.current().nextInt(-1, 8);
        if(randomInt == -1) {
            return 0;
        }
        return retval << randomInt;
    }

    @Override
    public boolean beginGame(String name) {
        return false;
    }

    @Override
    public SaveGameState saveGameState() {
        return null;
    }

    @Override
    public boolean loadFrame(byte[] buffer, int length) {
        return false;
    }

    @Override
    public boolean logGameState(String filename, String buffer) {
        return false;
    }

    @Override
    public Object freeBuffer(Object buffer) {
        return null;
    }

    @Override
    public boolean advanceFrame(int flags) {
        return false;
    }

    @Override
    public boolean onEvent(GgpoEvent event) {
        return false;
    }
}
