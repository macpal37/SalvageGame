package com.xstudios.salvage.game.models;

import box2dLight.RayHandler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;
import com.xstudios.salvage.util.PooledList;


import java.util.ArrayList;

public class DiverModel extends GameObject {

    /**
     * Shape information for this box
     */
    protected PolygonShape shape;


    protected CircleShape end1;
    /**
     * Shape information for the end cap
     */
    protected CircleShape end2;

    private Fixture cap1;
    /**
     * A cache value for the second end cap fixture (for resizing)
     */
    private Fixture cap2;

    /**
     * The texture for the shape.
     */
    protected TextureRegion pingTexture;


    private ArrayList<FilmStrip> diverSprites;

    public PooledList<TreasureModel> getTreasureChests() {
        return treasureChests;
    }

    private PooledList<TreasureModel> treasureChests = new PooledList<>();


    private int minFlareDist = 5;

    private FilmStrip diverSprite;


    private int diverState = 0;
    private final int DIVER_IMG_FLAT = 6;

    private int invincible_time = 0;
    private int MAX_INVINCIBLE_TIME;

    private short no_hazard_collision_mask = 0x004;
    private short no_hazard_collision_category = 0x002;

    private short hazard_collision_mask = -1;
    private short hazard_collision_category = 0x002;

    private ArrayList<FixtureDef> diver_fixtures;

    public void addFilmStrip(FilmStrip value) {
        value.setFrame(DIVER_IMG_FLAT);
        diverSprites.add(value);

    }


    /**
     * A cache value for the fixture (for resizing)
     */
    private Fixture geometry;
    /**
     * Cache of the polygon vertices (for resizing)
     */
    private float[] vertices;
    /**
     * The movement of the character
     */
    private Vector2 movement;
    /**
     * The movement of the character from currents
     */
    private Vector2 drift_movement;

    /**
     * The factor to multiply by the input
     */
    private final float force;
    /**
     * The amount to slow the character down
     */
    private float damping;
    /**
     * The maximum character speed
     */
    private final float swimMaxSpeed;

    /**
     * The maximum character speed when drifting
     */

    private final float drift_maxspeed;

    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    /**
     * Cache for internal force calculations
     */
    private final Vector2 forceCache = new Vector2();
    /**
     * item that diver is currently carrying
     */
    private ArrayList<ItemModel> item_list;

    private int num_keys = 0;

    /**
     * dead body that is the target for the level
     */
    private DeadBodyModel dead_body;

    private boolean carrying_body;
    private boolean contact_body;

    /**
     * All the itemModels diver is in contact with
     */
    protected ArrayList<ItemModel> potential_items = new ArrayList<ItemModel>();

    /**
     * whether user is pinging
     */
    private boolean ping;
    /**
     * ping cooldown
     */
    private int ping_cooldown;
    /**
     * whether user is pinging
     */
    private Vector2 pingDirection;
    private final int MAX_PING_COOLDOWN = 20;
    /**
     * whether user wants to pick up/drop item
     */
    private boolean pickUpOrDrop;

    /**
     * whether the diver is stunned
     */
    private boolean stunned;
    /**
     * cooldown on stun
     */
    private float stunCooldown;

    /**
     * Store oxygen level
     */
    private float oxygenLevel;
    private float maxOxygenLevel = 150;

    /**
     * number of flares
     */
    private int num_flares;
    private ArrayList<FlareModel> flares;
    private ArrayList<FlareModel> neighboring_flares;
    /**
     * flare duration
     */
    private int flare_duration;
    private int MAX_FLARE_DURATION;
    /**
     * true if there is currently an active flare
     */
    private boolean active_flare;

    /**
     * used to indicate whether the diver is colliding with the monster
     * if the diver is colliding with the monster, then set it to true so that we can change the
     * filter values for the light
     */
    private boolean changeLightFilter;

    /** Diver Sensor Used to pick up items and open doors*/

    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private final String sensorNameRight;
    private final String sensorNameLeft;
    private final String hitboxSensorName;
    private final String diverCollisionBox;
    /**
     * The physics shape of this object
     */
//    private PolygonShape sensorShapeRight;
//    private PolygonShape sensorShapeLeft;
    private PolygonShape hitboxShape;


    /**
     * Whether you are touching another GameObject
     */
    private ArrayList<GObject> touchingRight;
    private ArrayList<GObject> touchingLeft;

