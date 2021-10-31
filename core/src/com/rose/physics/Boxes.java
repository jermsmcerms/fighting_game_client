package com.rose.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rose.animation.AnimationState;

import java.io.Serializable;

public class Boxes implements Serializable {
    static final long serialVersionUID = 47L;
    private final Type boxType;
    private final Rectangle box;
    private float init_width;
    private float init_height;

    private boolean active;

    public void restoreShape() {
        box.width = init_width;
        box.height = init_height;
    }

    public void resize(float x, float y, float width, float height) {
        box.x = x;
        box.y = y;
        box.width = width;
        box.height = height;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Type getType() {
        return boxType;
    }

    public boolean overlaps(Boxes other_box) {
        return box.x < other_box.getX() + other_box.getWidth() &&
            box.x + box.width > other_box.getX() &&
            box.y < other_box.getY() + other_box.getHeight() &&
            box.height + box.y > other_box.getY();
    }

    public void initialize(float x, float y, float width, float height) {
        if(box != null) {
            init_width = width;
            init_height = height;
            resize(x, y, init_width, init_height);
        }
    }

    public float loadInitWidth() {
        return init_width;
    }

    public float loadInitialHeight() {
        return init_height;
    }

    public enum Type {
        None, Collision, Hit, Hurt
    }


    public Boxes(Type type, boolean active) {
        this(type, 0f, 0f, 0f, 0f, active);
    }

    public Boxes(Type type, float x, float y, float width, float height, boolean active) {
        boxType = type;
        init_width = width;
        init_height = height;
        box = new Rectangle(x, y, init_width, init_height);
        this.active = active;
    }

    public Boxes(Type type, Vector2 topLeft, Vector2 bottomRight, boolean active) {
        this(type, topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, active);
    }

    public Boxes(Type type, float x, float x_offset, float y, float y_offset, float width, float height, boolean active) {
        this(type, x + x_offset, y + y_offset, width, height, active);
    }

    public void setX(float x) {
        box.x = x;
    }

    public float getX() {
        return box.x;
    }

    public float getY() {
        return box.y;
    }

    public float getWidth() {
        return box.getWidth();
    }

    public void setWidth(int width) {
        box.setWidth(width);
    }

    public float getHeight() {
        return box.getHeight();
    }

    public boolean isActive() {
        return active;
    }

    public void drawBox(ShapeRenderer sr) {
        sr.rect(box.x, box.y, box.getWidth(), box.getHeight());
    }

    public void update(float dt, float direction) {
        box.x += direction * dt;
    }

    /* For updating after calculating direction * dt */
    public void update(float pos) {
        box.x += pos;
    }

    public void orient(float offset) {
        box.x = offset;
    }

    public void keepInBounds() {
        if(box.x < 0) {
            box.x = 0;
        }
        if(box.x > 800 - box.getWidth() - 4) {
            box.x = 800 - box.getWidth() - 4;
        }
    }
}
