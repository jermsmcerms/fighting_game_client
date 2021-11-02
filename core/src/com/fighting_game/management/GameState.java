package com.fighting_game.management;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.fighting_game.actors.Fighter;

public class GameState {
    public Fighter fighter;
    public int speed;
    public GameState() {
        this.fighter = new Fighter();
    }

    public void update(float delta, int input) {
        // process input
        if(input == 1) {
            speed = 100;
        } else {
            speed = 0;
        }
        // move player
        fighter.act(delta * speed);
    }

    // TODO: Change to array of actors
    public Actor getActors() {
        return fighter;
    }
}