    public Wall getTouchedWall() {
        return touchedWall;
    }

    public void setTouchedWall(Wall touchedWall) {
        this.touchedWall = touchedWall;
    }

    private Wall touchedWall = null;


    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;


    // ==================== Player Body==============================

    /**
     * The width and height of the box
     */
    private Vector2 dimension;


    /**
     * Rectangle representation of capsule core for fast computation
     */
    protected Rectangle center;

    /**
     * A cache value for when the user wants to access the dimensions
     */
    private Vector2 sizeCache;


    private boolean isTouchingObstacle;
    /**
     * if the player is currently latched onto a wall
     */
    private boolean latchedOn;

    /**
     * the max speed given that there is a speed boost which exceeds the
     * normal max speed
     */
    private final float boostedMaxSpeed;
    private float maxSpeed;
    private boolean boosting;

    private final float swimDamping;
    private final float boostDamping;
    private Vector2 facingDir;

    public float getMaxOxygen() {

        return maxOxygenLevel;
    }

    public void setMaxOxygen(float max) {
        System.out.println("Maxed Out!");
        maxOxygenLevel = max;
        oxygenLevel = max;
    }

    // ======================== CONSTRUCTORS ================================

    /**
     * @param data
     */

    public DiverModel(float x, float y, JsonValue data) {
        super(x, y);
        shape = new PolygonShape();
        vertices = new float[8];
        diverSprites = new ArrayList<>();
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
        setLinearDamping(data.getFloat("damping", 0));
        setMass(1);
        setFixedRotation(true);

        swimMaxSpeed = data.getFloat("maxspeed", 0);
        drift_maxspeed = data.getFloat("drift_maxspeed", 0);

        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);
        MAX_INVINCIBLE_TIME = data.getInt("invincible_time", 20);

        dimension = new Vector2();
        sizeCache = new Vector2();

        sensorNameRight = "DiverSensorRight";
        sensorNameLeft = "DiverSensorLeft";
        hitboxSensorName = "HitboxSensor";
        diverCollisionBox = "DiverBox";
        touchingRight = new ArrayList<>();
        touchingLeft = new ArrayList<>();
        // Initialize
        faceRight = true;
        setDimension(1.2f, 0.35f);
        setMass(1);
        resetMass();
        setName("diver");

        this.data = data;
        item_list = new ArrayList<>();
        ping = false;
        movement = new Vector2();
        drift_movement = new Vector2();

        oxygenLevel = data.getInt("max_oxygen", (int) maxOxygenLevel);

        num_flares = data.getInt("num_flares", 5);
        MAX_FLARE_DURATION = data.getInt("flare_duration", 100);
        flare_duration = 0;
        flares = new ArrayList<>();
        neighboring_flares = new ArrayList<>();
        active_flare = false;
        for (int i = 0; i < num_flares; i++) {
            flares.add(new FlareModel(data));
        }

        pingDirection = new Vector2();
        ping_cooldown = 0;
        center = new Rectangle();
        center.x = -dimension.x / 2.0f;
        center.y = -dimension.y / 2.0f;
        center.width = dimension.x;
        center.height = dimension.y;
        end1 = new CircleShape();
        end2 = new CircleShape();
        cap1 = null;
        cap2 = null;
        // TODO: Put this in the constants JSON

        boostedMaxSpeed = swimMaxSpeed * 1.5f;

        maxSpeed = swimMaxSpeed;
        swimDamping = damping;

        boostDamping = damping / 7;
        facingDir = new Vector2(0, 0);

        setFixedRotation(false);


        carrying_body = false;
        dead_body = null;
        diver_fixtures = new ArrayList<>();
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    protected void resize(float width, float height) {
        // Make the box with the center in the center
        vertices[0] = -width / 2.0f;
        vertices[1] = -height / 2.0f;
        vertices[2] = -width / 2.0f;
        vertices[3] = height / 2.0f;
        vertices[4] = width / 2.0f;
        vertices[5] = height / 2.0f;
        vertices[6] = width / 2.0f;
        vertices[7] = -height / 2.0f;
        shape.setAsBox(width, height);
    }

    public void setVerticalMovement(float value) {
        movement.y = value;
    }

    //TODO Why is this just hanging out in the middle of the code?
    private boolean switchDir = false;


