package com.rose.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

import com.rose.actors.Fighter;
import com.rose.actors.Ken;
import com.rose.actors.Ryu;
import com.rose.actors.TextureActor;
import com.rose.ggpo.GgpoCallbacks;
import com.rose.ggpo.GgpoEvent;
import com.rose.main.Rose;
import com.rose.management.GameState;
import com.rose.management.NonGameState;
import com.rose.management.SaveGameState;
import com.rose.management.Utilities;
import com.rose.network.Client;
import com.rose.ui.TouchControlsUI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApplicationScreen extends ScreenBase implements GgpoCallbacks {
    private final boolean trainingMode;

    private Client client;
    private Ryu ryu;
    private Ken ken;

    private final int playerNumber;
    private long next, now;
    private TouchControlsUI touchInputUI;
    private GameState gs;
    private NonGameState ngs;

    private String checksum;

    private TextureActor background;
    private TextureActor ui_overlay;

    public ApplicationScreen(Rose parent) {
        super(parent);
        trainingMode = true;
        playerNumber = 1;
        initMatch();
    }

    public ApplicationScreen(Rose parent, Client client) {
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
        // Add background actor.
        stage.addActor(background);
        // Add UI overlay actor.
        stage.addActor(ui_overlay);
        // Add fighters
        for(int i = 0; i < gs.getFighters().length; i++) {
            stage.addActor(gs.getFighters()[i]);
        }

        // add touch buttons
        for(int i = 0; i < touchInputUI.getButtons().size(); i++) {
            stage.addActor(touchInputUI.getButtons().get(i));
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
        background = new TextureActor(Gdx.files.internal("sample_background.png"));
        ui_overlay = new TextureActor(Gdx.files.internal("match_ui_overlay.png"));
        touchInputUI = new TouchControlsUI(stage);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 420, 220);
        buildFighters();

        gs = new GameState(new Fighter[]{ryu, ken}, playerNumber, false);
        ngs = new NonGameState();
    }

    private void runFrame(float delta) {
        super.render(delta);
        int input = touchInputUI.getInput();
        int[] inputs;
        if(!trainingMode) {
            boolean result;
            result = client.addLocalInput(input);
            if (result) {
                inputs = client.syncInput();
                if (inputs != null) {
                    advanceFrame(delta, inputs);
                }
            }
        } else {
            inputs = new int[]{input, 0};
            advanceFrame(delta, inputs);
        }

        stage.draw();
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
        return null;
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
