package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

public abstract class ScreenController implements Screen, InputProcessor {

    /**
     * This class defines common functionality across screens, such as resizing,
     * and declares functions to be overridden such as gathering assets
     */

    protected static int STANDARD_WIDTH = 1280;

    protected static int STANDARD_HEIGHT = 720;

    protected static float BUTTON_SCALE = 0.75f;

    protected int width;
    protected int height;
    /** Scaling factor for when the player changes the resolution. */
    protected float scale;

    /**
     * Whether or not this player mode is still active
     */
    protected boolean active;
    /**
     * Reference to GameCanvas created by the root
     */
    protected GameCanvas canvas;
    /**
     * Listener that will update the player mode when we are done
     */
    protected ScreenListener listener;

    protected CameraController camera;

    public void setActive() {
        active = true;
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public abstract void gatherAssets(AssetDirectory directory);

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = BUTTON_SCALE * (sx < sy ? sx : sy);
        this.width = width;
        this.height = height;
        camera.getCamera().setToOrtho(false, width, height);
        camera.getCamera().update();
    }

    public void show() {
        // Useless if called in outside animation loop
        Gdx.input.setInputProcessor(this);
        active = true;
    }
    public void hide() {
        Gdx.input.setInputProcessor(null);
        active = false;
    }
}
