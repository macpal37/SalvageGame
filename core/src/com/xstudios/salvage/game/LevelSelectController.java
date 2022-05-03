package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;
import java.util.ArrayList;

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

    private static int STANDARD_WIDTH = 1280;

    private static int STANDARD_HEIGHT = 720;

    private static float BUTTON_SCALE = 0.75f;

    private GameCanvas canvas;

    private ScreenListener listener;

    private Texture main_menu;
    private Texture background;
    private Texture level;
    private Texture line;
    private Texture lock;

    private int width;
    private int height;
    private float scale;

    private int level_clicked;
    private int press_main_menu;
    private ArrayList<Texture> level_list;

    private boolean active;

    private CameraController camera;

    private int locked;

    private int total_levels;

    public LevelSelectController() {
        level_list = new ArrayList<>();
        level_clicked = 0;
        press_main_menu = 0;

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

    public void setActive() {
        active = true;
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void setCameraController(CameraController cameraController, int w, int h) {
        this.camera = cameraController;
        camera.setCameraPosition(width/2, height/2);
        camera.setBounds(width/2, height/2, width, height);
        camera.render();
    }

    public void gatherAssets(AssetDirectory directory) {
        background = directory.getEntry("background:level_select", Texture.class);
        main_menu = directory.getEntry("main_menu", Texture.class);
        level = directory.getEntry("level", Texture.class);
        line = directory.getEntry("line", Texture.class);
        lock = directory.getEntry("lock", Texture.class);
        for(int i = 1; i < 13; i++){
            level_list.add(directory.getEntry(Integer.toString(i), Texture.class));
        }
    }

    public void setLocked(int level){
        locked = level;
    }

    public void setTotalLevels(int level){
        total_levels = level;
    }

    public void dispose() {
        background = null;
        main_menu = null;
        level = null;
        line = null;
        level_clicked = 0;
        press_main_menu = 0;
    }

    private void help_draw_line(int x, int y, int level, float angle){
        help_draw(line, x, y, false, level, null, angle, true);
    }

    private boolean help_draw_level(int x, int y, int l){
        return help_draw(level, x, y, true, l, level_list.get(l - 1), 0, false);
    }

    private boolean help_draw(Texture t, int x, int y, boolean tint, int level, Texture t1, float angle, boolean line){
        int ox = t.getWidth()/2;
        int oy = t.getHeight()/2;
        Color c = Color.WHITE;
        boolean clicked = false;
        if(line){
            if(level > locked) return false;
            else {
                canvas.draw(t, c, ox, oy, x, height - y, angle, scale, scale);
                return true;
            }
        }
        if(tint){
            int pX = Gdx.input.getX();
            int pY = Gdx.input.getY();
            float flip_y = y - (int)(height/2 - camera.getCameraPosition2D().y);
            float w = scale * ox;
            float h = scale * oy;

            if(level != 0){
                if(level > locked) {
                    c = Color.GRAY;
                    canvas.draw(t, c, ox, oy, x, height - y, 0, scale, scale);
                    return false;
                }
                else{
                    if(t1 != null)
                        t = t1;
                }
            }
            if((x + w > pX && x - w < pX) && (flip_y + h > pY && flip_y - h < pY)){
                c = Color.GRAY;
                if(Gdx.input.isTouched()) clicked = true;
            }
        }
        canvas.draw(t, c, ox, oy, x, height - y, angle, scale, scale);
        return clicked;
    }

    private void draw() {
        canvas.clear();
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, -1 * height * 2, width,  height * 3);

        //menu
        press_main_menu = help_draw(main_menu, width/7,height/7, true, 0, null, 0, false) ? 1 : 0;

        //lines
        help_draw_line(width/3, height/2, 2, 0.8f);
        help_draw_line(width - width/3, height/3, 3, 0.2f);
        help_draw_line(width - width/7, height/2, 4, -1.5f);
        help_draw_line(width - width/3, height - height/5, 5, 0);
        help_draw_line(width/3, height - height/6, 6, 1f);
        help_draw_line(width/5, height + height/4, 7, -1f);
        help_draw_line(width/3, height + height/3, 8, 1f);
        help_draw_line(width - width/3, height + height/4, 9, 0);
        help_draw_line(width - width/4, height + height/2, 10, 1f);

        //levels
        Boolean[] levels = {
                help_draw_level(width/6, height/2, 1),
                help_draw_level(width/2, height/4, 2),
                help_draw_level(width - width/6, height/4, 3),
                help_draw_level(width - width/5, height - height/4, 4),
                help_draw_level(width/2, height - height/3, 5),
                help_draw_level(width/5, height, 6),
                help_draw_level(width/4, 2 * height - height/2, 7),
                help_draw_level(width/2, height + height/6, 8),
                help_draw_level(width - width/5, height + height/4, 9),
                help_draw_level(width - width/3, 2 * height - height/3, 10)};

        //clicked level
        for(int i = 0; i < levels.length; i++){
            if(levels[i])
                level_clicked = i + 1;
        }

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
//            if (press_main_menu) {
//                camera.setCameraPosition(width/2, height/2);
//                camera.render();
//                listener.exitScreen(this, 0);
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
        scale = BUTTON_SCALE * (sx < sy ? sx : sy);
        this.width = width;
        this.height = height;
        camera.resize(this.width, this.height);
        camera.render();
    }

    public void pause() {
        // TODO Auto-generated method stub

    }
    public void resume() {
        // TODO Auto-generated method stub

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
    int num;
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(press_main_menu == 1){
            listener.exitScreen(this, 0);
        }
        if (level_clicked >= 1) {
            listener.exitScreen(this, level_clicked);
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
        if((y + dy * 40.0f  > height/2  && dy > 0) || (y + dy * 40.0f < (-1 * height))  && dy < 0) {
            camera.setCameraPosition(width/2, camera.getCameraPosition2D().y);
        }
        else
            camera.setCameraPosition(width/2, camera.getCameraPosition2D().y + dy * 40.0f);
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