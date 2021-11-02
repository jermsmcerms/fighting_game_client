package com.rose.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class ButtonActor extends Button {
    public boolean drawable = true;

    public ButtonActor(ButtonStyle style) {
        super(style);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if(drawable) {
            System.out.println(this.getStyle().up);
            super.draw(batch, parentAlpha);
        }
    }
}
