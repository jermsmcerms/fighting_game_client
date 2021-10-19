package com.rose.screens;

import com.badlogic.gdx.Screen;
import com.rose.main.Rose;

public class LoadingScreen extends ScreenBase {
    public LoadingScreen(Rose parent) {
        super(parent);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        this.parent.changeScreen(Rose.ScreenType.MENU);
    }

    @Override
    public void resize(int width, int height) {

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

    }
}
