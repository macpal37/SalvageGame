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
public class SettingsController extends ScreenController implements ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /** Background texture for start-up */
    private Texture background;

    /** Background Texture */
    protected Texture menu;
    protected Texture reset;
    protected Texture settings;
    protected Texture tentacles;
    protected Texture music;
    protected Texture sound_effects;

    protected Texture confirmation;
    protected Texture yes;
    protected Texture no;

    protected Texture line;
    protected Texture box;

    private boolean press_menu;
    private boolean press_reset;
    private boolean confirmation_screen;
    private boolean press_yes;
    private boolean press_no;

    private boolean music_box;
    private boolean sound_effects_box;

    private int music_volume;
    private int sound_effects_volume;

    private int tick1;
    private int tick2;

    private int segment;

    Player player;

    public SettingsController() {


        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        press_menu = false;
        press_reset = false;
        confirmation_screen = false;
        press_yes = false;
        press_no = false;

        music_box = false;
        sound_effects_box = false;

        tick1 = 2;
        tick2 = 2;
    }

    //sets Player
    public void setPlayer(Player player){
        this.player = player;
        music_volume = player.getMusic();
        tick1 = music_volume;
        sound_effects_volume = player.getSoundEffects();
        tick2 = sound_effects_volume;
    }

    //sets CameraController
    public void setCameraController(CameraController cameraController) {
        this.camera = cameraController;
        camera.setCameraPosition(width/2, height/2);
        camera.setBounds(width/2, height/2, width, height);
        camera.render();
    }

    public void gatherAssets(AssetDirectory directory) {
        background =  directory.getEntry( "background:settings", Texture.class );
        settings = directory.getEntry("settings", Texture.class);
        menu = directory.getEntry("menu", Texture.class);
        music = directory.getEntry("music", Texture.class);
        sound_effects = directory.getEntry("sound_effects", Texture.class);
        reset = directory.getEntry("reset", Texture.class);
        tentacles = directory.getEntry("screen_tentacles", Texture.class);
        line = directory.getEntry("bar", Texture.class);
        box = directory.getEntry("slider", Texture.class);
        confirmation = directory.getEntry("confirmation", Texture.class);
        yes = directory.getEntry("yes", Texture.class);
        no = directory.getEntry("no", Texture.class);
    }

    //dispose
    public void dispose(){
        background = null;
        active = false;
        menu = null;
        settings = null;
        music = null;
        sound_effects = null;
        reset = null;
        line = null;
        box = null;
        confirmation = null;
        yes = null;
        no = null;

        press_menu = false;
        press_reset= false;
        confirmation_screen = false;
        segment = 0;
    }

    //helps in the draw function
    private boolean help_draw(Texture t, int x, int y, boolean tint){
        Color c = Color.WHITE;
        boolean clicked = false;

        //if tint is true, then the image can be changed color(interactive)
        if(tint){
            //mouse position
            int pX = Gdx.input.getX();
            int pY = Gdx.input.getY();
            // Flip to match graphics coordinates
            int flip_y = canvas.getHeight() - y;
            float w = scale * t.getWidth();
            float h = scale * t.getHeight();

            //if true, then image tint is gray
            if((x + w >= pX && x <= pX) && (flip_y >= pY && flip_y - h <= pY)){
                c = Color.GRAY;
                //if true(touched), then clicked is true
                if(Gdx.input.isTouched()) clicked = true;
            }
        }
        //draws
        canvas.draw(t, c, 0, 0, x, y, 0, scale, scale);
        return clicked;
    }

    private void draw() {
        canvas.begin();
   
        canvas.draw(background, Color.WHITE, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        canvas.draw(tentacles, Color.WHITE, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        press_menu = help_draw(menu, width/30, height - height/12, true);

        help_draw(settings, width/14, height - height/4 - height/20, false);

        help_draw(music, width/14, height - height/4 - height/20 - height/10, false);

        help_draw(line, width/14, height - height/2, false);

        if(!music_box) {
            music_box = help_draw(box, width/14 + segment * tick1, height - height / 2 - height / 24, true);
        }
        else help_draw(box, width / 14 + segment * tick1, height - height / 2 - height / 24, true);

        help_draw(sound_effects, width/14, height/2 - height/7, false);

        help_draw(line, width/14, height/2 - height/5 - height/20, false);

        if(!sound_effects_box) {
            sound_effects_box = help_draw(box, width/14 + segment * tick2, height / 2 - height / 5 - height / 11, true);
        }
        else help_draw(box, width/14 + segment * tick2, height / 2 - height / 5 - height / 11, true);

        press_reset = help_draw(reset, width/14, height/7 - height/20, true);

        if(confirmation_screen) {
            canvas.draw(confirmation, Color.WHITE, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            press_yes = help_draw(yes, width - width/3 - width/20 - width/40 - width/40, height/2 - height/6+ height/80, true);
            press_no = help_draw(no, width/3 + width/20, height/2 - height/6, true);
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
        segment = (int)((line.getWidth() - box.getWidth()/2) * scale)/4;
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
        if(press_reset){
            confirmation_screen = true;
        }
        else if(confirmation_screen){
            if(press_yes) {
                player.setLevel(1);
                tick1 = 2;
                tick2 = 2;
                press_yes = false;
                confirmation_screen = false;
            }
            else if(press_no) {
                press_no = false;
                confirmation_screen = false;
            }
        }
        else if(music_box) music_box = false;
        else if(sound_effects_box) sound_effects_box = false;
        else if(press_menu) {
            listener.exitScreen(this, 0);
        }

        player.setMusic(tick1);
        player.setSoundEffects(tick2);
        player.save();

        AudioController.getInstance().setMusic((float)tick1);
        AudioController.getInstance().set_sound_effect_volume((float)tick2);

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
        if(!confirmation_screen) {
            int total = segment * 5;
            int max = screenX - width / 14;
            int start = width / 14;
            int ticks = max / segment;
            if (music_box) {
                if (max >= start && max <= total)
                    tick1 = (ticks <= 4) ? ticks : 4;
            }
            AudioController.getInstance().setMusic((float) tick1);
            if (sound_effects_box) {
                if (max >= start && max <= total)
                    tick2 = (ticks <= 4) ? ticks : 4;
            }
            AudioController.getInstance().set_sound_effect_volume((float) tick2);
        }

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