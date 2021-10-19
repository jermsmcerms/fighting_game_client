package com.rose.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rose.actors.Ken;
import com.rose.actors.Ryu;

public class MatchUI {
    int timer;
    float stateTime;
    Label timerLabel;
    ProgressBar health_bar;
    ProgressBar health_bar2;
    private boolean match_over;

    public MatchUI() {
        stateTime = 0.0f;
        timer = 30;
    }

    public void showUI(Stage stage) {
        Skin skin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));
        Gdx.input.setInputProcessor(stage);

        timerLabel = new Label(Integer.toString(timer), skin);
        timerLabel.setPosition(stage.getCamera().viewportWidth / 2.0f - timerLabel.getWidth() / 2.0f, stage.getCamera().viewportHeight - timerLabel.getHeight());
        stage.addActor(timerLabel);

        health_bar = new ProgressBar(-1, 100, 1, false, skin);
        health_bar.setValue(100);
        health_bar.setPosition(0, stage.getCamera().viewportHeight - health_bar.getHeight()-5);
        stage.addActor(health_bar);

        health_bar2 = new ProgressBar(-1, 100, 1, false, skin);
        health_bar2.setValue(100);
        health_bar2.setPosition(stage.getCamera().viewportWidth - health_bar2.getWidth(), stage.getCamera().viewportHeight-health_bar2.getHeight()-5);
        stage.addActor(health_bar2);
    }

    public void updateUI(float delta, Ryu ryu, Ken ken) {
        stateTime += delta;
        if(stateTime >= 1.0f && timer > 0) {
            timer--;
            if(timer <= 0) {
                match_over = true;
            }
            if(timer >= 10) {
                timerLabel.setText(timer);
            } else {
                timerLabel.setText("0" + timer);
            }
            stateTime = 0.0f;
        }
        health_bar.setValue(ryu.getHealth());
        health_bar2.setValue(ken.getHealth());
        if(health_bar.getValue() <= 0 || health_bar2.getValue() <= 0) {
            match_over = true;
        }
    }

    public int getTimer() {
        return timer;
    }

    public boolean isMatchOver() {
        return match_over;
    }
}
