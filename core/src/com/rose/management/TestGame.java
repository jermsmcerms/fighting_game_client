package com.rose.management;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.rose.actors.Fighter;
import com.rose.actors.Ken;
import com.rose.actors.Ryu;
import com.rose.ggpo.GGPOErrorCode;
import com.rose.ggpo.GGPOEventCode;
import com.rose.ggpo.GgpoCallbacks;
import com.rose.ggpo.GgpoEvent;
import com.rose.network.SyncTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Adler32;

public class TestGame implements GgpoCallbacks {
    private static final int NUM_PLAYERS = 2;
    private static final int MAX_TEST_FRAMES = 300;
    private GameState gs;
    private final NonGameState ngs;
    private final SyncTest syncTest;
    private long checksum;
    private int currentFrame;

    public TestGame() {
        Fighter[] fighters = new Fighter[NUM_PLAYERS];
        fighters[0] = new Ryu(new Vector2(250 / 2f - 32 / 2f, 20), true);
        fighters[1] = new Ken(new Vector2(500 / 2f - 32 / 2f, 20), false);
        syncTest = new SyncTest(this);
        gs = new GameState(fighters, 1, true);
        ngs = new NonGameState();

        System.out.println("Begin sync test now...");
        runTestLoop();
    }

    public void runTestLoop() {
        long now, next;
        next = System.nanoTime();

        while(currentFrame <= MAX_TEST_FRAMES) {
            now = System.nanoTime();
            syncTest.doPoll();
            if(now >= next) {
                runFrame();
                next = now + (1000L / 60);
                currentFrame++;
            }
        }
    }

    public void runFrame() {
        int input = getRandomInput();
        GGPOErrorCode result = syncTest.addLocalInput(input);
        if(GGPOErrorCode.GGPOSucceeded(result)) {
            int[] remoteInputs = syncTest.syncInput();
            if(remoteInputs != null) {
                advanceFrame(Gdx.graphics.getDeltaTime(), remoteInputs);
            }
        }
        drawCurrentFrame();
    }

    @Override
    public boolean beginGame(String name) {
        return true;
    }

    @Override
    public byte[] saveGameState() {
        return gs.saveGameState();
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
        return true;
    }

    @Override
    public boolean logGameState(String filename, String buffer) {
        return false;
    }

    @Override
    public Object freeBuffer(Object buffer) {
        if(buffer != null) {
            buffer = null;
        }
        return buffer;
    }

    @Override
    public boolean advanceFrame(int flags) {
        int[] inputs = syncTest.syncInput();
        advanceFrame(Gdx.graphics.getDeltaTime(), inputs);
        return true;
    }

    @Override
    public boolean onEvent(GgpoEvent event) {
        if (event.getCode() == GGPOEventCode.GGPO_EVENTCODE_CONNECTED_TO_PEER) {
            ngs.setConnectState(event.connected.playerHandle, NonGameState.PlayerConnectState.SYNCHRONIZING);
        }
        return false;
    }

    private void advanceFrame(float delta, int[] inputs) {
        gs.update(delta, inputs);
        ngs.now.frameNumber = gs.getFrameNumber();
        ngs.now.checksum = this.checksum;
        if((gs.getFrameNumber() % 90) == 0) {
            ngs.periodic = ngs.now;
        }

        syncTest.incrementFrame();

        // TODO: Update performance monitor when it's built here
    }

    private void drawCurrentFrame() {
        // do nothing since I don't care about drawing the game.
        // Kept to keep inline with how the game would run though.
    }

    private int getRandomInput() {
        int retval = 1;
        int randomInt = ThreadLocalRandom.current().nextInt(-1, 8);
        if(randomInt == -1) {
            return 0;
        }
        return retval << randomInt;
    }
}
