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
import com.xstudios.salvage.game.levels.LevelBuilder;
import com.xstudios.salvage.util.FilmStrip;

import java.util.ArrayList;

public class Tentacle extends GameObject {


    public enum TentacleType {
        OldAttack, NewAttack, Idle, KILL, NewAttack2, SmallAttack
    }

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

    public void setTentacleSprite2(FilmStrip tentacleSprite2) {
        this.tentacleSprite2 = tentacleSprite2;
    }

    private FilmStrip tentacleSprite2;


    protected Fixture geometry;
    private int frame = 0;
    private int life = 0;
    private int change = 0;
    private int extend_frame_length = 16;
    private int total_frames = 30;
    private int type;
    private TentacleType tentacleType;


    private Wall spawnWall;
    private int animation_length;

    public Tentacle(Wall wall, float len) {
        this(wall.getTentacleSpawnPosition().x, wall.getTentacleSpawnPosition().y);
        spawnWall = wall;
        spawnWall.setHasTentcle(true);
        setAngle(wall.getTentacleRotation() / 180 * (float) Math.PI);

        extend_frame_length = 16;

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

    public TentacleType getTentacleType() {
        return tentacleType;
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


    @Override
    public void setPosition(Vector2 value) {
        super.setPosition(value);
        for (HazardModel hm : collisionBoxes) {
            if (hm != null)
                hm.setPosition(value);
        }
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        for (HazardModel hm : collisionBoxes) {
            if (hm != null)
                hm.setPosition(x, y);
        }
    }

    @Override
    public void setAngle(float value) {
        super.setAngle(value);
        for (HazardModel hm : collisionBoxes) {
            if (hm != null)
                hm.setAngle(value);
        }
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
        if (isActive())
            life++;

        if (life > maxLifeSpan && startGrowing) {
            setStartGrowing(false);
        }


        if (tentacleType != TentacleType.Idle) {
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
        } else {
            if (frame == 1) {
                collisionBoxes[0].setActive(true);
            }
            if (frame == 5) {
                collisionBoxes[1].setActive(true);
            }
            if (frame == 10) {
                collisionBoxes[0].setActive(false);
            }
            if (frame == 15) {
                collisionBoxes[1].setActive(false);
            }
        }


    }

    /*==============================================
     * Activates the tentacles and makes it start growing
     */
    public void setStartGrowing(boolean startGrowing) {
        this.startGrowing = startGrowing;
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


    public Vector2 pivot = new Vector2(0, 0);

    public void setPivot(float x, float y) {
        pivot.set(x, y);
    }

    public void setAnimationLength(int l) {
        extend_frame_length = l;
    }

    public void setGrowRate(int grow_rate) {
        this.grow_rate = grow_rate;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TentacleType getType() {
        return this.tentacleType;
    }

    int grow_rate = 10;

    public boolean isDead() {
        return dead;
    }

    boolean dead = false;


    int wiggleFrame = 0;

    @Override
    public void draw(GameCanvas canvas) {
        update();

        if (frame >= 29) {
            frame = -1;
            dead = true;
        }


        if (startGrowing && frame < extend_frame_length && wiggleFrame == 0) {
            if (tick % grow_rate == 0) {
                frame++;
            }


        } else if (!startGrowing && frame > 0 && wiggleFrame == 0) {
            if (tick % grow_rate == 0) {
                frame++;
            }
        } else if (frame >= extend_frame_length && tentacleSprite2 != null && wiggleFrame == 0)
            wiggleFrame = 1;
        if (wiggleFrame > 0 && tentacleSprite2 != null) {
            if (tick % grow_rate == 0) {
                wiggleFrame++;
            }


            if (wiggleFrame == 17){
                wiggleFrame = 0;
                tentacleSprite2.setFrame(wiggleFrame); }
        }


        if (frame >= 0 && isActive()) {
            tentacleSprite.setFrame(frame);
            if (wiggleFrame == 0) {
                canvas.draw(tentacleSprite, Color.WHITE, 0, 0, (getX()) * drawScale.x + pivot.x,
                        (getY()) * drawScale.y + pivot.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y); }
            else {
                canvas.draw(tentacleSprite2, Color.WHITE, 0, 0, (getX()) * drawScale.x + pivot.x,
                        (getY()) * drawScale.y + pivot.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y); }
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
        spawnWall.setHasTentcle(false);
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

    public void setTentacleType(TentacleType type) {
        tentacleType = type;
    }
}
