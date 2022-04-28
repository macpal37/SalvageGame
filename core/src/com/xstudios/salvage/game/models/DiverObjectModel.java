package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameObject;

public abstract class DiverObjectModel extends GameObject {


    /**
     * Drawing scale for when being carried
     */
    protected Vector2 drawSymbolScale;
    /**
     * If item is being carried
     */
    protected boolean carried;

    protected boolean isTouched;
    /**
     * Shape information for this box
     */
    protected PolygonShape shape;
    /**
     * A cache value for the fixture (for resizing)
     */
    protected Fixture geometry;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    protected float[] vertices;
    /**
     * The factor to multiply by the input
     */
    protected float force;
    /**
     * Cache for internal force calculations
     */
    protected final Vector2 forceCache = new Vector2();
    /**
     * The amount to slow the character down
     */
    protected float damping;
    /**
     * The maximum character speed
     */
    protected float maxspeed;

    /**
     * Creates a new simple physics object
     *
     * @param x  Initial x position in world coordinates
     * @param y  Initial y position in world coordinates
     */
    /**
     * Creates a new simple physics object
     *
     * @param data Json values
     */
    protected DiverObjectModel(float x, float y, JsonValue data) {
        // super (x,y);
        super(x,
                y);

        shape = new PolygonShape();
        origin = new Vector2();
        body = null;
        vertices = new float[8];

        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
        setMass(1);
        setFixedRotation(true);
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);

        // Initialize
        setDimension(1, 1);
        setMass(1);
        resetMass();
        drawSymbolScale = new Vector2(1, 1);
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

    /**
     * Returns the drawing scale for this object when it is being carried
     * <p>
     * We allow for the scaling factor to be non-uniform.
     *
     * @return the drawing scale for this object when it is being carried
     */
    public Vector2 getDrawSymbolScale() {
        scaleCache.set(drawSymbolScale);
        return scaleCache;
    }

    /**
     * Sets the drawing scale for this object when it is being carried
     * <p>
     * We allow for the scaling factor to be non-uniform.
     *
     * @param value the drawing scale for this object when it is being carried
     */
    public void setDrawSymbolScale(Vector2 value) {
        setDrawSymbolScale(value.x, value.y);
    }

    /**
     * Sets the drawing scale for this object when it is being carried
     *
     * @param x the x-axis scale for this object
     * @param y the y-axis scale for this object
     */
    public void setDrawSymbolScale(float x, float y) {
        drawSymbolScale.set(x, y);
    }


    public void setCarried(boolean b) {
        carried = b;

    }

    public boolean isCarried() {
        return carried;
    }


    public boolean isTouched() {
        return isTouched;
    }

    public void setTouched(boolean touched) {
        isTouched = touched;
    }

    public Color getColor() {
        return Color.WHITE;
    }
}
