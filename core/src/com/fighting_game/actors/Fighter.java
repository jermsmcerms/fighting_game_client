package com.fighting_game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.io.Serializable;

public class Fighter extends Actor implements Serializable {
    public Vector2 position;

    private final transient Texture texture;

    public Fighter() {
        texture = new Texture(Gdx.files.internal("characters/idle_00.png"));
        position = new Vector2(
            (800 - texture.getWidth())  / 2.0f,
            (480 - texture.getHeight()) / 2.0f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        position.x += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(texture, position.x, position.y, texture.getWidth(), texture.getHeight());
    }
}
