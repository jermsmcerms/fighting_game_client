package com.rose.animation;

import java.io.Serializable;

public enum AnimationState implements Serializable {
    IDLE, FWD_DASH, BWD_DASH, BLOCK, HIT, KNOCK_DOWN, GET_UP,
    C_LIGHT, C_MEDIUM, F_LIGHT, F_MEDIUM,
    HEAVY, SPECIAL, SUPER,
    FWD_THROW, BWD_THROW;

    public static int length() {
        return AnimationState.values().length;
    }
    static final long serialVersionUID = 46L;
}
