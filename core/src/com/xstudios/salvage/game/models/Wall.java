package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.ShortArray;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameController;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

import java.util.Arrays;
import java.util.Collections;

public class Wall extends GameObject {
    /**
     * An earclipping triangular to make sure we work with convex shapes
     */
    private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

    public boolean isWall() {
        return isWall;
    }

    public void setWall(boolean wall) {
        isWall = wall;
    }

    public boolean isCanAlertMonster() {
        return canAlertMonster;
    }

    public void setCanAlertMonster(boolean canAlertMonster) {
        this.canAlertMonster = canAlertMonster;
    }

    private boolean canAlertMonster = false;

    private boolean isWall = false;

    /**
     * Shape information for this physics object
     */
    protected PolygonShape[] shapes;
    /**
     * Texture information for this object
     */
    protected PolygonRegion region;

    /**
     * The polygon vertices, scaled for drawing
     */
    private float[] scaled;
    /**
     * The triangle indices, used for drawing
     */
    private short[] tridx;

    /**
     * A cache value for the fixtures (for resizing)
     */
    protected Fixture[] geoms;
    /**
     * The polygon bounding box (for resizing purposes)
     */
    private Vector2 dimension;
    /**
     * A cache value for when the user wants to access the dimensions
     */
    private Vector2 sizeCache;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    protected float[] vertices;
    /**
     * The texture anchor upon region initialization
     */
    protected Vector2 anchor;
    /**
     * Whether the wall is invisible or not.
     */
    protected boolean invisible = false;

    protected FilmStrip sprite;


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
        resize(width, height);
        markDirty(true);
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
     * Creates a (not necessarily convex) polygon at the origin.
     * <p>
     * The points given are relative to the polygon's origin.  They
     * are measured in physics units.  They tile the image according
     * to the drawScale (which must be set for drawing to work
     * properly).
     *
     * @param points The polygon vertices
     */
    public Wall(float[] points) {
        this(points, 0, 0);
    }

    /**
     * Creates a (not necessarily convex) polygon
     * <p>
     * The points given are relative to the polygon's origin.  They
     * are measured in physics units.  They tile the image according
     * to the drawScale (which must be set for drawing to work
     * properly).
     *
     * @param points The polygon vertices
     * @param x      Initial x position of the polygon center
     * @param y      Initial y position of the polygon center
     */
    public Wall(float[] points, float x, float y) {
        super(x, y);
        assert points.length % 2 == 0;

        float minX = points[0];
        float minY = points[1];
        for (int i = 2; i < points.length; i++) {


            if (i % 2 == 0) {
                if (points[i] < minX)
                    minX = points[i];
            } else {
                if (points[i] < minY)
                    minY = points[i];
            }
        }
        wallPos = new Vector2(minX, minY);


        // Compute the bounds.
        initShapes(points);
        initBounds();
        circ.setRadius(0.0625f);
    }

/*===================================================================*
======================== Tentacle Spawn Location ========================
/===================================================================*
 */

    public boolean isHasTentcle() {
        return hasTentcle;
    }

    public void setHasTentcle(boolean hasTentcle) {
        this.hasTentcle = hasTentcle;
    }

    private boolean hasTentcle = false;


    public boolean canSpawnTentacle() {
        return tentacleSpawnPosition != null && !hasTentcle;
    }

    public Vector2 getTentacleSpawnPosition() {
        return tentacleSpawnPosition;
    }

    public float getTentacleRotation() {
        return tentacleRotation;
    }

    public void setTentacleSpawnPosition(float x, float y) {

        this.tentacleSpawnPosition = new Vector2(getX() + x, y + getY());
    }

    private Vector2 tentacleSpawnPosition = null;

    public void setTentacleRotation(float tentacleRotation) {
        this.tentacleRotation = tentacleRotation;
    }

    private float tentacleRotation = 0;


    /**
     * Initializes the bounding box (and drawing scale) for this polygon
     */
    private void initBounds() {
        float minx = vertices[0];
        float maxx = vertices[0];
        float miny = vertices[1];
        float maxy = vertices[1];

        for (int ii = 2; ii < vertices.length; ii += 2) {
            if (vertices[ii] < minx) {
                minx = vertices[ii];
            } else if (vertices[ii] > maxx) {
                maxx = vertices[ii];
            }
            if (vertices[ii + 1] < miny) {
                miny = vertices[ii + 1];
            } else if (vertices[ii] > maxy) {
                maxy = vertices[ii + 1];
            }
        }
        dimension = new Vector2((maxx - minx), (maxy - miny));
        sizeCache = new Vector2(dimension);
    }

    /**
     * Initializes the Box2d shapes for this polygon
     * <p>
     * If the texture is not null, this method also allocates the PolygonRegion
     * for drawing.  However, the points in the polygon region may be rescaled
     * later.
     *
     * @param points The polygon vertices
     */
    private void initShapes(float[] points) {
        // Triangulate
        ShortArray array = TRIANGULATOR.computeTriangles(points);
        trimColinear(points, array);

        tridx = new short[array.items.length];
        System.arraycopy(array.items, 0, tridx, 0, tridx.length);

        // Allocate space for physics triangles.
        int tris = array.items.length / 3;
        vertices = new float[tris * 6];
        shapes = new PolygonShape[tris];
        geoms = new Fixture[tris];
        for (int ii = 0; ii < tris; ii++) {
            for (int jj = 0; jj < 3; jj++) {
                vertices[6 * ii + 2 * jj] = points[2 * array.items[3 * ii + jj]];
                vertices[6 * ii + 2 * jj + 1] = points[2 * array.items[3 * ii + jj] + 1];
            }
            shapes[ii] = new PolygonShape();
            shapes[ii].set(vertices, 6 * ii, 6);
        }

        // Draw the shape with the appropriate scaling factor
        scaled = new float[points.length];
        for (int ii = 0; ii < points.length; ii += 2) {
            scaled[ii] = points[ii] * drawScale.x;
            scaled[ii + 1] = points[ii + 1] * drawScale.y;
        }
        if (texture != null) {
            // WARNING: PolygonRegion constructor by REFERENCE
            region = new PolygonRegion(texture, scaled, tridx);
        }

    }

