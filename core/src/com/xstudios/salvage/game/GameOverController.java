package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

public class GameOverController implements Screen, InputProcessor {

    private ScreenListener listener;

    private boolean active;

    protected GameCanvas canvas;

    protected TextureRegion background;
    protected Texture main_menu;
    protected Texture try_again_next;
    protected Texture title;

    private boolean restart_game;
    private boolean exit_home;
    private boolean next_level;
    private boolean display_win;

    private int index;

    private int width;
    private int height;

    private static int STANDARD_WIDTH = 1280;

    private static int STANDARD_HEIGHT = 720;

    private float scale;

    private CameraController camera;

    public GameOverController() {
        active = false;
        restart_game = false;
        exit_home = false;
        next_level = false;

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
    }

    public void setCameraController(CameraController cameraController) {
        this.camera = cameraController;
        camera.setCameraPosition(width/2, height/2);
        camera.setBounds(width/2, height/2, width, height);
        camera.render();
    }

    @Override
    public void dispose() {
        exit_home = false;
        restart_game = false;
        next_level = false;
    }

    public void render() {
        if(active) {
//            if (restart_game && listener != null)
//                listener.exitScreen(this, 0);
//
//            if(exit_home && listener != null)
//                listener.exitScreen(this, 1);
//
//            if(next_level && listener != null)
//                listener.exitScreen(this, 2);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        active = true;
    }

    @Override
    public void render(float delta) {
        draw(delta);
        render();
    }

    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        this.width = width;
        this.height = height;
        camera.resize(this.width, this.height);
        camera.getCamera().setToOrtho(false, width, height);
        camera.getCamera().update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
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
    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    private boolean help_draw(Texture t, int x, int y, boolean tint){
        int ox = t.getWidth()/2;
        int oy = t.getHeight()/2;
        Color c = Color.WHITE;
        boolean clicked = false;
        if(tint){
            int pX = Gdx.input.getX();
            int pY = Gdx.input.getY();
            // Flip to match graphics coordinates
            int flip_y = canvas.getHeight() - y;
            float w = scale * ox;
            float h = scale * oy;

            if((x + w > pX && x - w < pX) && (flip_y + h > pY && flip_y - h < pY)){
                c = Color.GRAY;
                if(Gdx.input.isTouched()) clicked = true;
            }
        }
        canvas.draw(t, c, ox, oy, x, y, 0, scale, scale);
        return clicked;
    }

    public void draw(float dt) {
        canvas.clear();

        canvas.begin();

        index = 5;
        canvas.draw(background, Color.WHITE,0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        help_draw(title, width/2, height - height/7, false);
        exit_home = help_draw(main_menu, width/6, height/9, true);

        boolean which = help_draw(try_again_next, width - width/6, height/9, true);
        if(display_win)
            next_level = which;
        else
            restart_game = which;

        canvas.end();
    }

    public void gatherAssets(AssetDirectory directory) {
        main_menu = directory.getEntry("main_menu", Texture.class);
        if(display_win) {
            try_again_next = directory.getEntry("next", Texture.class);
            background = new TextureRegion(directory.getEntry("level_complete", Texture.class));
            title = directory.getEntry("complete", Texture.class);
        }else{
            try_again_next = directory.getEntry("try_again", Texture.class);
            background =  new TextureRegion(directory.getEntry( "game_over", Texture.class ));
            title = directory.getEntry("perish", Texture.class);
        }
    }

    public void setWin(boolean w) {
        display_win = w;
    }

    public boolean getWin(){ return display_win;}

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
        System.out.println("touchup in gameover");
        System.out.println("restart game: " + restart_game);
        if (restart_game)
            listener.exitScreen(this, 0);

        else if(exit_home)
            listener.exitScreen(this, 1);

        else if(next_level)
            listener.exitScreen(this, 2);

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
    public boolean buttonDown (Controller controller, int buttonCode) {return true;}

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
    public boolean buttonUp (Controller controller, int buttonCode) {return true;}

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
    public boolean scrolled(float dx, float dy) { return true;}

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
