package com.rose.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import com.rose.actors.Fighter;
import com.rose.actors.Ken;
import com.rose.actors.Ryu;
import com.rose.ggpo.GgpoCallbacks;
import com.rose.ggpo.GgpoEvent;
import com.rose.main.Rose;
import com.rose.management.GameState;
import com.rose.management.NonGameState;
import com.rose.management.SaveGameState;
import com.rose.management.Utilities;
import com.rose.network.Client;
import com.rose.ui.MatchUI;
import com.rose.ui.TouchControlsUI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class MainScreen extends ScreenBase implements GgpoCallbacks {
    private static final boolean DEBUG = false;
    private static final String gameName = "Rose";
    private final boolean trainingMode;
    private MatchUI ui;
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private Client client;
    private Fighter[] fighters;
    private Ryu ryu;
    private Ken ken;

    private final int playerNumber;
    private long next, now;
    private TouchControlsUI touchInputUI;
    private GameState gs;
    private NonGameState ngs;

    private int randomInput;
    private String checksum;

    public MainScreen(Rose parent) {
        super(parent);
        trainingMode = true;
        playerNumber = 1;
        initMatch();
    }

    public MainScreen(Rose parent, Client client) {
        super(parent);
        this.client = client;
        playerNumber = client.getPlayerNumber();
        trainingMode = false;
        now = next = System.nanoTime();
        initMatch();
    }

    // TODO: add load and save frame functions

    @Override
    public void show() {
        super.show();
        if(ui != null) {
            ui.showUI(stage);
        }

        if(touchInputUI != null) {
            touchInputUI.showUI(stage);
        }
    }

    @Override
    public void render(float delta) {
        now = System.nanoTime();
        if(client != null) {
            try {
                client.doPoll(Math.max(0, next - now - 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(now >= next) {
            runFrame(delta);
            now = next + (1000000000/60);
        }
    }

    private void initMatch() {
        ui = new MatchUI();
        touchInputUI = new TouchControlsUI();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 400, 240);
        buildFighters();

        gs = new GameState(new Fighter[]{ryu, ken}, playerNumber, false);
        ngs = new NonGameState();
        randomInput = getRandomInput();
    }

    private void runFrame(float delta) {
        super.render(delta);
        int input = 0;
        if(DEBUG) {
//            if(playerNumber == 1) {
//                randomInput = ThreadLocalRandom.current().nextInt(3);
//            } else if (playerNumber == 2) {
//                randomInput = 0;
//            }
            randomInput =  ThreadLocalRandom.current().nextInt(3);
        } else {
            input = touchInputUI.getInput();
        }
        int[] inputs;
        if(!trainingMode) {
            boolean result;
            if(DEBUG) {
                result = client.addLocalInput(randomInput);
            } else {
                result = client.addLocalInput(input);
            }
            if (result) {
//                inputs = client.syncInput();
//                if (inputs != null) {
//                    advanceFrame(delta, inputs);
//                }
            }
        } else {
            inputs = new int[]{input, 0};
            advanceFrame(delta, inputs);
        }
        drawCurrentFrame(delta);
    }

    private int getRandomInput() {
        int retval = 1;
        int randomInt = ThreadLocalRandom.current().nextInt(-1, 8);
        if(randomInt == -1) {
            return 0;
        }
        return retval << randomInt;
    }

    private void buildFighters() {
        ryu = new Ryu(new Vector2(250 / 2f - 32 / 2f, 20), true);
        ken = new Ken(new Vector2(500 / 2f - 32 / 2f, 20), false);
    }

    private void advanceFrame(float delta, int[] inputs) {
        // inputs[0] is always the local player.
        // inputs[1] is always the remote player.
        // player 1 is always ryu
        // player 2 is always ken
        gs.update(delta, inputs);
        ngs.now.frameNumber = gs.getFrameNumber();
        ngs.now.checksum = this.checksum; // <-- always "". May still need to calculate checksum in GGPOCallbacks implementations.
        if((gs.getFrameNumber() % 90) == 0) {
            ngs.periodic = ngs.now;
        }
        if(!trainingMode) {
            try {
                client.incrementFrame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawCurrentFrame(float delta) {
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        ryu.draw(delta, batch, camera);
        ken.draw(delta, batch, camera);
        batch.end();
        // TODO: Eventually I'll want to replace this debug call with code that draws
        //       a sprite representation of the player's boxes.
        ryu.drawDebug(camera);
        ken.drawDebug(camera);

        // Draw UI
        ui.updateUI(delta, ryu, ken);
        if(ui.isMatchOver()) {
            this.parent.changeScreen(Rose.ScreenType.MENU);
        }
        if(ui.getTimer() <= 0) {
            this.parent.changeScreen(Rose.ScreenType.MENU);
        }
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
            in.close();
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
        int[] inputs = client.syncInput();
        advanceFrame(Gdx.graphics.getDeltaTime(), inputs);
        return true;
    }

    @Override
    public boolean onEvent(GgpoEvent event) {
        switch(event.getCode()){
            case GGPO_EVENTCODE_CONNECTED_TO_SERVER:
                ngs.setConnectState(event.connected.playerHandle, NonGameState.PlayerConnectState.SYNCHRONIZING);
                break;
            case GGPO_EVENTCODE_TIMESYNC:
                try {
                    Thread.sleep((long) event.timeSync.frames_ahead * (1000/60));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }
}
