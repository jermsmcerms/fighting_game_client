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
    TextButton preferences;
    TextButton syncTest;
    TextButton exit;

    public MenuScreen(Rose parent) {
        super(parent);
        Gdx.input.setInputProcessor(stage);
        music = Gdx.audio.newMusic(Gdx.files.internal("music/theme.ogg"));
        music.setVolume(2.0f);
        music.setLooping(true);
        music.play();
    }

    @Override
    public void show() {
        stage.clear();
        Gdx.input.setInputProcessor(stage);
        Viewport viewport = new FitViewport(800, 480, stage.getCamera());
        stage.setViewport(viewport);
        Table table = new Table();
        table.setFillParent(true);
//        table.setDebug(true);
        stage.addActor(table);

        Skin skin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));

        onlineGame = new TextButton("Online Match", skin);
        trainingMode = new TextButton("Training Mode", skin);
        preferences = new TextButton("Preferences", skin);
        syncTest = new TextButton("syncTest", skin);
        exit = new TextButton("Exit", skin);

        table.add(onlineGame).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(trainingMode).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(preferences).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(syncTest).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.add(exit).fillX().uniform();
        table.row().pad(10,0,0,0);

        onlineGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                music.stop();
                music.dispose();
                parent.setTrainingMode(false);
                parent.changeScreen(Rose.ScreenType.APPLICATION);
            }
        });

        trainingMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                music.stop();
                music.dispose();
                parent.setTrainingMode(true);
                parent.changeScreen(Rose.ScreenType.APPLICATION);
            }
        });

        preferences.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Rose.ScreenType.PREFERENCES);
            }
        });

        syncTest.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(Rose.ScreenType.SYNCTEST);
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
    public void render(float delta) {
        Gdx.gl.glClearColor(0f,0f,0f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/60f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
