package com.xstudios.salvage.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

public class GameOverController implements Screen, ApplicationListener {
    private Skin skin;
    private Stage stage;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Whether or not this is an active controller */
    private boolean active;

    /** The actual assets to be loaded */
    private AssetDirectory assets;
    /** Reference to the game canvas */
    protected GameCanvas canvas;

    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** Background Texture */
    protected TextureRegion background;
    /** The font for giving messages to the player */
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
        this.scale = new Vector2(1,1);
        restart_game = false;
        exit_home = false;
        next_level = false;
    }

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        stage = new Stage();

        final TextButton button = new TextButton("Restart", skin, "default");

        button.setWidth(200f);
        button.setHeight(20f);
        button.setPosition(Gdx.graphics.getWidth() /2 - 100f, Gdx.graphics.getHeight()/2 - 10f);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                restart_game = true;
            }
        });

        stage.addActor(button);

        final TextButton game_over = new TextButton("Return to Home", skin, "default");

        game_over.setWidth(200f);
        game_over.setHeight(20f);
        game_over.setPosition(Gdx.graphics.getWidth() /2 - 100f, Gdx.graphics.getHeight()/2 - 30f);

        game_over.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                exit_home = true;
            }
        });

        stage.addActor(game_over);

        final TextButton next = new TextButton("Go to next level", skin, "default");

        next.setWidth(200f);
        next.setHeight(20f);
        next.setPosition(Gdx.graphics.getWidth() /2 - 100f, Gdx.graphics.getHeight()/2 - 50f);

        next.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                next_level = true;
            }
        });

        stage.addActor(next);

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
        if(active) {
            canvas.begin();
            stage.draw();
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
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
        x_pos_text = canvas.getWidth()/2f;
        y_pos_text = canvas.getHeight()/2f;
    }

    public void draw(float dt) {
        canvas.clear();

        canvas.begin();
        canvas.draw(background, Color.DARK_GRAY,background.getRegionWidth()/2f,background.getRegionHeight()/2f,0,0,0,4,4);

        // draw game objects
         if(display_win) {
             canvas.drawText(
                     "you win",
                     displayFont, x_pos_text-50,y_pos_text+150);
         } else {
            canvas.drawText(
                    "you lose",
                    displayFont, x_pos_text-50,y_pos_text+150);
        }
        canvas.end();
    }

    public void gatherAssets(AssetDirectory directory) {
        displayFont = directory.getEntry("fonts:lightpixel", BitmapFont.class);

        background =  new TextureRegion(directory.getEntry( "background:game_over", Texture.class ));
    }

    public void setWin(boolean w) {
        display_win = w;
    }

    public void setTextPos(float x, float y) {
        x_pos_text = x;
        y_pos_text = y;
    }

}
