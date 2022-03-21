package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
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
public class LoadingMode implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    /** Background texture for start-up */
    private Texture background;
    /** Play button to display when done */
    private Texture playButton;
    /** Texture atlas to support a progress bar */
    private final Texture statusBar;

    // statusBar is a "texture atlas." Break it up into parts.
    /** Left cap to the status background (grey region) */
    private TextureRegion statusBkgLeft;
    /** Middle portion of the status background (grey region) */
    private TextureRegion statusBkgMiddle;
    /** Right cap to the status background (grey region) */
    private TextureRegion statusBkgRight;
    /** Left cap to the status forground (colored region) */
    private TextureRegion statusFrgLeft;
    /** Middle portion of the status forground (colored region) */
    private TextureRegion statusFrgMiddle;
    /** Right cap to the status forground (colored region) */
    private TextureRegion statusFrgRight;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO = 0.66f;
    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Height of the progress bar */
    private static float BUTTON_SCALE = 0.75f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The width of the progress bar */
    private int width;
    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Current progress (0 to 1) of the asset manager */
    private float progress;
    /** The current state of the play button */
    private int pressState;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int budget;

    /** Whether or not this player mode is still active */
    private boolean active;

    /**
     * Returns the budget for the asset loader.
     *
     * <p>The budget is the number of milliseconds to spend loading assets each animation frame. This
     * allows you to do something other than load assets. An animation frame is ~16 milliseconds. So
     * if the budget is 10, you have 6 milliseconds to do something else. This is how game companies
     * animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     *
     * <p>The budget is the number of milliseconds to spend loading assets each animation frame. This
     * allows you to do something other than load assets. An animation frame is ~16 milliseconds. So
     * if the budget is 10, you have 6 milliseconds to do something else. This is how game companies
     * animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }

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
     * @param file The asset directory to load in the background
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
     * @param file The asset directory to load in the background
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

        // Load the next two images immediately.
        playButton = null;
        background = internal.getEntry("background", Texture.class);
        background.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        statusBar = internal.getEntry("progress", Texture.class);

        // Break up the status bar texture into regions
        statusBkgLeft = internal.getEntry("progress.backleft", TextureRegion.class);
        statusBkgRight = internal.getEntry("progress.backright", TextureRegion.class);
        statusBkgMiddle = internal.getEntry("progress.background", TextureRegion.class);

        statusFrgLeft = internal.getEntry("progress.foreleft", TextureRegion.class);
        statusFrgRight = internal.getEntry("progress.foreright", TextureRegion.class);
        statusFrgMiddle = internal.getEntry("progress.foreground", TextureRegion.class);

        // No progress so far.
        progress = 0;
        pressState = 0;

        Gdx.input.setInputProcessor(this);

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }

        // Start loading the real assets
        assets = new AssetDirectory(file);
        assets.loadAssets();
        active = true;
    }

    /** Called when this screen should release all resources. */
    public void dispose() {
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
//        System.out.println("??");
        if (playButton == null) {
            assets.update(budget);
            this.progress = assets.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;

                playButton = internal.getEntry("play", Texture.class);
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
        if (playButton == null) {
            drawProgress(canvas);
        } else {
            Color tint = (pressState == 1 ? Color.GRAY : Color.WHITE);
            canvas.draw(
                    playButton,
                    tint,
                    playButton.getWidth() / 2,
                    playButton.getHeight() / 2,
                    centerX,
                    centerY,
                    0,
                    BUTTON_SCALE * scale,
                    BUTTON_SCALE * scale);
        }
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
                centerX - width / 2,
                centerY,
                scale * statusBkgLeft.getRegionWidth(),
                scale * statusBkgLeft.getRegionHeight());
        canvas.draw(
                statusBkgRight,
                Color.WHITE,
                centerX + width / 2 - scale * statusBkgRight.getRegionWidth(),
                centerY,
                scale * statusBkgRight.getRegionWidth(),
                scale * statusBkgRight.getRegionHeight());
        canvas.draw(
                statusBkgMiddle,
                Color.WHITE,
                centerX - width / 2 + scale * statusBkgLeft.getRegionWidth(),
                centerY,
                width - scale * (statusBkgRight.getRegionWidth() + statusBkgLeft.getRegionWidth()),
                scale * statusBkgMiddle.getRegionHeight());

        canvas.draw(
                statusFrgLeft,
                Color.WHITE,
                centerX - width / 2,
                centerY,
                scale * statusFrgLeft.getRegionWidth(),
                scale * statusFrgLeft.getRegionHeight());
        if (progress > 0) {
            float span =
                    progress
                            * (width - scale * (statusFrgLeft.getRegionWidth() + statusFrgRight.getRegionWidth()))
                            / 2.0f;
            canvas.draw(
                    statusFrgRight,
                    Color.WHITE,
                    centerX - width / 2 + scale * statusFrgLeft.getRegionWidth() + span,
                    centerY,
                    scale * statusFrgRight.getRegionWidth(),
                    scale * statusFrgRight.getRegionHeight());
            canvas.draw(
                    statusFrgMiddle,
                    Color.WHITE,
                    centerX - width / 2 + scale * statusFrgLeft.getRegionWidth(),
                    centerY,
                    span,
                    scale * statusFrgMiddle.getRegionHeight());
        } else {
            canvas.draw(
                    statusFrgRight,
                    Color.WHITE,
                    centerX - width / 2 + scale * statusFrgLeft.getRegionWidth(),
                    centerY,
                    scale * statusFrgRight.getRegionWidth(),
                    scale * statusFrgRight.getRegionHeight());
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

            // We are are ready, notify our listener
            if (isReady() && listener != null) {
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
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        this.width = (int) (BAR_WIDTH_RATIO * width);
        centerY = (int) (BAR_HEIGHT_RATIO * height);
        centerX = width / 2;
        heightY = height;
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

    /** Called when this screen becomes the current screen for a Game. */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /** Called when this screen is no longer the current screen for a Game. */
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

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * <p>This method checks to see if the play button is available and if the click is in the bounds
     * of the play button. If so, it signals the that the button has been pressed and is currently
     * down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playButton == null || pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        // TODO: Fix scaling
        // Play button is a circle.
        float radius = BUTTON_SCALE * scale * playButton.getWidth() / 2.0f;
        float dist =
                (screenX - centerX) * (screenX - centerX) + (screenY - centerY) * (screenY - centerY);
        if (dist < radius * radius) {
            pressState = 1;
        }
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * <p>This method checks to see if the play button is currently pressed down. If so, it signals
     * the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * <p>The buttonCode is controller specific. This listener only supports the start button on an
     * X-Box controller. This outcome of this method is identical to pressing (but not releasing) the
     * play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (pressState == 0) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart) {
                pressState = 1;
                return false;
            }
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * <p>The buttonCode is controller specific. This listener only supports the start button on an
     * X-Box controller. This outcome of this method is identical to releasing the the play button
     * after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (pressState == 1) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart) {
                pressState = 2;
                return false;
            }
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected(Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected(Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * <p>The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode The axis moved
     * @param value The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return true;
    }
}