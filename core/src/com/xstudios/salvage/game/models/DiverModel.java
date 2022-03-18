package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;

public class DiverModel extends GameObject {
    /** The physics body for Box2D. */
    protected Body body;

    /** The texture for the shape. */
    protected TextureRegion texture;

    /** The texture origin for drawing */
    protected Vector2 origin;


    /** Shape information for this box */
    protected PolygonShape shape;
    /** The width and height of the box */
    private Vector2 dimension;
    /** A cache value for when the user wants to access the dimensions */
    private Vector2 sizeCache;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;
    /** Cache of the polygon vertices (for resizing) */
    private float[] vertices;



    /**
     * Creates a new simple physics object
     *
     * @param x  Initial x position in world coordinates
     * @param y  Initial y position in world coordinates
     */
    public DiverModel(float x, float y){
        super(x,y);
        shape = new PolygonShape();
        origin = new Vector2();
        body = null;
    }
    /**
     * Sets the object texture for drawing purposes.
     *
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value  the object texture for drawing purposes.
     */
    public void setTexture(TextureRegion value) {
        texture = value;
        origin.set(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
    }
    @Override
    public boolean activatePhysics(World world) {
        // Make a body, if possible
        bodyinfo.active = true;
        body = world.createBody(bodyinfo);
        body.setUserData(this);
        body.setFixedRotation(false);
        body.setType(BodyDef.BodyType.DynamicBody);
        // Only initialize if a body was created.
        if (body != null) {
            createFixtures();
            return true;
        }

        bodyinfo.active = false;
        return false;
    }
    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    @Override
    public void deactivatePhysics(World world) {

    }

    @Override
    public void draw(GameCanvas canvas) {
        body.applyAngularImpulse(1f,false);
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x-texture.getRegionWidth()/2f,getY()*drawScale.y-texture.getRegionHeight()/2f,getAngle(),0.5f,0.5f);
        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {

    }
}
