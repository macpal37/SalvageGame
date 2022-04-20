package com.xstudios.salvage.game.models;

import com.badlogic.gdx.math.Vector2;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

public class DecorModel extends GameObject {


    private FilmStrip spriteSheet;

    enum DecorType {
        Dust, Plant

    }


    private int startingFrame = 0;

    public DecorModel(float x, float y) {
        super(x, y);
    }

    public void setStartingFrame(int f) {
        startingFrame = f;
    }

    public int getFrame() {
        return spriteSheet.getFrame();

    }


    public void setFilmStrip(FilmStrip value) {
        spriteSheet = value;
        spriteSheet.setFrame(startingFrame);
    }

    @Override
    public boolean activatePhysics(World world) {
        return false;
    }

    @Override
    public void deactivatePhysics(World world) {

    }

    @Override
    protected void createFixtures() {

    }

    @Override
    protected void releaseFixtures() {

    }

    public Vector2 scale = new Vector2(1, 1);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }


    int tick = 0;

    @Override
    public void draw(GameCanvas canvas) {
        tick++;
        if (tick % 5 == 0) {
            int frame = spriteSheet.getFrame();
            frame++;
            if (frame >= spriteSheet.getSize())
                frame = 0;
            spriteSheet.setFrame(frame);
        }
        canvas.draw(spriteSheet, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), scale.x, scale.y);

    }

    @Override
    protected void resize(float width, float height) {

    }

    @Override
    public void drawDebug(GameCanvas canvas) {

    }
}

