package com.rose.animation;

import com.badlogic.gdx.utils.Array;

import java.util.Optional;

public class SpriteAnimation<T> {
    public static final float FRAME_LENGTH = 1.0f/60.0f;
    private final Array<T> frames;
    private final float[] frameTimes;

    public SpriteAnimation(Array<T> frames, float[] frameTimes) {
        this.frames = frames;
        this.frameTimes = frameTimes;
    }

    public Optional<T> getFrame(final float stateTime) {
        float currentTime = stateTime;
        int index = 0;

        while(  index < frameTimes.length &&
                currentTime > frameTimes[index] &&
                index < frames.size) {
            currentTime -= frameTimes[index];
            index++;
        }

        return index < frames.size ?
                Optional.of(frames.get(index)) :
                Optional.empty();
    }

    public float getAnimationDuration() {
        float sum = 0.f;
        for(float item : frameTimes) {
            sum += item;
        }
        return sum;
    }
}