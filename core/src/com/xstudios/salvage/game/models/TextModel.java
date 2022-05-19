package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.xstudios.salvage.game.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

public class TextModel extends GameObject {


    private FilmStrip spriteSheet;


    private CircleShape circ;

    private String display_text;

    private boolean isDisplay = false;

    /**
     * A cache value for the fixture (for resizing)
     */
    protected Fixture geometry;

    public TextModel(float x, float y, String s) {
        super(x, y);

        display_text = s;
        circ = new CircleShape();
        circ.setRadius(0.0625f);

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
        if (body == null) {
            return;
        }

        releaseFixtures();
        fixture.filter.maskBits = -1;
        fixture.shape = circ;

        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    @Override
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }

    public Vector2 getScale() {
        return scale;
    }

    public Vector2 scale = new Vector2(1, 1);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }


    public void setDisplay(boolean s) {
        isDisplay = s;
    }

    @Override
    public void draw(GameCanvas canvas) {
        if(isDisplay) {
            canvas.drawText(display_text, GameController.displayFont, (getX() - getWidth() / 3 * 2) * drawScale.x,
                    (getY() + getHeight() / 3 * 2) * drawScale.y);
        }
    }

    @Override
    protected void resize(float width, float height) {

    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(circ, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }
}

