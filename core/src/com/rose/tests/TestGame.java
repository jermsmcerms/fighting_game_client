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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

/*
    The test game runs a suite of tests covering a range of possiblities such as input sync,
    connection testing, network syncing, time syncing, etc. All tests are ran one at a time.
    The pass/fail is shown at the end of each test.
 */
public class TestGame implements GgpoCallbacks {
    private static final int NUM_PLAYERS = 2;
    private static final int MAX_TEST_FRAMES = 100; // 1 frame at 60fps
    private static final int MAX_INPUT_QUEUE_TEST_FRAMES = 64;
    private static final int MAX_SEND_INPUT_FRAMES = 5000;
    private GameState gs;
    private final NonGameState ngs;
    private InputQueueTest queueTest;
    private SyncTest syncTest;
    private NetworkSyncTest nst;
    private ConnectTest connectTest;
    private SendInputTest sendInputTest;
    private String checksum;
    private int currentFrame;

    public TestGame() {
        Fighter[] fighters = new Fighter[NUM_PLAYERS];
        fighters[0] = new Ryu(new Vector2(250 / 2f - 32 / 2f, 20), true);
        fighters[1] = new Ken(new Vector2(500 / 2f - 32 / 2f, 20), false);
        gs = new GameState(fighters, 1, true);
        ngs = new NonGameState();
//        queueTest = new InputQueueTest(this);
//        queueTest.runTest(MAX_INPUT_QUEUE_TEST_FRAMES);
//        syncTest = new SyncTest(this);
//        syncTest.runSyncTest(MAX_TEST_FRAMES);
//        connectTest = new ConnectTest(this);
//        connectTest.runConnectTest();
//        nst = new NetworkSyncTest(this);
//        nst.runSyncTest();
        sendInputTest = new SendInputTest();
        sendInputTest.runTest(MAX_SEND_INPUT_FRAMES);
    }

    public void runSyncFrame() {
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

    public void runConnectRequestFrame() {

    }

    @Override
    public boolean beginGame(String name) {
        return true;
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
        return true;
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
        int[] inputs = syncTest.syncInput();
        advanceFrame(Gdx.graphics.getDeltaTime(), inputs);
        return true;
    }

    @Override
    public boolean onEvent(GgpoEvent event) {
        if (event.getCode() == GGPOEventCode.GGPO_EVENTCODE_CONNECTED_TO_SERVER) {
            ngs.setConnectState(event.connected.playerHandle, NonGameState.PlayerConnectState.SYNCHRONIZING);
        }
        return false;
    }

    private void advanceFrame(float delta, int[] inputs) {
        gs.update(delta, inputs);
        ngs.now.frameNumber = gs.getFrameNumber();
        ngs.now.checksum = this.checksum; // <-- always "". May still need to calculate checksum in GGPOCallbacks implementations.
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