    /**
     * Removes colinear vertices from the given triangulation.
     * <p>
     * For some reason, the LibGDX triangulator will occasionally return colinear
     * vertices.
     *
     * @param points  The polygon vertices
     * @param indices The triangulation indices
     */
    private void trimColinear(float[] points, ShortArray indices) {
        int colinear = 0;
        for (int ii = 0; ii < indices.size / 3 - colinear; ii++) {
            float t1 = points[2 * indices.items[3 * ii]] * (points[2 * indices.items[3 * ii + 1] + 1] - points[2 * indices.items[3 * ii + 2] + 1]);
            float t2 = points[2 * indices.items[3 * ii + 1]] * (points[2 * indices.items[3 * ii + 2] + 1] - points[2 * indices.items[3 * ii] + 1]);
            float t3 = points[2 * indices.items[3 * ii + 2]] * (points[2 * indices.items[3 * ii] + 1] - points[2 * indices.items[3 * ii + 1] + 1]);
            if (Math.abs(t1 + t2 + t3) < 0.0000001f) {
                indices.swap(3 * ii, indices.size - 3 * colinear - 3);
                indices.swap(3 * ii + 1, indices.size - 3 * colinear - 2);
                indices.swap(3 * ii + 2, indices.size - 3 * colinear - 1);
                colinear++;
            }
        }
        indices.size -= 3 * colinear;
        indices.shrink();
    }

    /**
     * Resize this polygon (stretching uniformly out from origin)
     *
     * @param width  The new width
     * @param height The new height
     */
    public void resize(float width, float height) {
        float scalex = width / dimension.x;
        float scaley = height / dimension.y;

        for (int ii = 0; ii < shapes.length; ii++) {
            for (int jj = 0; jj < 3; jj++) {
                vertices[6 * ii + 2 * jj] *= scalex;
                vertices[6 * ii + 2 * jj + 1] *= scaley;
            }
            shapes[ii].set(vertices, 6 * ii, 6);
        }

        // Reset the drawing shape as well
        for (int ii = 0; ii < scaled.length; ii += 2) {
            scaled[ii] *= scalex;
            scaled[ii + 1] *= scaley;
        }

        dimension.set(width, height);
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
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

        // Create the fixtures
        if (invisible) {
            for (int ii = 0; ii < shapes.length; ii++) {
                fixture.filter.categoryBits = 0x002;
                fixture.filter.groupIndex = 0x004;
                fixture.filter.maskBits = -1;
                fixture.shape = shapes[ii];
                geoms[ii] = body.createFixture(fixture);
            }
        } else {
            for (int ii = 0; ii < shapes.length; ii++) {
                fixture.filter.categoryBits = 0x004;
                fixture.filter.groupIndex = 0x002;
                fixture.filter.maskBits = -1;
                fixture.shape = shapes[ii];
                geoms[ii] = body.createFixture(fixture);
            }
        }
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     * <p>
     * This is the primary method to override for custom physics objects
     */
    protected void releaseFixtures() {
        if (geoms[0] != null) {
            for (Fixture fix : geoms) {
                body.destroyFixture(fix);
            }
        }
    }

    /**
     * Sets the object texture for drawing purposes.
     * <p>
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value the object texture for drawing purposes.
     */
    public void setTexture(TextureRegion value) {
        texture = value;
        initRegion();
    }

    private void initRegion() {
        if (texture == null) {
            return;
        }
        float[] scaled = new float[vertices.length];
        for (int ii = 0; ii < scaled.length; ii++) {
            if (ii % 2 == 0) {
                scaled[ii] = (vertices[ii] + getX()) * drawScale.x;
            } else {
                scaled[ii] = (vertices[ii] + getY()) * drawScale.y;
            }
        }
        short[] tris = {0, 1, 3, 3, 2, 1};
        anchor = new Vector2(getX(), getY());
        region = new PolygonRegion(texture, scaled, tris);
    }

    /**
     * Sets the drawing scale for this physics object
     * <p>
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     * <p>
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x the x-axis scale for this physics object
     * @param y the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
        assert x != 0 && y != 0 : "Scale cannot be 0";
        float dx = x / drawScale.x;
        float dy = y / drawScale.y;
        // Reset the drawing shape as well
        for (int ii = 0; ii < scaled.length; ii += 2) {
            scaled[ii] *= dx;
            scaled[ii + 1] *= dy;
        }
        if (texture != null) {
            region = new PolygonRegion(texture, scaled, tridx);
        }
        drawScale.set(x, y);
    }


    public int getFrame() {
        return sprite.getFrame();

    }

    public void setFilmStrip(FilmStrip value) {
        sprite = value;
        sprite.setFrame(getID());
    }


    Vector2 wallPos = new Vector2();

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        if (sprite != null && !invisible) {
            canvas.draw(sprite, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(),
                    40f / 25f * worldDrawScale.x, 40f / 25f * worldDrawScale.y);
        }


    }

    private CircleShape circ = new CircleShape();

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    @Override
    public void drawDebug(GameCanvas canvas) {

        if (canSpawnTentacle())
            canvas.drawPhysics(circ, Color.RED, tentacleSpawnPosition.x, tentacleSpawnPosition.y, drawScale.x, drawScale.y);
        for (PolygonShape tri : shapes) {
            canvas.drawPhysics(tri, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        }

    }


}