package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.xstudios.salvage.util.FilmStrip;

public class Tentacle extends GameObject {
    private float x;
    private float y;
    private float angle;
    private float size = 1.0f;
    /**
     * Shape information for this box
     */
    protected PolygonShape shape;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    private float[] vertices;
    private TextureRegion tentacle;
    private FilmStrip tentacleSprite;
    protected Fixture geometry;
    private int frame = 0;
    private int life = 0;
    private int change = 0;
    private Vector2 dimension;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAngle() {
        return angle;
    }

    public float getSize() {
        return size;
    }

    public void setX(float x_base) {
        x = x_base;
    }

    public void setY(float y_base) {
        y = y_base;
    }

    public void setAngle(float temp_angle) {
        angle = temp_angle;
    }

    public void setSize(float temp_size) {
        size = temp_size;
    }

    public Tentacle(float x, float y) {
        super(x, y);
        shape = new PolygonShape();
        origin = new Vector2();
        body = null;
        vertices = new float[8];

        setDensity(1.0f);
        setFriction(0.5f);
        setMass(1);
        setFixedRotation(true);

        // Initialize
        setDimension(1, 1);
        setMass(1);
        resetMass();

    }

    public FilmStrip getTentacleSprite() {
        return tentacleSprite;
    }

    public void resize(float width, float height) {

    }

    public int getLife() {
        return life;
    }

    public void setTexture(TextureRegion value) {
        texture = value;
        origin.set(texture.getRegionWidth() / 2.0f, texture.getRegionHeight() / 2.0f);
    }

    public void setFilmStrip(FilmStrip value) {
        tentacleSprite = value;
        tentacleSprite.setFrame(1);
    }

    public int getFrame() {
        return tentacleSprite.getFrame();

    }

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


    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }

    public void dispose() {

    }

    public void update() {
        life++;
        if (life % 10 == 0) {
            int current_frame = tentacleSprite.getFrame();
            if (current_frame == 29) {
                change = -1;
            } else if (current_frame == 1) {
                change = 1;
            }
            frame = current_frame + change;
            tentacleSprite.setFrame(frame);
        }
        if (life > 100) {
            //releaseFixtures();
        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        if (texture != null) {
            canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        }
    }


    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(tentacleSprite, Color.RED, getX(), getY(), drawScale.x, drawScale.y, 1f, 1f);
        }
    }


    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }
        body.setUserData(this);

        return false;
    }
}
