package com.fighting_game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fighting_game.Main;

public class MainMenuScreen extends ScreenBase {
    TextButton trainingMode;
    TextButton preferences;
    TextButton exit;

    public MainMenuScreen(Main parent) {
        super(parent);
        Gdx.input.setInputProcessor(stage);
        trainingMode = new TextButton("Training Mode", default_skin);
        preferences = new TextButton("Preferences", default_skin);
        exit = new TextButton("Exit", default_skin);

        trainingMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Main.ScreenType.APPLICATION);
            }
        });

        preferences.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Main.ScreenType.PREFERENCES);
            }
        });

        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        stage.addActor(table);

        table.add(trainingMode).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(preferences).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(exit).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.setPosition(stage.getWidth() / 2.0f, stage.getHeight() / 2.0f);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act(Math.min(delta, 1/60f));
        stage.draw();
    }
}
