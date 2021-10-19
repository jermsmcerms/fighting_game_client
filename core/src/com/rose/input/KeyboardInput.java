package com.rose.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class KeyboardInput implements InputProcessor {
    public boolean moveLeft;
    public boolean moveRight;

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){
            // Using up and down because default phone orientation is vertical.
            case Input.Keys.UP:
                moveLeft = true;
                moveRight = false;
                break;
            case Input.Keys.DOWN:
                moveRight = true;
                moveLeft = false;
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){
            case Input.Keys.UP:
                moveLeft = false;
                moveRight = false;
                break;
            case Input.Keys.DOWN:
                moveRight = false;
                moveLeft = false;
                break;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
