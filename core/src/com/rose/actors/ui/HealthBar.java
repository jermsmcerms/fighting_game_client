package com.rose.actors.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class HealthBar extends Actor {
    Sprite sprite;
    private final boolean invert;
    private final int max_width;

    public HealthBar(TextureRegion region, Vector2 position, boolean invert) {
        sprite = new Sprite(region);
        sprite.setPosition(position.x,  position.y);
        this.invert = invert;
        max_width = sprite.getRegionWidth();
    }

    public void update(int value) {
        sprite.setRegionWidth(value);
        if(sprite.getRegionWidth() < 0) {
            sprite.setRegionWidth(0);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(sprite, sprite.getX(), sprite.getY(),
            invert ? -sprite.getRegionWidth() : sprite.getRegionWidth(),
            sprite.getRegionHeight());
    }
}
