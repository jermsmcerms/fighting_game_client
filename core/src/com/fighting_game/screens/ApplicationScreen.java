package com.fighting_game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.fighting_game.Main;
import com.fighting_game.actors.Fighter;
import com.fighting_game.actors.TextureActor;
import com.fighting_game.input.TouchInput;
import com.fighting_game.management.GameState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ApplicationScreen extends ScreenBase {
    TextureActor background;
    TextButton mainMenu;
    TouchInput touchInput;
    GameState gameState;

    public ApplicationScreen(Main parent) {
        super(parent);
        background = new TextureActor();
        background.texture = new Texture(Gdx.files.internal("stages/stage.png"));

        touchInput = new TouchInput();
        gameState = new GameState();

        mainMenu = new TextButton("Main menu", default_skin);
        mainMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                parent.changeScreen(Main.ScreenType.MENU);
            }
        });
    }

    @Override
    public void show() {
        super.show();
        stage.addActor(table);
        table.add(mainMenu).fillX().uniform();
        table.row().pad(10,0,0,0);
        table.setPosition(stage.getWidth() / 2.0f, mainMenu.getHeight() - 50);

        stage.addActor(background);
        background.setPosition(
            (camera.viewportWidth  - background.texture.getWidth())  / 2.0f,
            (camera.viewportHeight - background.texture.getHeight()) / 2.0f);

        touchInput.getButton().setPosition(
            (camera.viewportWidth  - touchInput.getButton().getWidth())  / 2.0f,
            (camera.viewportHeight - touchInput.getButton().getHeight()) / 2.0f);
        stage.addActor(touchInput.getButton());

        stage.addActor(gameState.getActors());
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        gameState.update(delta, touchInput.getInput());
        stage.draw();
    }
}