    int turnFrames = 0;
    int kickOffFrame = 10;
    int pickupFrame = 6;
    int idleFrame = 0;

    public void balanceRotation() {
        while (body.getAngle() > 0.1 || body.getAngle() < -0.1) {
            if (body.getAngle() > 0) {
                body.setAngularVelocity(-0.01f);
            } else if (getBody().getAngle() < 0) {
                body.setAngularVelocity(0.01f);
            } else {
                body.setAngularVelocity(0.0f);
            }
        }
    }


    public void setHorizontalMovement(float value) {
        movement.x = value;
        if (movement.x < 0 && faceRight) {
        } else if (movement.x > 0 && !faceRight) {

        }


        // Change facing if appropriate


    }

    public void setFacingDir(float x, float y) {
        facingDir.set(x, y);
    }

    public void setDriftMovement(float x_val, float y_val) {
        if (movement.isZero()) {
            drift_movement.x = x_val;
            drift_movement.y = y_val;
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
        origin.set(texture.getRegionWidth() / 2.0f, texture.getRegionHeight() / 2.0f);
    }

    public boolean hasItem() {
        return potential_items.size() > 0;
    }

    public void setDeadBody(DeadBodyModel b) {
        dead_body = b;
    }

    public boolean hasBody() {
        return carrying_body;
    }

    public void updateDeadBodyPos() {
        if (dead_body != null) {
            dead_body.setX(getX());
            dead_body.setY(getY());
        }
    }

    public void setCarryingBody() {
        carrying_body = contact_body;
    }


    /**
     * Sets the object texture for drawing purposes.
     * <p>
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value the object texture for drawing purposes.
     */
    public void setPingTexture(TextureRegion value) {
        pingTexture = value;
    }

    /**
     * Sets the ping direction for drawing purposes.
     *
     * @param bodypos the ping direction for drawing purposes.
     */
    public void setPingDirection(Vector2 bodypos) {
        pingDirection.set(getPosition()).sub(bodypos);//.sub(texture.getRegionWidth()/2f + body_width, texture.getRegionHeight()/2f + body_height);
        pingDirection.nor();
        pingDirection.scl(getTexture().getRegionWidth());
    }

    public void setPing(boolean p) {
        ping = p;
        if (ping) {
            ping_cooldown = MAX_PING_COOLDOWN;
        }
    }

    public void resetInvincibleTime() {
        invincible_time = MAX_INVINCIBLE_TIME;
    }

    public int getInvincibleTime() {
        return invincible_time;
    }

    public void reduceInvincibleTime() {
        if (invincible_time > 0) {
            invincible_time--;
        }
    }

    public boolean isInvincible() {
        return invincible_time > 0;
    }

    /**
     * set stunned
     */
    public void setStunned(boolean stun) {

        stunned = stun;
    }

    /**
     * set stun cooldown
     */
    public void setStunCooldown(float cooldown) {
        stunCooldown = cooldown;
    }

    /**
     * decrement stun cooldown
     */
    public void decrementStunCooldown(float decrement) {
        stunCooldown -= decrement;
    }

    public boolean getStunned() {
        return stunned;
    }

    public float getStunCooldown() {
        return stunCooldown;
    }


    /**
     * Returns the name of the ground sensor
     * <p>
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {

        if (faceRight) {
            return getSensorNameRight();
        } else {
            return getSensorNameLeft();
        }

    }

    private Vector2 posCache = new Vector2();

    public String getSensorNameRight() {
        return sensorNameRight;
    }

    public String getSensorNameLeft() {
        return sensorNameLeft;
    }

    public String getSensorNameHitBox() {
        return hitboxSensorName;
    }

    public boolean activatePhysics(World world) {

        if (!super.activatePhysics(world)) {
            return false;
        }
        body.setUserData(this);


        JsonValue sensorjv = data.get("sensor");


        // create a sensor to detect wall collisions
        FixtureDef hitboxDef = new FixtureDef();
        hitboxDef.density = data.getFloat("density", 0);
        hitboxDef.isSensor = true;
        // we don't want this fixture to collide, just act as a sensor
//        hitboxDef.filter
        hitboxDef.filter.categoryBits = 0x0002;
        hitboxDef.filter.maskBits = 0x0004;
        hitboxDef.filter.groupIndex = 1;
        hitboxShape = new PolygonShape();
        hitboxShape.setAsBox(getWidth() * 1.6f, getHeight(),
                new Vector2(0, 0), 0.0f);
        hitboxDef.shape = hitboxShape;
//        Fixture hitboxFixture = body.createFixture(hitboxDef);
//        hitboxFixture.setUserData(hitboxSensorName);

        for (FlareModel f : flares) {
            f.activatePhysics(world);
        }

        diver_fixtures.add(hitboxDef);
        return true;
    }

    public String getHitboxSensorName() {
        return hitboxSensorName;
    }

    public String getDiverCollisionBox() {
        return diverCollisionBox;
    }


    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        for (FlareModel f : flares) {
            f.removeLights();
        }
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

    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();
        // Create the fixture
        fixture.shape = shape;
//        fixture.filter.categoryBits = 0x002;
//        fixture.filter.groupIndex = 0x006;
//        fixture.filter.maskBits = -1;
        fixture.filter.maskBits = -1;
        fixture.filter.groupIndex = -1;
        geometry = body.createFixture(fixture);
        geometry.setUserData(getDiverCollisionBox());
        markDirty(false);
        // so im gonna change the filter category bits
        // and stuff so the diver can't collide with the monster for a specified amonut of time
    }

    public void setHazardInvincibilityFilter() {
        setFilterData(no_hazard_collision_mask, no_hazard_collision_category);
    }

    public void setHazardCollisionFilter() {
        setFilterData(hazard_collision_mask, hazard_collision_category);
    }

    private void setFilterData(short mask, short category) {
//        if (body == null) {
//            return;
//        }
//
//        fixture.filter.categoryBits = category;
//        fixture.filter.maskBits = mask;
        if (body == null || (mask == fixture.filter.maskBits && category == fixture.filter.categoryBits)) {
            return;
        }

//        releaseFixtures();
        // Create the fixture
//        fixture.shape = shape;
//        fixture.filter.categoryBits = category;
//        fixture.filter.groupIndex = 0x004;
//        fixture.filter.maskBits = mask;
        Filter f = new Filter();
        f.categoryBits = category;
        f.maskBits = mask;
        f.groupIndex = 0x006;
        setFilterData(f);
//        System.out.println("SET CATEGORY TO "+ fixture.filter.categoryBits + " mask to "+ fixture.filter.maskBits);
//        geometry = body.createFixture(fixture);
//        geometry.setUserData(getDiverCollisionBox());
        markDirty(true);
    }

    /**
     * Returns left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getHorizontalMovement() {
        return movement.x;
    }

    /**
     * Returns up/down movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getVerticalMovement() {
        return movement.y;
    }


    /**
     * Returns left/right movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getHorizontalDriftMovement() {
        return drift_movement.x;
    }

    /**
     * Returns up/down movement of this character.
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getVerticalDriftMovement() {
        return drift_movement.y;
    }


    /**
     * Returns how much force to apply to get the dude moving
     * <p>
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float speed) {
        maxSpeed = speed;
    }


    // TODO: Having a state machine would probably be helpful
    public boolean isSwimming() {
        return !isLatching() /*&& !isBoosting() */ && movement.len() != 0;
    }

