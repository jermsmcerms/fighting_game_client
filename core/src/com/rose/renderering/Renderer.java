package com.rose.renderering;

import com.rose.management.GameState;
import com.rose.management.NonGameState;
import com.rose.ui.MatchUI;
import com.rose.ui.TouchControlsUI;

public class Renderer {
    private MatchUI matchUI;
    private TouchControlsUI touchControlsUI;

    public Renderer() {
        matchUI = new MatchUI();
        touchControlsUI = new TouchControlsUI();
    }

    public void draw(GameState gameState, NonGameState nonGameState) {

    }
}
