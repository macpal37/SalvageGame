package com.xstudios.salvage.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

public class GameOverController implements Screen, ApplicationListener, InputProcessor {
    private Skin skin;
    private Stage stage;
    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;
    /**
     * Whether or not this is an active controller
     */
    private boolean active;

    /**
     * The actual assets to be loaded
     */
    private AssetDirectory assets;
    /**
     * Reference to the game canvas
     */
    protected GameCanvas canvas;

    /**
     * The boundary of the world
     */
    protected Rectangle bounds;
    /**
     * The world scale
     */
    protected Vector2 scale;
    /**
     * Background Texture
     */
    protected TextureRegion background;
    protected Texture main_menu;
    protected Texture try_again_next;
    protected Texture title;
    /**
     * The font for giving messages to the player
     */
    public static BitmapFont displayFont;

    private boolean restart_game;
    private boolean exit_home;
    private boolean next_level;

    private boolean display_win;

    private float x_pos_text;
    private float y_pos_text;

    public GameOverController(Rectangle bounds) {
        active = false;
        this.bounds = bounds;
        this.scale = new Vector2(1, 1);
        restart_game = false;
        exit_home = false;
        next_level = false;
    }

    @Override
    public void create() {

        TextureRegion imageTR = new TextureRegion(try_again_next);
        TextureRegionDrawable imageTRD = new TextureRegionDrawable(imageTR);
        ImageButton button = new ImageButton(imageTRD);
        stage = new Stage();

        button.setWidth(try_again_next.getWidth());
        button.setHeight(try_again_next.getHeight());
        button.setPosition(87, 66);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(display_win) next_level = true;
                else restart_game = true;
            }
        });
        stage.addActor(button);

        TextureRegion imageTR1 = new TextureRegion(main_menu);
        TextureRegionDrawable imageTRD1 = new TextureRegionDrawable(imageTR1);
        ImageButton button1 = new ImageButton(imageTRD1);

        button1.setWidth(main_menu.getWidth());
        button1.setHeight(main_menu.getHeight());
        button1.setPosition(780, 66);

        button1.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                exit_home = true;
            }
        });
        stage.addActor(button1);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        exit_home = false;
        restart_game = false;
        next_level = false;
    }

    @Override
    public void render() {
        if (active) {
            canvas.begin();
            stage.draw();

            Color tint = pointer1(87, 66, try_again_next.getWidth() , try_again_next.getHeight()) ? Color.GRAY : Color.WHITE;
            canvas.draw(try_again_next, tint, 0, 0, 87, 66, 0, 1, 1);

            tint = pointer1(780, 66, main_menu.getWidth(), main_menu.getHeight()) ? Color.GRAY : Color.WHITE;
            canvas.draw(main_menu, tint, 0, 0,780,66, 0, 1, 1);

            canvas.end();
            // We are are ready, notify our listener
            if (restart_game && listener != null) {
                listener.exitScreen(this, 0);
            }

            if(exit_home && listener != null){
                listener.exitScreen(this, 1);
            }

            if(next_level && listener != null){
                listener.exitScreen(this, 2);
            }
            // can do this with different exit codes to indicate which screen to switch to
        }
    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void render(float delta) {
        draw(delta);
        render();
    }

    @Override
    public void resize(int width, int height) {
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
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth() / bounds.getWidth();
        this.scale.y = canvas.getHeight() / bounds.getHeight();
        x_pos_text = canvas.getWidth() / 2f;
        y_pos_text = canvas.getHeight() / 2f;
    }
    public boolean pointer1(int x, int y, int w, int h) {
        int pX = Gdx.input.getX();
        int pY = Gdx.input.getY();
        // Flip to match graphics coordinates
        y = canvas.getHeight() - y;

        if((x + w > pX && x < pX) && (y > pY && y - h < pY)){
            return true;
        }
        return false;
    }

    public boolean pointer() {
        int pX = Gdx.input.getX();
        int pY = Gdx.input.getY();
        //System.out.println("x: " + pX + " y: " + (Gdx.graphics.getHeight() - pY));
        return false;
    }


    public void draw(float dt) {
        canvas.clear();

        canvas.begin();
        pointer();
        canvas.draw(background, Color.WHITE,0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.draw(title, Color.WHITE, title.getWidth()/2, title.getHeight()/2, Gdx.graphics.getWidth()/2,
                625, 0,1, 1);
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
