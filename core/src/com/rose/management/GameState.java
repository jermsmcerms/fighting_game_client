package com.rose.management;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.rose.actors.Fighter;
import com.rose.ui.TouchControlsUI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GameState implements Serializable {
    public static final long serialVersionUID = 42L;
    private final Fighter[] fighters;
    private final boolean syncTest;
    private int frameCount;
    private final int playerNumber;
    transient ShapeRenderer sr;

    public GameState(Fighter[] fighters, int playerNumber, boolean syncTest) {
        this.fighters = fighters;
        this.playerNumber = playerNumber;
        this.syncTest = syncTest;
        sr = new ShapeRenderer();
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
                fighters[i].keepInBounds();
                if (fighters[i].isAttacking()) {
                    fighters[i].checkForHit(fighters[(i + 1) % fighters.length], delta);
                }
            }
        }

    }

    public byte[] saveGameState() {
        byte[] data = null;
        printState();
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

    public int getFrameNumber() {
        return frameCount;
    }

    public Fighter[] getFighters() {
        return fighters;
    }

    public boolean gameOver() {
        return fighters[0].getHealth() <= 0 || fighters[1].getHealth() <= 0;
    }

    public void printState() {
        for(int i = 0; i < fighters.length; i++) {
            System.out.printf("Player %d:\n\tPosition: (x: %.2f, y: %.2f)\n",
                    (i + 1),
                    fighters[i].getAnchor().x,
                    fighters[i].getAnchor().y);
            System.out.printf("\tAnimation State: %s\n", fighters[i].getAnimationState());
        }
    }
}
