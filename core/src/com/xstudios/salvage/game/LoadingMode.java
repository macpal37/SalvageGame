package com.xstudios.salvage.game;


import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.Controllers;
import com.xstudios.salvage.util.ScreenListener;
import com.xstudios.salvage.util.XBoxController;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * <p>You still DO NOT need to understand this class for this lab. We will talk about this class
 * much later in the course. This class provides a basic template for a loading screen to be used at
 * the start of the game or between levels. Feel free to adopt this to your needs.
 *
 * <p>You will note that this mode has some textures that are not loaded by the AssetManager. You
 * are never required to load through the AssetManager. But doing this will block the application.
 * That is why we try to have as few resources as possible for this loading screen.
 */
public class LoadingMode implements Screen {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /**
     * Internal assets for this loading screen
     */
    private AssetDirectory internal;
    /**
     * The actual assets to be loaded
     */
    private AssetDirectory assets;

    /**
     * Background texture for start-up
     */
    private Texture background;
    /**
     * Play button to display when done
     */
    private boolean play;

    // statusBar is a "texture atlas." Break it up into parts.
    /**
     * Left cap to the status background (grey region)
     */
    private TextureRegion statusBkgLeft;
    /**
     * Middle portion of the status background (grey region)
     */
    private TextureRegion statusBkgMiddle;
    /**
     * Right cap to the status background (grey region)
     */
    private TextureRegion statusBkgRight;
    /**
     * Left cap to the status forground (colored region)
     */
    private TextureRegion statusFrgLeft;
    /**
     * Middle portion of the status forground (colored region)
     */
    private TextureRegion statusFrgMiddle;
    /**
     * Right cap to the status forground (colored region)
     */
    private TextureRegion statusFrgRight;

    /**
     * Default budget for asset loader (do nothing but load 60 fps)
     */
    private static int DEFAULT_BUDGET = 15;
    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;
    /**
     * Ratio of the bar width to the screen
     */
    private static float BAR_WIDTH_RATIO = 0.66f;
    /**
     * Ration of the bar height to the screen
     */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /**
     * Height of the progress bar
     */
    private static float BUTTON_SCALE = 0.75f;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

    /**
     * The width of the progress bar
     */
    private int width;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;

    /**
     * Current progress (0 to 1) of the asset manager
     */
    private float progress;
    /**
     * The amount of time to devote to loading assets (as opposed to on screen hints, etc.)
     */
    private int budget;

    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    /**
     * Returns the asset directory produced by this loading screen
     *
     * <p>This asset loader is NOT owned by this loading scene, so it persists even after the scene is
     * disposed. It is your responsbility to unload the assets in this directory.
     *
     * @return the asset directory produced by this loading screen
     */
    public AssetDirectory getAssets() {
        return assets;
    }

    /**
     * Creates a LoadingMode with the default budget, size and position.
     *
     * @param file   The asset directory to load in the background
     * @param canvas The game canvas to draw to
     */
    public LoadingMode(String file, GameCanvas canvas) {
        this(file, canvas, DEFAULT_BUDGET);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * <p>The budget is the number of milliseconds to spend loading assets each animation frame. This
     * allows you to do something other than load assets. An animation frame is ~16 milliseconds. So
     * if the budget is 10, you have 6 milliseconds to do something else. This is how game companies
     * animate their loading screens.
     *
     * @param file   The asset directory to load in the background
     * @param canvas The game canvas to draw to
     * @param millis The loading budget in milliseconds
     */
    public LoadingMode(String file, GameCanvas canvas, int millis) {
        this.canvas = canvas;
        budget = millis;
        // Compute the dimensions from the canvas
        resize(canvas.getWidth(), canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory("loading.json");
        internal.loadAssets();
        internal.finishLoading();

        background = internal.getEntry("background", Texture.class);
        background.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        // Break up the status bar texture into regions
        statusBkgLeft = internal.getEntry("progress.backleft", TextureRegion.class);
        statusBkgRight = internal.getEntry("progress.backright", TextureRegion.class);
        statusBkgMiddle = internal.getEntry("progress.background", TextureRegion.class);

        statusFrgLeft = internal.getEntry("progress.foreleft", TextureRegion.class);
        statusFrgRight = internal.getEntry("progress.foreright", TextureRegion.class);
        statusFrgMiddle = internal.getEntry("progress.foreground", TextureRegion.class);

        // No progress so far.
        progress = 0;
        play = false;

        // Start loading the real assets
        assets = new AssetDirectory(file);
        assets.loadAssets();
        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        play = false;
        internal.unloadAssets();
        internal.dispose();
    }

    /**
     * Update the status of this player mode.
     *
     * <p>We prefer to separate update and draw from one another as separate methods, instead of using
     * the single render() method that LibGDX does. We will talk about why we prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (play == false) {
            assets.update(budget);
            this.progress = assets.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;
                play = true;
            }
        }
    }

    /**
     * Draw the status of this player mode.
     *
     * <p>We prefer to separate update and draw from one another as separate methods, instead of using
     * the single render() method that LibGDX does. We will talk about why we prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background, 0, 0);
        if (play == false) drawProgress(canvas);
        canvas.end();
    }

    /**
     * Updates the progress bar according to loading progress
     *
     * <p>The progress bar is composed of parts: two rounded caps on the end, and a rectangle in a
     * middle. We adjust the size of the rectangle in the middle to represent the amount of progress.
     *
     * @param canvas The drawing context
     */
    private void drawProgress(GameCanvas canvas) {
        canvas.draw(
                statusBkgLeft,
                Color.WHITE,
                0,
                0,
                scale * statusBkgLeft.getRegionWidth(),
                scale * statusBkgLeft.getRegionHeight());
        if (progress > 0) {
            float span =
                    progress
                            * (width - scale * (statusBkgLeft.getRegionWidth() + statusBkgRight.getRegionWidth()))
                            / 2.0f;
            canvas.draw(
                    statusBkgRight,
                    Color.WHITE,
                    scale * statusBkgLeft.getRegionWidth() + span,
                    0,
                    scale * statusBkgRight.getRegionWidth(),
                    scale * statusBkgRight.getRegionHeight());
            canvas.draw(
                    statusBkgMiddle,
                    Color.WHITE,
                    scale * statusBkgLeft.getRegionWidth(),
                    0,
                    span,
                    scale * statusBkgMiddle.getRegionHeight());
        } else {
            canvas.draw(
                    statusBkgRight,
                    Color.WHITE,
                    scale * statusBkgLeft.getRegionWidth(),
                    0,
                    scale * statusBkgRight.getRegionWidth(),
                    scale * statusBkgRight.getRegionHeight());
        }
    }

    // ADDITIONAL SCREEN METHODS

    /**
     * Called when the Screen should render itself.
     *
     * <p>We defer to the other methods update() and draw(). However, it is VERY important that we
     * only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are  ready, notify our listener
            if (play == true && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * <p>This can happen at any point during a non-paused state but will never happen before a call
     * to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        this.width = (int) (BAR_WIDTH_RATIO * width);
    }

    /**
     * Called when the Screen is paused.
     *
     * <p>This is usually when it's not active or visible on screen. An Application is also paused
     * before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * <p>This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * <p>The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}

