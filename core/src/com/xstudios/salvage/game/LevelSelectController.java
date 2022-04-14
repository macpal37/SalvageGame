package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.Controllers;
import com.xstudios.salvage.util.ScreenListener;
import com.xstudios.salvage.util.XBoxController;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LevelSelectController implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /** Background texture for start-up */
    /** Play button to display when done */
    /** Texture atlas to support a progress bar */

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Height of the progress bar */
    private static float BUTTON_SCALE  = 0.75f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Background Texture */
    private Texture main_menu;
    private Texture background;
    private Texture level;
    private Texture line;

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

    /** The current state of the play button */
    private int pressState;


    /** Whether or not this player mode is still active */
    private boolean active;

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */

    public LevelSelectController() {
        // Load the next two images immediately.

        pressState = 0;

        Gdx.input.setInputProcessor(this);

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener( this );
        }
    }

    public void setActive(){
        active = true;
    }

    public void setCanvas(GameCanvas canvas){
        this.canvas = canvas;
    }

    public void gatherAssets(AssetDirectory directory) {
        background =  directory.getEntry( "background:level_select", Texture.class );
        main_menu = directory.getEntry("main_menu", Texture.class);
        level = directory.getEntry("level", Texture.class);
        line = directory.getEntry("line", Texture.class);
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        background = null;
        main_menu = null;
        pressState = 0;
        level = null;
        line = null;
    }

    public boolean pointer(int x, int y, int width, int height){
        int pX = Gdx.input.getX();
        int pY = Gdx.input.getY();
        // Flip to match graphics coordinates
        pY = canvas.getHeight() - pY;

        float widthR = BUTTON_SCALE * scale * width/ 2.0f;
        float heightR = BUTTON_SCALE * scale * height/ 2.0f;
        float dist =
                (pX - x) * (pX - x) + (pY - y) * (pY - y);
        if (dist < widthR * heightR) {
            return true;
        }
        return false;
    }
    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background, 0, 0);
        Color tint = (pointer(centerX - centerX/2 - centerX/5, 3 * centerY + centerY/2 + centerY/5, main_menu.getWidth() / 2,
                main_menu.getHeight() / 2) ? Color.GRAY : Color.WHITE);
        canvas.draw(
                main_menu,
                tint,
                main_menu.getWidth() / 2,
                main_menu.getHeight() / 2,
                centerX - centerX/2 - centerX/5,
                3 * centerY + centerY/2 + centerY/5,
                0,
                BUTTON_SCALE * scale,
                BUTTON_SCALE * scale);
        canvas.draw(
                line,
                Color.WHITE,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX/3 + centerX/4,
                2 * centerY,
                0,
                1,
                1);
        canvas.draw(
                line,
                Color.WHITE,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX + centerX/2,
                2 * centerY + centerY/3,
                0,
                -1,
                1);
        tint = (pointer(centerX/3,
                2 * centerY, level.getWidth() / 2,
                level.getHeight() / 2) ? Color.GRAY : Color.WHITE);
        canvas.draw(
                level,
                tint,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX/3,
                2 * centerY,
                0,
                1,
                1);
        tint = (pointer(centerX,
                centerY, level.getWidth() / 2,
                level.getHeight() / 2) ? Color.GRAY : Color.WHITE);
        canvas.draw(
                level,
                tint,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX,
                centerY,
                0,
                1,
                1);
        tint = (pointer(centerX + centerX/2 + centerX/6,
                2 * centerY + centerY/2, level.getWidth() / 2,
                level.getHeight() / 2) ? Color.GRAY : Color.WHITE);
        canvas.draw(
                level,
                tint,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX + centerX/2 + centerX/6,
                2 * centerY + centerY/2,
                0,
                1,
                1);

        canvas.end();
    }


    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            draw();

            // We are are ready, notify our listener
            if (pressState == 4 && listener != null) {
                listener.exitScreen(this, 0);
            }
            if (pressState == 5 && listener != null) {
                listener.exitScreen(this, 1);
            }
//            if (pressState == 6 && listener != null) {
//                listener.exitScreen(this, 2);
//            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
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
        scale = (sx < sy ? sx : sy);

        this.width = (int)(BAR_WIDTH_RATIO*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        Gdx.input.setInputProcessor(this);
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
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressState >= 4) {
            return true;
        }
        // Flip to match graphics coordinates
        screenY = heightY - screenY;

        float widthR = BUTTON_SCALE * scale * main_menu.getWidth() / 2.0f;
        float heightR = BUTTON_SCALE * scale * main_menu.getHeight() / 2.0f;
        float dist =
                (screenX - (centerX - centerX/2 - centerX/5)) * (screenX - (centerX - centerX/2 - centerX/5)) +
                        (screenY - (3 * centerY + centerY/2 + centerY/5)) * (screenY - (3 * centerY + centerY/2 + centerY/5));
        if (dist < widthR * heightR) {
            pressState = 1;
        }
        widthR = BUTTON_SCALE * scale * level.getWidth() / 2.0f;
        heightR = BUTTON_SCALE * scale * level.getHeight() / 2.0f;
        dist = (screenX - centerX/3) * (screenX - centerX/3) +
                        (screenY - 2 * centerY) * (screenY - 2 * centerY);
        if (dist < widthR * heightR) {
            pressState = 2;
        }

        return false;
    }


    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState >= 1) {
            pressState = pressState + 3;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (pressState == 0) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                pressState = 1;
                return false;
            }
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (pressState == 1) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
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
     *
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
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

}