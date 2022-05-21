package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
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


    private CircleShape circ;


    public DecorModel(float x, float y) {
        super(x, y);

        circ = new CircleShape();
        circ.setRadius(0.0625f);

    }

    public void setStartingFrame(int f) {
        startingFrame = f;
    }

    private int startingFrame = 0;

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

    public Vector2 getScale() {
        return scale;
    }

    public Vector2 scale = new Vector2(1, 1);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }


    @Override
    public void draw(GameCanvas canvas) {

        if (tick % 10 == 0) {
            int frame = spriteSheet.getFrame();
            frame++;
            if (frame >= spriteSheet.getSize() && tick % ((int) animSleep) == 0)
                frame = 0;
            if (frame < spriteSheet.getSize())
                spriteSheet.setFrame(frame);
        }
        canvas.draw(spriteSheet, Color.WHITE, origin.x, origin.y, getX() * drawScale.x - origin.x,
                getY() * drawScale.y - origin.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y);

    }

    @Override
    protected void resize(float width, float height) {

    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(circ, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }

    float animSleep = 1;

    public void setAnimSleep(float v) {
        animSleep = v;
    }
}