    public boolean isIdling() {
        return !isLatching() && !isBoosting() && movement.isZero();
    }

    int tick = 0;
    boolean stroke = false;

    public float getDynamicAngle() {


        int a = (int) ((getBody().getAngle()) / Math.PI * 180) + ((faceRight) ? 0 : 180);
        while (a < 0)
            a += 360;

        return a % 360;
    }


    public int targetAngleX = 0;
    public int targetAngleY = 0;
    public boolean bodyFlip = false;

    public void setTargetAngle(int x, int y) {
        if (x != -1)
            targetAngleX = x;
        if (y != -1)
            targetAngleY = y;
    }

    float targetAngle = 0;

    public void applyForce() {
        if (isLatching()) {
            if (touchedWall != null)
                targetAngle = touchedWall.getTentacleRotation() + 270;
            else
                targetAngle = 0;
        } else {

            targetAngle = (isSwimming() || isBoosting()) ? targetAngleX + ((targetAngleX == 0) ? targetAngleY : -targetAngleY) : getDynamicAngle();

        }

        targetAngle += (targetAngle < 0) ? 360f : 0f;
        float dist = targetAngle - getDynamicAngle();
        float angle = 0.4f * 3;
        int buffer = 5;
        int flip = 180;
        if (Math.abs(dist) >= 180 - buffer * 5) {
            dist += (dist > 0) ? -flip : flip;
            faceRight = !faceRight;
            if (Math.abs(dist) >= 90) {
                dist += (dist > 0) ? -flip : flip;
                faceRight = !faceRight;
            } else {
                turnFrames = 4;
            }
        }
//        float coAngle = 180 * angle / (float) Math.PI;

        float newAngle = angle * ((isLatching()) ? ((isBoosting()) ? 2 : 2) : 1);
        float coAngle = angle;


        if (dist > coAngle / 2) {
            body.setAngularVelocity(newAngle);
        } else if (dist < -coAngle / 2) {
            body.setAngularVelocity(-newAngle);
        } else {
            body.setAngularVelocity(0.0f);
        }
        float tinyBuffer = 5f;

//        if (getDynamicAngle() != 269 || getDynamicAngle() != 271 || getDynamicAngle() != 89 || getDynamicAngle() != 91) {

        if (getDynamicAngle() <= 90 - tinyBuffer || getDynamicAngle() > 270 + tinyBuffer) {
            bodyFlip = !faceRight;
        }
        if (getDynamicAngle() > 90 + tinyBuffer && getDynamicAngle() <= 270 - tinyBuffer) {
            bodyFlip = faceRight;
        }


        if (!isActive()) {
            return;
        }


        float desired_xvel = 0;
        float desired_yvel = 0;
        float max_impulse = 15f;
        float max_impulse_drift = 2f;


//        tick++;
        // possible states: swimming, idling/drifting, latching, boosting
        if (isSwimming()) { // player is actively using the arrow keys
            System.out.println("IS SWIMMING");

            // set custom max speed and damping values
            setMaxSpeed(swimMaxSpeed);
            setLinearDamping(swimDamping);

            int frame = diverSprites.get(diverState).getFrame();
            if (pickupFrame >= 5) {
                if (tick % 5 == 0) {
                    frame++;
                    if (frame >= 12)
                        frame = 0;
                    diverSprites.get(diverState).setFrame(frame);
                }
            }

            // compute desired velocity, capping it if it exceeds the maximum speed
            // TODO: Do we only want to be able to swim in 4 directions?
            desired_xvel = getVX() + Math.signum(getHorizontalMovement()) * max_impulse;
            desired_xvel = Math.max(Math.min(desired_xvel, getMaxSpeed()), -getMaxSpeed());
            desired_yvel = getVY() + Math.signum(getVerticalMovement()) * max_impulse;
            desired_yvel = Math.max(Math.min(desired_yvel, getMaxSpeed()), -getMaxSpeed());

            float xvel_change = desired_xvel - getVX();
            float yvel_change = desired_yvel - getVY();

            float x_impulse = body.getMass() * xvel_change;
            float y_impulse = body.getMass() * yvel_change;


            if (movement.y > 0) {
                targetAngleY = 85;
            } else if (movement.y < 0) {
                targetAngleY = -85;
            }
            if (movement.x != 0) {
                targetAngleY /= 2;
            }
            if (movement.x > 0) {
                targetAngleX = 0;
            } else if (movement.x < 0) {
                targetAngleX = 180;
            }


            body.applyForce(x_impulse, y_impulse, body.getWorldCenter().x,
                    body.getWorldCenter().y, true);
        } else if (isIdling()) { // player is not using the arrow keys
            setMaxSpeed(drift_maxspeed);
            setLinearDamping(swimDamping);

//            targetAngleY = (int) getDynamicAngle() - targetAngleX;

            /**====================================================*/
            /**============= Turning Angle Code=============*/
            /**====================================================*/

//            if (pickupFrame >= 5) {
//                if (tick % 10 == 0) {
//                    int frame = diverSprites.get(diverState).getFrame();
//
//                    diverSprites.get(diverState).setFrame(frame);
//                }
//            }
            /**====================================================*/
            /**============= Turning Angle Cod: ENDe=============*/
            /**====================================================*/

            desired_xvel = getVX() + Math.signum(getHorizontalDriftMovement()) * max_impulse_drift;
            desired_xvel = Math.max(Math.min(desired_xvel, getMaxSpeed()), -getMaxSpeed());
            desired_yvel = getVY() + Math.signum(getVerticalDriftMovement()) * max_impulse_drift;
            desired_yvel = Math.max(Math.min(desired_yvel, getMaxSpeed()), -getMaxSpeed());

            float xvel_change = desired_xvel - getVX();
            float yvel_change = desired_yvel - getVY();

            float x_impulse = body.getMass() * xvel_change;
            float y_impulse = body.getMass() * yvel_change;

            body.applyForce(x_impulse, y_impulse, body.getWorldCenter().x,
                    body.getWorldCenter().y, true);
        } else if (isLatching()) { // player is latched onto a wall
            body.setLinearVelocity(0, 0);
        } else if (isBoosting()) { // player has kicked off a wall and may or may not be steering
            setMaxSpeed(boostedMaxSpeed);
            setLinearDamping(boostDamping);
            if (movement.y > 0) {
//                targetAngleY = 85;

            } else if (movement.y < 0) {
                targetAngleY = -85;
            }
            if (movement.x != 0) {
                targetAngleY /= 2;
            }
            if (movement.x > 0) {
                targetAngleX = 0;
            } else if (movement.x < 0) {
                targetAngleX = 180;
            }
            // TODO: Currently doesn't take movement input. Will need steering in specific dirs only?
            if (Math.abs(getVX()) >= getMaxSpeed()) {
                setVX(Math.signum(getVX()) * getMaxSpeed());
            }
            if (Math.abs(getVY()) >= getMaxSpeed()) {
                setVY(Math.signum(getVY()) * getMaxSpeed());
            }
        }
//        for(FlareModel f: flares) {
//            f.applyForce();
//        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {

        canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
//        canvas.drawPhysics(sensorShapeRight, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
//        canvas.drawPhysics(sensorShapeLeft, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        canvas.drawPhysics(hitboxShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);

        for (FlareModel f : flares) {
            f.drawDebug(canvas);
        }
    }

