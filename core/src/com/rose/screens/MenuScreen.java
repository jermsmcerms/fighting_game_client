package com.rose.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rose.main.Rose;

public class MenuScreen extends ScreenBase {
    TextButton onlineGame;
    TextButton trainingMode;
    TextButton tests;
    TextButton exit;

    public MenuScreen(Rose parent) {
        super(parent);
        Gdx.input.setInputProcessor(stage);
        onlineGame      = new TextButton("Online Match",    default_skin);
        trainingMode    = new TextButton("Training Mode",   default_skin);
        tests           = new TextButton("GGPO Test",       default_skin);
        exit            = new TextButton("Exit",            default_skin);

        onlineGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.setTrainingMode(false);
                parent.changeScreen(Rose.ScreenType.APPLICATION);
            }
        });

        trainingMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.setTrainingMode(true);
                parent.changeScreen(Rose.ScreenType.APPLICATION);
            }
        });

        tests.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Rose.ScreenType.TEST);
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
        stage.clear();
        Gdx.input.setInputProcessor(stage);
        Viewport viewport = new FitViewport(800, 480, stage.getCamera());
        stage.setViewport(viewport);
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(onlineGame).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(trainingMode).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(tests).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(exit).fillX().uniform();
        table.row().pad(10,0,0,0);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        stage.act(Math.min(delta, 1/60f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
