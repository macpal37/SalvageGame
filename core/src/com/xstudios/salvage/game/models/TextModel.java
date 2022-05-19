package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

public class TextModel extends GameObject {


    private FilmStrip spriteSheet;

    private Fixture geometry;
    private PolygonShape shape;
    private CircleShape circ;


    public TextModel(float x, float y) {
        super(x, y);

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
        fixture.shape = shape;

        geometry = body.createFixture(fixture);
        markDirty(false);
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
}

