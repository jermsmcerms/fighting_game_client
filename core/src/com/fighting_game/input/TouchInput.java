package com.fighting_game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class TouchInput {
    private Skin skin;
    private Button button;
    private int input;

    public TouchInput() {
        skin = new Skin(Gdx.files.internal("glassy/skin/glassy-ui.json"));
        button = new Button(skin, "small");
        button.setSize(50, 50);
        button.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("touch down");
                input = 1;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("touch up");
                input = 0;
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    public Button getButton() {
        return button;
    }

    public int getInput() {
        return input;
    }
}
