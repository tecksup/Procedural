package com.tecksup.experiment.data;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Ported to java and modified by tecksup, originally "Created by Darius on 17/01/2018" in kotlin
 */
public class TkTextureAnimation<T> {

    Array<T> frames;
    float frameDuration;

    boolean loop;
    public boolean isPaused = false;

    float totalAnimationDuration;
    float elapsedTime = 0;

    public boolean onLastFrame = false;
    boolean onStartFrame = false;
    boolean onStartFrameCache = true;
    boolean finishedOneLoop = false;

    public TkTextureAnimation(Array<T> frames, float frameDuration, boolean loop) {
        this.frames = frames;
        this.frameDuration = frameDuration;

        this.loop = loop;

        this.totalAnimationDuration = frameDuration * frames.size;
    }

    public TkTextureAnimation() {
        this.frames = null;
        this.frameDuration = 0;

        this.loop = false;

        this.totalAnimationDuration = 0;
    }

    public TkTextureAnimation(Array<T> frames, float frameDuration) {
        this(frames, frameDuration, true);
    }

    public T getFrame() {
        if (frames != null)
            return frames.get(getFrameIndex());
        return null;
    }

    public void update(float delta) {
        if (delta > 0) {
            onStartFrame = onStartFrameCache;
            onStartFrameCache = false;
        }

        onLastFrame = false;

        if (!isPaused) {
            elapsedTime += delta;

            //if loop and animation has played through
            if (!loop && elapsedTime >= totalAnimationDuration) {
                finishedOneLoop = true;
                onLastFrame = true;
                //Now we halt until a reset is called
            } else {
                if (elapsedTime >= totalAnimationDuration) {
                    onLastFrame = true;
                    reset();
                }
            }

        }
    }

    public boolean hasFinishedLoop() {
        return finishedOneLoop;
    }

    public boolean onLastFrame() {
        return onLastFrame;
    }

    public boolean onStartFrame() {
        return onStartFrame;
    }

    public int getFrameIndex() {
        int index = MathUtils.floor((elapsedTime / frameDuration));
        index = Math.max(index, 0);
        index = Math.min(index, frames.size - 1);
        return index;
    }

    public int getFutureFrameIndex(float Delta) {
        int index = MathUtils.floor(((elapsedTime+Delta) / frameDuration));
        index = Math.max(index, 0);
        index = Math.min(index, frames.size - 1);
        return index;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public int getNumberOfFrames() {
        return frames.size;
    }

    public float getFrameDuration() {
        return frameDuration;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void reset() {
        finishedOneLoop = false;
        onStartFrameCache = true;
        elapsedTime = 0f;
    }

    public void setFrame(int index) {
        elapsedTime = index * frameDuration;
        finishedOneLoop = false;
    }

}
