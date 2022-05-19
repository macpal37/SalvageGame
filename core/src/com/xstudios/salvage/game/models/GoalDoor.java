package com.xstudios.salvage.game.models;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

/**
 * Box-shaped model to support collisions.
 * <p>
 * Unless otherwise specified, the center of mass is as the center.
 */
public class GoalDoor extends GameObject {
    /**
     * Shape information for this box
     */
    protected PolygonShape shape;
    /**
     * The width and height of the box
     */
    private Vector2 dimension;
    /**
     * A cache value for when the user wants to access the dimensions
     */
    private Vector2 sizeCache;
    /**
     * A cache value for the fixture (for resizing)
     */
    private Fixture geometry;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    private float[] vertices;


    private Light light;

    /**
     * Returns the dimensions of this box
     * <p>
     * This method does NOT return a reference to the dimension vector. Changes to this
     * vector will not affect the shape.  However, it returns the same vector each time
     * its is called, and so cannot be used as an allocator.
     *
     * @return the dimensions of this box
     */
    public Vector2 getDimension() {
        return sizeCache.set(dimension);
    }

    /**
     * Sets the dimensions of this box
     * <p>
     * This method does not keep a reference to the parameter.
     *
     * @param value the dimensions of this box
     */
    public void setDimension(Vector2 value) {
        setDimension(value.x, value.y);
    }

    /**
     * Sets the dimensions of this box
     *
     * @param width  The width of this box
     * @param height The height of this box
     */
    public void setDimension(float width, float height) {
        dimension.set(width, height);
        markDirty(true);
        resize(width, height);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getWidth() {
        return dimension.x;
    }

    /**
     * Sets the box width
     *
     * @param value the box width
     */
    public void setWidth(float value) {
        sizeCache.set(value, dimension.y);
        setDimension(sizeCache);
    }

    /**
     * Returns the box height
     *
     * @return the box height
     */
    public float getHeight() {
        return dimension.y;
    }

    /**
     * Sets the box height
     *
     * @param value the box height
     */
    public void setHeight(float value) {
        sizeCache.set(dimension.x, value);
        setDimension(sizeCache);
    }

    /**
     * Creates a new box at the origin.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public GoalDoor(float width, float height) {
        this(0, 0, width, height);
    }

    /**
     * Creates a new goal door object.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x      Initial x position of the box center
     * @param y      Initial y position of the box center
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public GoalDoor(float x, float y, float width, float height) {
        super(x, y);
        dimension = new Vector2(width, height);
        sizeCache = new Vector2();
        shape = new PolygonShape();
        vertices = new float[8];
        geometry = null;

        // Initialize
        resize(width, height);
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    public void resize(float width, float height) {
        // Make the box with the center in the center
        vertices[0] = -width / 2.0f;
        vertices[1] = -height / 2.0f;
        vertices[2] = -width / 2.0f;
        vertices[3] = height / 2.0f;
        vertices[4] = width / 2.0f;
        vertices[5] = height / 2.0f;
        vertices[6] = width / 2.0f;
        vertices[7] = -height / 2.0f;
        shape.set(vertices);
    }

    /**
     * Create new fixtures for this body, defining the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
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

    /**
     * Release the fixtures for this body, reseting the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }

    public void initLight(RayHandler rayHandler) {
        System.out.println("HEIGHT: " + getHeight());
        System.out.println("Y: " + getY());
        light = new PointLight(rayHandler, 100, new Color(255 / 255f, 220 / 255f, 92 / 255f, 0.2f), 15, getX(), getY());
        Filter f = new Filter();
        f.categoryBits = 0x0002;
        f.maskBits = 0x0004;
        f.groupIndex = 1;
        light.setContactFilter(f);
        light.setSoft(true);
    }

    @Override
    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        light.dispose();
    }

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
    }

    public Vector2 doorScale = new Vector2();

    public void setDoorScale(float w, float h) {
        doorScale.set(w, h);
    }

    protected FilmStrip sprite;

    public void setFilmStrip(FilmStrip value) {
        sprite = value;
        sprite.setFrame(0);
    }

    public void addTextures(TextureRegion closed, TextureRegion open) {
        openDoor = open;
        closedDoor = closed;
    }

    private TextureRegion openDoor;
    private TextureRegion closedDoor;
    int tick = 0;

    public void draw(GameCanvas canvas) {
//        tick++;
//        if (openDoor != null && closedDoor != null) {
//            float x = vertices[0];
//            float y = vertices[1];
//            if (sprite.getFrame() == 0) {
//                canvas.draw(closedDoor, ItemModel.COLOR_OPTIONS[getID()], 0, 0, x * drawScale.x,
//                        (y) * drawScale.y, getAngle(), doorScale.x * worldDrawScale.x, doorScale.y * worldDrawScale.y);
//            } else if (sprite.getFrame() < 11) {
//                if (tick % 6 == 0) {
//                    sprite.setFrame(sprite.getFrame() + 1);
//                }
//                canvas.draw(sprite, ItemModel.COLOR_OPTIONS[getID()], 0, 0, x * drawScale.x,
//                        (y) * drawScale.y, getAngle(), doorScale.x * worldDrawScale.x, doorScale.y * worldDrawScale.y);
//
//            } else {
//                canvas.draw(openDoor, ItemModel.COLOR_OPTIONS[getID()], 0, 0, x * drawScale.x,
//                        (y) * drawScale.y, getAngle(), doorScale.x * worldDrawScale.x, doorScale.y * worldDrawScale.y);
//            }
//        }
    }


}


