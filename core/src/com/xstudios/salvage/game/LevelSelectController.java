package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.Controllers;
import com.xstudios.salvage.util.ScreenListener;
import com.xstudios.salvage.util.XBoxController;

import java.awt.*;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the e.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LevelSelectController implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the "assets

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
     * Background Texture
     */
    private Texture main_menu;
    private Texture background;
    private Texture level;
    private Texture line;

    /**
     * The width of the progress bar
     */
    private int width;
    /**
     * The y-coordinate of the center of the progress bar
     */
    private int centerY;
    /**
     * The x-coordinate of the center of the progress bar
     */
    private int centerX;
    /**
     * The height of the canvas window (necessary since sprite origin != screen origin)
     */
    private int heightY;
    /**
     * Scaling factor for when the student changes the resolution.
     */
    private float scale;

    /**
     * The current state of the play button
     */
    private int pressState;

    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    private CameraController camera;

    private int locked;

    public LevelSelectController() {
        pressState = 0;
        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game".
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }
    }

    public void setActive() {
        active = true;
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void setCameraController(CameraController cameraController, int w, int h) {
        this.camera = cameraController;
    }

    public void gatherAssets(AssetDirectory directory) {
        background = directory.getEntry("background:level_select", Texture.class);
        main_menu = directory.getEntry("main_menu", Texture.class);
        level = directory.getEntry("level", Texture.class);
        line = directory.getEntry("line", Texture.class);
    }

    public void setLocked(int level){
        locked = level;
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

    public boolean pointer1(int x, int y, int width, int height, float scale) {
        int pX = Gdx.input.getX();
        int pY = Gdx.input.getY();
        // Flip to match graphics coordinates
        y = canvas.getHeight() - y;
        float y1 = (float)y - (int)(360 - camera.getCameraPosition2D().y);
        float w = scale * width;
        float h = scale * height;

        if((x + w > pX && x - w < pX) && (y1 + h > pY && y1 - h < pY)){
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
        canvas.clear();
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, -1 * (background.getWidth()/2 + background.getWidth()/5), canvas.getWidth(), background.getWidth());

        Color tint = (pointer1(15,canvas.getHeight() * 7/8, main_menu.getWidth(),
                main_menu.getHeight(), 0.7f) ? Color.GRAY : Color.WHITE);
        canvas.draw(
                main_menu,
                tint,
                0,
                0,
                15,
                canvas.getHeight() * 7/8,
                0,
                0.7f,
                0.7f);
//        canvas.draw(
//                line,
//                Color.WHITE,
//                level.getWidth() / 2,
//                level.getHeight() / 2,
//                centerX/3 + centerX/4,
//                2 * centerY,
//                0,
//                1,
//                1);
//        canvas.draw(
//                line,
//                Color.WHITE,
//                level.getWidth() / 2,
//                level.getHeight() / 2,
//                centerX + centerX/2,
//                2 * centerY + centerY/3,
//                0,
//                -1,
//                1);
//
//        canvas.draw(
//                line,
//                Color.WHITE,
//                level.getWidth() / 2,
//                level.getHeight() / 2,
//                centerX/3 + centerX/4,
//                centerY,
//                0,
//                -1,
//                -2);
//        canvas.draw(
//                line,
//                Color.WHITE,
//                level.getWidth() / 2,
//                level.getHeight() / 2,
//                centerX + centerX/2,
//                2 * centerY + centerY/3,
//                0,
//                -1,
//                1);
//        canvas.draw(
//                line,
//                Color.WHITE,
//                level.getWidth() / 2,
//                level.getHeight() / 2,
//                centerX + centerX/2,
//                2 * centerY + centerY/3,
//                0,
//                -1,
//                1);

        //1
        tint = (pointer1(centerX/3, 2 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) || 1 > locked? Color.GRAY : Color.WHITE);
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
        //2
        tint = (pointer1(centerX,
                centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) || 2 > locked? Color.GRAY : Color.WHITE);
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
        //3
        tint = (pointer1(centerX + centerX/2 + centerX/6,
                2 * centerY + centerY/2, level.getWidth() / 2,
                level.getHeight() / 2, 1) || 3 > locked ? Color.GRAY : Color.WHITE);
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

        //second row stuff
        //6
        tint = (pointer1(centerX/3, - 3 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) || 6 > locked? Color.GRAY : Color.WHITE);
        canvas.draw(
                level,
                tint,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX/3,
                - 3 * centerY,
                0,
                1,
                1);

        //5
        tint = (pointer1(centerX,
                -2 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) || 5 > locked? Color.GRAY : Color.WHITE);
        canvas.draw(
                level,
                tint,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX,
                -2 * centerY,
                0,
                1,
                1);

        //4
        tint = (pointer1(centerX + centerX/2 + centerX/6,
                -1 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) || 4 > locked ? Color.GRAY : Color.WHITE);
        canvas.draw(
                level,
                tint,
                level.getWidth() / 2,
                level.getHeight() / 2,
                centerX + centerX/2 + centerX/6,
                -1 * centerY,
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
            camera.render();
            System.out.println("pressState= " + pressState);
            // We are ready, notify our listener
            if (pressState == 1 && listener != null) {
                camera.setCameraPosition(640, 360);
                camera.render();
                listener.exitScreen(this, 0);
            }

            if (pressState >= 2  && listener != null) {
                listener.exitScreen(this, pressState - 1);
            }
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
        if (pressState >= 1) {
            return true;
        }
        if (pointer1(15,canvas.getHeight() * 7/8, main_menu.getWidth(),
                main_menu.getHeight(), 0.7f)) {
            pressState = 1;
        }
        //1
        else if (pointer1(centerX/3, 2 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) && 1 <= locked) {
            pressState = 2;
        }
        //2
        else if (pointer1(centerX,
                centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) && 2 <= locked) {
            pressState = 3;
        }

        //3
        else if (pointer1(centerX + centerX/2 + centerX/6,
                2 * centerY + centerY/2, level.getWidth() / 2,
                level.getHeight() / 2, 1) && 3 <= locked) {
            pressState = 4;
        }

        //4
        else if (pointer1(centerX + centerX/2 + centerX/6,
                -1 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) && 4 <= locked) {
            pressState = 5;
        }
        //5
        else if (pointer1(centerX,
                -2 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) && 5 <= locked) {
            pressState = 6;
        }
        //6
        else if (pointer1(centerX/3, - 3 * centerY, level.getWidth() / 2,
                level.getHeight() / 2, 1) && 6 <= locked) {
            pressState = 7;
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
//        if (pressState >= 1) {
//            System.out.println("touchup");
//            pressState = pressState + 3;
//            return false;
//        }
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
        float y = camera.getCameraPosition2D().y;
        if((y + dy * 40.0f  > canvas.getHeight()/2  && dy > 0) || (y + dy * 40.0f < (-1 * (background.getHeight()/(2.8)))  && dy < 0)) {
            camera.setCameraPosition(640, camera.getCameraPosition2D().y);
        }
        else
        camera.setCameraPosition(640, camera.getCameraPosition2D().y + dy * 40.0f);
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