    /**
     * Set the current item the diver is carrying
     */
    public void setItem() {
        if (pickUpOrDrop) {

            for (ItemModel i : potential_items) {
                if (!item_list.contains(i)) {
                    item_list.add(i);
                    i.setX(getX());
                    i.setY(getY());
                    i.setCarried(true);
                    i.setKeyActive(true);
                    if(i.getItemType() == ItemModel.ItemType.KEY){
                        num_keys++;
                    }
                    break;
                }

            }
        }
    }

    /**
     * @return if the diver is carrying an item
     */
    public boolean carryingItem() {
        return item_list.size() > 0;
    }

    /**
     * @return the list of current items the diver is carrying
     */
    public ArrayList<ItemModel> getItem() {
        return item_list;
    }

    /**
     * @return the number of keys the diver is carrying
     */
    public int getNumKeys() {
        return num_keys;
    }

    /**
     * @return the number of keys the diver is carrying
     */
    public void reduceNumKeys() {
        num_keys--;
    }

    /**
     * @return the number of keys the diver is carrying
     */
    public void incrementNumKeys() {
        num_keys++;
    }

    /**
     * remove an item from diver inventory
     */
    public void removeItem(ItemModel i) {
        item_list.remove(i);
    }

    public void setPickUpOrDrop(boolean val) {
        pickUpOrDrop = val;
    }

