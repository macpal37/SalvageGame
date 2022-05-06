package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.xstudios.salvage.util.FilmStrip;

import java.util.ArrayList;

public class Tentacle extends GameObject {
    /**
     * Shape information for this physics object
     */
    protected PolygonShape[] shapes;

    /**
     * Shape information for this physics object
     */
    protected ArrayList<Fixture[]> geoms = new ArrayList<>();
    /**
     * Cache of the polygon vertices (for resizing)
     */

    protected HazardModel[] collisionBoxes;


    private float[] vertices;
    private FilmStrip tentacleSprite;
    protected Fixture geometry;
    private int frame = 0;
    private int life = 0;
    private int change = 0;
    private int extend_frame_length = 16;
    private int total_frames = 30;

    private Wall spawnWall;
    private int animation_length;

    public Tentacle(Wall wall, float len) {
        this(wall.getTentacleSpawnPosition().x, wall.getTentacleSpawnPosition().y);
        spawnWall = wall;
        spawnWall.setHasTentcle(true);
        setAngle(wall.getTentacleRotation() / 180 * (float) Math.PI);
        System.out.println("length " + len);
        extend_frame_length = Math.min(extend_frame_length,(int)(len));
//        setStartGrowing(false);
        System.out.println("extend frame length: " + extend_frame_length);
        System.out.println("LENGTH " + len);
    }

    public Tentacle() {
        this(0, 0);
    }

    public Tentacle(float x, float y) {
        super(x, y);

        origin = new Vector2();
        body = null;

        setDensity(1.0f);
        setFriction(0.5f);
        setMass(1);
        setFixedRotation(true);
        // Initialize
        setDimension(1, 1);
        setMass(1);
        resetMass();
        collisionBoxes = new HazardModel[4];
        circ.setRadius(0.0625f * 2);
    }

    public void initShape(HazardModel[] hazards) {
        int i = 0;
        for (HazardModel hm : hazards) {
            collisionBoxes[i] = hazards[i++];
        }
    }


    public FilmStrip getTentacleSprite() {
        return tentacleSprite;
    }

    public void resize(float width, float height) {

    }

    public int getLife() {
        return life;
    }

    public Wall getSpawnWall() {
        return spawnWall;
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

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        for (HazardModel hm : collisionBoxes) {
            hm.createFixtures();
        }

        markDirty(false);
    }


    protected void releaseFixtures() {
        for (HazardModel hm : collisionBoxes)
            hm.releaseFixtures();
    }

    public void dispose() {

    }

    private float maxLifeSpan = 1000;

    public void setMaxLifeSpan(float maxLifeSpan) {
        this.maxLifeSpan = maxLifeSpan;
    }

    public void update() {
        life++;

//        if (life > maxLifeSpan && startGrowing) {
        System.out.println("frame " + frame + " max life span " + extend_frame_length);
        if (frame >= extend_frame_length && life > maxLifeSpan && startGrowing) {
                setStartGrowing(false);
        }

        if (frame == 1) {
            collisionBoxes[0].setActive(true);
        }
        if (frame == 5) {
            collisionBoxes[1].setActive(true);
        }
        if (frame == 10) {
            collisionBoxes[2].setActive(true);
        }
        if (frame == 15) {
            collisionBoxes[3].setActive(true);
        }
        if (frame == 17) {
            collisionBoxes[3].setActive(false);
        }
        if (frame == 20) {
            collisionBoxes[2].setActive(false);
        }
        if (frame == 24) {
            collisionBoxes[1].setActive(false);
        }
        if (frame == 29) {
            collisionBoxes[0].setActive(false);
        }


    }

    /*==============================================
     * Activates the tentacles and makes it start growing
     */
    public void setStartGrowing(boolean startGrowing) {
        this.startGrowing = startGrowing;
        System.out.println("start growing: " + startGrowing + " frame " + frame + " total frame " + extend_frame_length);
        if(!startGrowing && frame <= extend_frame_length) {

            frame = total_frames - frame;
        }
    }

    public boolean isStartGrowing() {
        return startGrowing;
    }

    private boolean startGrowing = false;

    private CircleShape circ = new CircleShape();

    @Override
    public void drawDebug(GameCanvas canvas) {


        canvas.drawPhysics(circ, Color.GREEN, getX(), getY(), drawScale.x, drawScale.y);
        for (HazardModel hm : collisionBoxes) {
            hm.drawDebug(canvas);
        }
    }

    public Vector2 getScale() {
        return scale;
    }

    public Wall getDead() {
        if (frame == -1) {
            return spawnWall;
        } else {
            return null;
        }
    }

    public Vector2 scale = new Vector2(1 / 2f, 1 / 2f);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }


    int tick = 0;

    public Vector2 pivot = new Vector2(0, 0);

    public void setPivot(float x, float y) {
        pivot.set(x, y);
    }

    public void setAnimationLength(int l) {
        extend_frame_length = l;
    }

    @Override
    public void draw(GameCanvas canvas) {
        update();

        tick++;
        int grow_rate = 10;
        if (frame > 30) {
            frame = -1;

        }
        if (startGrowing && frame < extend_frame_length) {
            if (tick % grow_rate == 0) {
                frame++;
            }
        } else if (!startGrowing && frame > 0) {
            if (tick % grow_rate == 0) {
                frame++;
            }
        }

        if (frame >= 0) {
            tentacleSprite.setFrame(frame);
            canvas.draw(tentacleSprite, Color.WHITE, 0, 0, (getX()) * drawScale.x + pivot.x, (getY()) * drawScale.y + pivot.y, getAngle(), scale.x, scale.y);


        }

    }


    public void despawn() {
        spawnWall.setHasTentcle(false);
        for (HazardModel hm : collisionBoxes) {
            hm.setActive(false);
        }
    }


    public boolean activatePhysics(World world) {
        for (HazardModel hm : collisionBoxes) {
            hm.activatePhysics(world);

            for (int i = 0; i < hm.getFixtureList().length; i++) {
                hm.getFixtureList()[i].setUserData(this);
            }
            hm.setActive(false);
        }

        return false;
    }

    /**
     * Destroys the physics Body(s) of this object if applicable,
     * removing them from the world.
     *
     * @param world Box2D world that stores body
     */
    public void deactivatePhysics(World world) {
        for (HazardModel hm : collisionBoxes) {
            hm.deactivatePhysics(world);
        }


        if (body != null) {
            // Snapshot the values
            setBodyState(body);
            world.destroyBody(body);
            body = null;
            bodyinfo.active = false;
        }
    }
}
