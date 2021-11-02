package com.fighting_game.screens;

import com.fighting_game.Main;

public class LoadingScreen extends ScreenBase {
    public LoadingScreen(Main parent) {
        super(parent);
    }

    @Override
    public void render(float delta) {
        this.parent.changeScreen(Main.ScreenType.MENU);
    }
}
