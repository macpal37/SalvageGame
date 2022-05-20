package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.audio.AudioController;

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
public class RulesController extends ScreenController implements ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /** Background texture for start-up */
    /** Background Texture */
    protected Texture one;
    protected Texture two;

    protected Texture menu;
    protected Texture active_right;
    protected Texture active_left;
    protected Texture inactive_right;
    protected Texture inactive_left;

    private boolean press_menu;
    private boolean press_left;
    private boolean press_right;

    private boolean left;
    private boolean right;

    public RulesController() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        press_menu = false;
        press_left = true;
        press_right = false;

        right = false;
        left = true;
    }

    //sets CameraController
    public void setCameraController(CameraController cameraController) {
        this.camera = cameraController;
        camera.setCameraPosition(width/2, height/2);
        camera.setBounds(width/2, height/2, width, height);
        camera.render();
    }

    public void gatherAssets(AssetDirectory directory) {
        one = directory.getEntry("one", Texture.class);
        two = directory.getEntry("two", Texture.class);

        menu = directory.getEntry("rules_menu", Texture.class);

        active_right = directory.getEntry("active_right", Texture.class);
        active_left = directory.getEntry("active_left", Texture.class);

        inactive_right = directory.getEntry("inactive_right", Texture.class);
        inactive_left = directory.getEntry("inactive_left", Texture.class);
    }

    //dispose
    public void dispose(){
        one = null;
        two = null;
        menu = null;
        active_right = null;
        active_left = null;
        inactive_right = null;
        inactive_left = null;

        press_menu = false;
        press_left = true;
        press_right = false;
        active = false;
    }

    private boolean help_draw(Texture t, int x, int y, boolean tint){
        int ox = t.getWidth()/2;
        int oy = t.getHeight()/2;
        Color c = Color.WHITE;
        boolean clicked = false;
        if(tint) {
            int pX = Gdx.input.getX();
            int pY = Gdx.input.getY();
            // Flip to match graphics coordinates
            int flip_y = canvas.getHeight() - y;
            float w = scale * ox;
            float h = scale * oy;

            if ((x + w > pX && x - w < pX) && (flip_y + h > pY && flip_y - h < pY)) {
                c = Color.GRAY;
                if (Gdx.input.isTouched()) clicked = true;
            }
        }
        canvas.draw(t, c, ox, oy, x, y, 0, scale, scale);
        return clicked;
    }

    private void draw() {
        canvas.begin();
        if(press_left) {
            canvas.draw(one, Color.WHITE, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            help_draw(inactive_left, canvas.getWidth()/14, canvas.getHeight()/2, false);
            right = help_draw(active_right, canvas.getWidth() - canvas.getWidth()/14, canvas.getHeight()/2, true);
            left = !right;
        }
        else{
            canvas.draw(two, Color.WHITE, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            help_draw(inactive_right, canvas.getWidth() - canvas.getWidth()/14, canvas.getHeight()/2, false);
            left = help_draw(active_left, canvas.getWidth()/14, canvas.getHeight()/2, true);
            System.out.println("we are in the image2");
            right = !left;
        }
        press_menu = help_draw(menu, width/12, height - height/10, true);
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
        super.resize(width, height);
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
        return true;
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
        if(press_menu) {
            listener.exitScreen(this, 0);
        }
        else if(right){
            press_right = true;
            press_left = false;
            System.out.println("we are in the right of touchup");
        }
        else if(left) {
            press_left = true;
            press_right = false;
            System.out.println("we are in the left of touchup");
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
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
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
     * @param screenX the x-coordinate of the mouse on the
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