    public void addPotentialItem(ItemModel i) {
        if (potential_items != null && !potential_items.contains(i)) {
            potential_items.add(i);
        }
    }

    public void removePotentialItem(ItemModel i) {
        potential_items.remove(i);
    }

    public boolean containsPotentialItem(ItemModel i) {
        return potential_items.contains(i);
    }


    public DeadBodyModel getDeadBody() {
        return dead_body;

    }

    public void printPotentialItems() {
        System.out.println("POTENTIAL ITEMS SIZE" + potential_items.size());
        for (ItemModel i : potential_items) {
            System.out.println("ID NUMBER: " + i.getID());
        }
    }

    public void setFlareTexture(TextureRegion f) {
        for (FlareModel flare : flares) {
            flare.setTexture(f);
        }
    }

    public void setFlareFilmStrip(FilmStrip f) {
        for (FlareModel flare : flares) {
            flare.setFilmStrip(f);
        }
    }

    public void initFlares(RayHandler rayHandler) {

        for (FlareModel f : flares) {
            f.initLight(rayHandler);
            f.setCarried(true);
            f.setWorldDrawScale(worldDrawScale);
            f.setActivated(false);
        }
    }

    public void dropFlare(boolean d) {
        active_flare = d;
    }

    public void updateFlare() {

        if (num_flares > 0 && active_flare) {
            FlareModel f = flares.get(num_flares - 1);
            if (flare_duration < MAX_FLARE_DURATION) {

                f.setActivated(true);
                f.setX((getX() + (25 / 32f * (float) Math.cos(getAngle()))) * worldDrawScale.x);
                f.setY((getY() + (25 / 32f * (float) Math.sin(getAngle()))) * worldDrawScale.y);
                f.setAngle(getAngle());
                flare_duration++;
//                System.out.println("FLARe IS ACTIVE");
            } else {
//                f.setActivated(false);
                f.setCarried(false);
                f.setX((getX() + (25 / 32f * (float) Math.cos(getAngle()))) * worldDrawScale.x);
                f.setY((getY() + (25 / 32f * (float) Math.sin(getAngle()))) * worldDrawScale.y);
                f.setAngle(0);
                f.setVX(0);
                f.setVY(0);
                num_flares--;
                System.out.println("FLARe IS DROPPED");
                active_flare = false;
                flare_duration = 0;

            }
        }

    }

