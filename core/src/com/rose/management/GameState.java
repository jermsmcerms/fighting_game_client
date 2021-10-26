package com.rose.management;

import com.rose.actors.Fighter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class GameState implements Serializable {
    public static final long serialVersionUID = 42L;
    private final Fighter[] fighters;
    private final boolean syncTest;
    private int frameCount;
    private int playerNumber;

    public GameState(Fighter[] fighters, int playerNumber, boolean syncTest) {
        this.fighters = fighters;
        this.playerNumber = playerNumber;
        this.syncTest = syncTest;
    }

    public void update(float delta, int[] inputs) {
        frameCount++;
        if(syncTest) {
            fighters[0].update(delta, inputs[0]);
            fighters[1].update(delta, 0);
        } else {
            if (playerNumber == 1) {
                fighters[0].update(delta, inputs[0]);
                fighters[1].update(delta, inputs[1]);
            } else if (playerNumber == 2) {

                fighters[0].update(delta, inputs[1]);
                fighters[1].update(delta, inputs[0]);
            }

            for (int i = 0; i < fighters.length; i++) {
                if (fighters[i].isAttacking()) {
                    fighters[i].checkForHit(fighters[(i + 1) % fighters.length]);
                }
            }
        }
    }

    public byte[] saveGameState() {
        byte[] data = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out;
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            data = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void loadGameState() {
    }

    public int getFrameNumber() {
        return frameCount;
    }
}
