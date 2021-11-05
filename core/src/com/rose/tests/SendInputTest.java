package com.rose.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.rose.actors.Fighter;
import com.rose.actors.Ken;
import com.rose.actors.Ryu;
import com.rose.ggpo.GGPOErrorCode;
import com.rose.ggpo.GGPOEventCode;
import com.rose.ggpo.GgpoCallbacks;
import com.rose.ggpo.GgpoEvent;
import com.rose.management.GameState;
import com.rose.management.NonGameState;
import com.rose.management.SaveGameState;
import com.rose.management.Utilities;
import com.rose.network.ConnectState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SendInputTest implements GgpoCallbacks {
    private int current_frame;
    private DummyClient dc;
    private int input;
    private int frame_timer;
    private GameState gs;
    private final NonGameState ngs;
    private String checksum;

    public SendInputTest() {
        Fighter[] fighters = new Fighter[2];
        fighters[0] = new Ryu(new Vector2(250 / 2f - 32 / 2f, 20), true);
        fighters[1] = new Ken(new Vector2(500 / 2f - 32 / 2f, 20), false);
        gs = new GameState(fighters, 1, true);
        ngs = new NonGameState();

        input = getRandomInput();
        System.out.println("Begin network test now...");
        try {
            dc = new DummyClient(this);
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
            GGPOErrorCode result = dc.addLocalInput(current_frame, input);
            if(GGPOErrorCode.GGPOSucceeded(result)) {
                int inputs[] = dc.syncInput();
                advanceFrame(Gdx.graphics.getDeltaTime(), inputs);
            }

            current_frame++;
            frame_timer++;
        }

    }

    private void advanceFrame(float deltaTime, int[] inputs) {
        gs.update(deltaTime, inputs);
        ngs.now.frameNumber = gs.getFrameNumber();
        ngs.now.checksum = this.checksum;
        if((gs.getFrameNumber() % 90) == 0) {
            ngs.periodic = ngs.now;
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
        byte[] data = gs.saveGameState();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            this.checksum = Utilities.bytesToHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new SaveGameState(data, checksum);
    }

    @Override
    public boolean loadFrame(byte[] buffer, int length) {
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        try {
            ObjectInputStream is = new ObjectInputStream(in);
            gs = (GameState)is.readObject();
            gs.loadGameState();
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;    }

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
        int[] inputs = dc.syncInput();
        advanceFrame(Gdx.graphics.getDeltaTime(), inputs);
        return true;
    }

    @Override
    public boolean onEvent(GgpoEvent event) {
        if (event.getCode() == GGPOEventCode.GGPO_EVENTCODE_CONNECTED_TO_SERVER) {
            ngs.setConnectState(event.connected.playerHandle, NonGameState.PlayerConnectState.SYNCHRONIZING);
        }

        if(event.getCode() == GGPOEventCode.GGPO_EVENTCODE_TIMESYNC) {
            try {
                System.out.println("try to sleep for: " +
                        (1000000000L * event.timeSync.frames_ahead / 60000000) + " ms");
                Thread.sleep((1000000000L * event.timeSync.frames_ahead / 60000000L));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
