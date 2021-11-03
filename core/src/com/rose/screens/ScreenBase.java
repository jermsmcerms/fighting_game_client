package com.rose.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rose.main.Rose;

public class ScreenBase implements Screen {
    protected Rose parent;
    protected Stage stage;
    protected OrthographicCamera camera;
    protected Skin default_skin;
    protected Table table;

    public ScreenBase(Rose parent) {
        this.parent = parent;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 420, 220);
        stage = new Stage(new FitViewport(camera.viewportWidth, camera.viewportHeight, camera));
        default_skin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));
        table = new Table();
    }

    @Override
    public void show() {
        stage.clear();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        stage.dispose();
        parent.dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
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
}