    public void setBodyContact(boolean b) {
        contact_body = b;
        if (b) {
            diverState = 1;
            pickupFrame = 0;
            carrying_body = true;

        }
    }

    public ArrayList<FlareModel> getFlares() {
        return flares;
    }

    public boolean isBodyContact() {
        return contact_body;
    }

    /**
     * @return the current oxygen level of the diver
     */
    public float getOxygenLevel() {
        return oxygenLevel;
    }

    /**
     * @param delta
     */
    public void changeOxygenLevel(float delta) {
        float updatedOxygen = oxygenLevel + delta;
        oxygenLevel = Math.max(Math.min(updatedOxygen, maxOxygenLevel), 0);
    }

    public boolean isTouchingObstacle() {
        return isTouchingObstacle;
    }

    public void setTouchingObstacle(boolean isTouching) {
        isTouchingObstacle = isTouching;
    }

    /**
     * @return whether the player has latched onto the wall
     */
    public boolean isLatching() {
        return latchedOn;
    }

    /**
     * @param latched used to set whether the player has latched onto something
     */
    public void setLatching(boolean latched) {
        if (latched && !latchedOn)
            kickOffFrame = 0;

        latchedOn = latched;
    }

    public void setBoosting(boolean boost) {
        boosting = boost;
    }

    public boolean isBoosting() {
        return boosting;
    }

    public void boost() {
        // set impulse in direction of key input
        float boost = 3f;

        forceCache.set(facingDir.nor().x * boost, facingDir.nor().y * boost);
        body.applyLinearImpulse(forceCache, body.getWorldCenter(), true);
        forceCache.set(movement.nor().x * boost, movement.nor().y * boost);
        body.applyLinearImpulse(forceCache, body.getPosition(), true);
    }

//    public void dropItem() {
//        if (item_list != null) {
//            item_list.setGravityScale(0f);
//            item_list.setX(getX());
//            item_list.setY(getY());
//            item_list.setVerticalMovement(0);
//            item_list.setVX(0);
//            item_list.setVY(0);
//            item_list.setCarried(false);
//            item_list = null;
//        }
//    }

    public void dropBody() {
        carrying_body = false;
    }

    public void pickUpBody() {
        if (contact_body) {
            carrying_body = true;
        }
    }

    /**
     * Player Sensor Stuff
     */
    public void addTouching(String name, GObject obj) {

        if (obj instanceof Wall) {
            touchingRight.add(obj);

        }
        if (name.equals(sensorNameRight) && !touchingRight.contains(obj))
            touchingRight.add(obj);
        else if (name.equals(sensorNameLeft) && !touchingLeft.contains(obj))
            touchingLeft.add(obj);

        for (int i = 0; i < touchingRight.size(); i++) {
//            System.out.println("touching right " + touchingRight.get(i).getClass());
            if (touchingRight.get(i) instanceof ItemModel) {
                ItemModel tmp = (ItemModel) (touchingRight.get(i));
//                System.out.println("ID " + tmp.getID());
            }
        }
        for (int i = 0; i < touchingLeft.size(); i++) {
//            System.out.println("touching left " + touchingLeft.get(i).getClass());
            if (touchingLeft.get(i) instanceof ItemModel) {
                ItemModel tmp = (ItemModel) (touchingLeft.get(i));
//                System.out.println("ID " + tmp.getID());
            }
        }
    }

    public void removeTouching(String name, GObject obj) {
        if (name.equals(sensorNameRight))
            touchingRight.remove(obj);
        else if (name.equals(sensorNameLeft))
            touchingLeft.remove(obj);

    }


    @Override
    public void draw(GameCanvas canvas) {
        if (pickupFrame == 5 && dead_body != null) {
            oxygenLevel += dead_body.getOxygenRewarded();
            dead_body.setCarried(true);
            dead_body.setActive(false);
        }
        for (FlareModel f : flares) {
            f.draw(canvas);
        }

        tick++;
        float effect = faceRight ? 1.0f : -1.0f;
        float flip = bodyFlip ? -1.0f : 1.0f;
        float angle = getAngle();
        if (texture != null) {

            if (pickupFrame < 7) {
                if (tick % 3 == 0) {
                    pickupFrame++;
                }
                diverSprites.get(diverState).setFrame(pickupFrame + 40);
//                canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
            } else {
                if (isIdling()) {
                    if (tick % 4 == 0) {
                        idleFrame++;
                    }
                    if (idleFrame >= 16) {
                        idleFrame = 0;
                    }
                    diverSprites.get(diverState).setFrame(idleFrame + 24);
//                    canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);

                }/* else if (stunned) {
                    if (stunCooldown % 20 > 5) {

//                        canvas.draw(diverSprites.get(diverState), Color.RED, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect * 0.25f, 0.25f);
                    } else {
//                        canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect * 0.25f, 0.25f);
                    }
                } */ else {
                    if (turnFrames > 0 && turnFrames < 5) {
                        if (tick % 4 == 0) {
                            turnFrames--;
                        }
                        diverSprites.get(diverState).setFrame(turnFrames + 12);
//                        canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
                    } else if (kickOffFrame < 5) {
                        if (tick % 3 == 0) {

                            if (kickOffFrame < 3) {
                                kickOffFrame++;
                            } else if (kickOffFrame >= 3 && !isLatching()) {
                                kickOffFrame++;
                            }

                        }
                        diverSprites.get(diverState).setFrame(kickOffFrame + 18);
//                        canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
                    } else {
//                        canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
                    }
                }

            }
            canvas.draw(diverSprites.get(diverState), Color.WHITE, origin.x - 50, origin.y + 50,
                    getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f * worldDrawScale.x, flip * 0.25f * worldDrawScale.y);
        }
        if (ping || ping_cooldown > 0) {
//            canvas.draw(pingTexture, Color.WHITE, origin.x + pingDirection.x,
//                    origin.y + pingDirection.y, getX() * drawScale.x + 50, getY() * drawScale.y, getAngle(), 0.25f, 0.25f);
            ping_cooldown--;
        }
    }


//    public boolean atAngle() {
//        return currentAngle == targetAngle;
//    }

    public int getRemainingFlares() {
        if (num_flares > 0 && (!flares.get(num_flares - 1).isCarried() || flares.get(num_flares - 1).isActivated())) {
            return num_flares - 1;
        }
        return num_flares;
    }


    public void setChangeLightFilter(boolean b) {
        changeLightFilter = b;
    }

    public boolean getChangeLightFilter() {
        return changeLightFilter;
    }
    /**
     * Returns the angle corresponding to the direction of diver's movement
     * <p>
     * The value returned is in radians
     *
     * @return the angle of rotation for this body
     */
//    public float getAngle() {
//
//        if (!faceRight && !movement.isZero()) {
//            targetAngle = movement.angleRad() - (float) (Math.PI);
//        } else if (!movement.isZero()) {
//            targetAngle = movement.angleRad();
//        }
//        if (currentAngle > targetAngle + .1) {
//            float tmp = targetAngle - currentAngle;
//            currentAngle -= .1;
//        } else if (currentAngle < targetAngle - .1) {
//            currentAngle += .1;
//            float tmp = targetAngle - currentAngle;
//        }
//        return targetAngle;
//    }

}
