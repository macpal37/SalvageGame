package com.xstudios.salvage.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.GifDecoder;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;
import com.xstudios.salvage.util.PooledList;
import sun.security.x509.OtherName;

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


    private FilmStrip diverSprite;

    private int current_frame = 0;

    public int getFrame() {
        return diverSprite.getFrame();

    }

    private final int DIVER_IMG_FLAT = 6;

    public void setFilmStrip(FilmStrip value) {
        diverSprite = value;
        diverSprite.setFrame(DIVER_IMG_FLAT);
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
    private ItemModel current_item;

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
    private PolygonShape sensorShapeRight;
    private PolygonShape sensorShapeLeft;
    private PolygonShape hitboxShape;

    /**
     * Whether you are touching another GameObject
     */
    private ArrayList<GObject> touchingRight;
    private ArrayList<GObject> touchingLeft;

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

        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
        setLinearDamping(data.getFloat("damping", 0));
        setMass(1);
        setFixedRotation(true);

        swimMaxSpeed = data.getFloat("maxspeed", 0);
        drift_maxspeed = data.getFloat("drift_maxspeed", 0);

        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);

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
        setDimension(1.2f, 0.5f);
        setMass(1);
        resetMass();
        setName("diver");

        this.data = data;
        current_item = null;
        ping = false;
        movement = new Vector2();
        drift_movement = new Vector2();
        oxygenLevel = data.getInt("max_oxygen", (int) maxOxygenLevel);
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
        boostedMaxSpeed = swimMaxSpeed * 3;
        maxSpeed = swimMaxSpeed;
        swimDamping = damping;

        boostDamping = damping / 100;

        setFixedRotation(false);
        carrying_body = false;
        dead_body = null;
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

    public boolean activatePhysics(World world) {

        if (!super.activatePhysics(world)) {
            return false;
        }
        body.setUserData(this);


        JsonValue sensorjv = data.get("sensor");
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density", 0);
        sensorDef.isSensor = true;
        sensorDef.filter.groupIndex = -1;
        sensorShapeRight = new PolygonShape();

        sensorShapeRight.setAsBox(sensorjv.getFloat("width", 0), sensorjv.getFloat("shrink", 0) * getWidth() / 2.0f,
                new Vector2(getWidth() + getWidth() / 2, 0), 0.0f);
        sensorDef.shape = sensorShapeRight;


        FixtureDef sensorDef2 = new FixtureDef();
        sensorDef2.density = data.getFloat("density", 0);
        sensorDef2.isSensor = true;
        sensorDef2.filter.groupIndex = -1;
        sensorShapeLeft = new PolygonShape();

        sensorShapeLeft.setAsBox(sensorjv.getFloat("width", 0), sensorjv.getFloat("shrink", 0) * getWidth() / 2.0f,
                new Vector2(-getWidth() - getWidth() / 2, 0), 0.0f);
        sensorDef2.shape = sensorShapeLeft;
        // Ground sensor to represent our feet
        Fixture sensorFixture2 = body.createFixture(sensorDef2);
        sensorFixture2.setUserData(getSensorNameLeft());


        // create a sensor to detect wall collisions
        FixtureDef hitboxDef = new FixtureDef();
        hitboxDef.density = data.getFloat("density", 0);
        hitboxDef.isSensor = true;
        // we don't want this fixture to collide, just act as a sensor
        hitboxDef.filter.groupIndex = -1;
        hitboxShape = new PolygonShape();
        hitboxShape.setAsBox(getWidth() * 1.6f, getHeight() * 2.4f,
                new Vector2(0, 0), 0.0f);
        hitboxDef.shape = hitboxShape;
        Fixture hitboxFixture = body.createFixture(hitboxDef);
        hitboxFixture.setUserData(hitboxSensorName);

        return true;
    }

    public String getHitboxSensorName() {
        return hitboxSensorName;
    }

    public String getDiverCollisionBox() {
        return diverCollisionBox;
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
        fixture.filter.categoryBits = 0x002;
        fixture.filter.groupIndex = 0x004;
        fixture.filter.maskBits = -1;

        geometry = body.createFixture(fixture);
        geometry.setUserData(getDiverCollisionBox());
        markDirty(false);
    }


    // draw the ping


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
        return !isLatching() && !isBoosting() && movement.len() != 0;
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

    public void applyForce() {


        float targetAngle = targetAngleX + ((targetAngleX == 0) ? targetAngleY : -targetAngleY);
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

        if (dist > 0) {
            body.setAngularVelocity(angle);
        } else if (dist < 0) {
            body.setAngularVelocity(-angle);
        } else {
            body.setAngularVelocity(0.0f);
        }
        float tinyBuffer = 5f;

//        if (getDynamicAngle() != 269 || getDynamicAngle() != 271 || getDynamicAngle() != 89 || getDynamicAngle() != 91) {
        if (movement.x != 0 || movement.y != 0) {
            if (getDynamicAngle() <= 90 - tinyBuffer || getDynamicAngle() > 270 + tinyBuffer) {
                bodyFlip = !faceRight;
            }
            if (getDynamicAngle() > 90 + tinyBuffer && getDynamicAngle() <= 270 - tinyBuffer) {
                bodyFlip = faceRight;
            }
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


            // set custom max speed and damping values
            setMaxSpeed(swimMaxSpeed);
            setLinearDamping(swimDamping);

            int frame = diverSprite.getFrame();
            if (tick % 5 == 0) {
                frame++;
                if (frame >= diverSprite.getSize() / 2)
                    frame = 0;
                diverSprite.setFrame(frame);
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
//                targetAngleY += (targetAngleY > 89) ? 0 : 5;

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

            if (tick % 10 == 0) {
                int frame = diverSprite.getFrame();

                if (frame > 8) {
                    frame = 7;
                    stroke = true;
                }
                frame += (stroke) ? -1 : 1;
                if (frame < 6) {
                    frame = 7;
                    stroke = false;
                }

                diverSprite.setFrame(frame);
            }
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

            // TODO: Currently doesn't take movement input. Will need steering in specific dirs only?
            if (Math.abs(getVX()) >= getMaxSpeed()) {
                setVX(Math.signum(getVX()) * getMaxSpeed());
            }
            if (Math.abs(getVY()) >= getMaxSpeed()) {
                setVY(Math.signum(getVY()) * getMaxSpeed());
            }
        }

    }

    @Override
    public void drawDebug(GameCanvas canvas) {

        canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        canvas.drawPhysics(sensorShapeRight, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        canvas.drawPhysics(sensorShapeLeft, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        canvas.drawPhysics(hitboxShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);

    }

    /**
     * Set the current item the diver is carrying
     */
    public void setItem() {
        if (pickUpOrDrop) {
            if (current_item != null) {

                current_item.setGravityScale(0f);
                current_item.setX(getX());
                current_item.setY(getY());
                current_item.setVerticalMovement(0);
                current_item.setVX(0);
                current_item.setVY(0);
                dropItem();
            } else if (potential_items.size() > 0) {

                current_item = potential_items.get(0);

                current_item.setX(getX());
                current_item.setY(getY());
                //current_item.setGravityScale(1);
                current_item.setCarried(true);
            }
        }
    }

    /**
     * @return if the diver is carrying an item
     */
    public boolean carryingItem() {
        return current_item != null;
    }

    /**
     * @return the current item the diver is carrying
     */
    public ItemModel getItem() {
        return current_item;
    }

    public void setPickUpOrDrop(boolean val) {
        pickUpOrDrop = val;
    }

    public void addPotentialItem(ItemModel i) {
        potential_items.add(i);
    }

    public void removePotentialItem(ItemModel i) {
        potential_items.remove(i);
    }

    public boolean containsPotentialItem(ItemModel i) {
        return potential_items.contains(i);
    }

    public void setBodyContact(boolean b) {
        contact_body = b;
        if (b) {
            carrying_body = true;
        }
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
        if (latched)
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
        forceCache.set(movement.nor().x * 20, movement.nor().y * 20);
        body.applyLinearImpulse(forceCache, body.getPosition(), true);
    }

    public void dropItem() {
        current_item.setCarried(false);
        current_item = null;
        potential_items.clear();
    }

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

        if (name.equals(sensorNameRight) && !touchingRight.contains(obj))
            touchingRight.add(obj);
        else if (name.equals(sensorNameLeft) && !touchingLeft.contains(obj))
            touchingLeft.add(obj);
    }

    public void removeTouching(String name, GObject obj) {
        if (name.equals(sensorNameRight))
            touchingRight.remove(obj);
        else if (name.equals(sensorNameLeft))
            touchingLeft.remove(obj);

    }

    public boolean isTouching() {

        if (faceRight)
            return touchingRight.size() > 0;
        else return touchingLeft.size() > 0;
    }

    @Override
    public void draw(GameCanvas canvas) {
        tick++;
        float effect = faceRight ? 1.0f : -1.0f;
        float flip = bodyFlip ? -1.0f : 1.0f;

        if (texture != null) {

            if (stunned) {
                if (stunCooldown % 20 > 5) {

                    canvas.draw(diverSprite, Color.RED, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect * 0.25f, 0.25f);
                } else {
                    canvas.draw(diverSprite, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect * 0.25f, 0.25f);
                }
            } else {

                float angle = getAngle();
                if (turnFrames > 0 && turnFrames < 5) {
                    if (tick % 4 == 0) {
                        turnFrames--;
                    }
                    diverSprite.setFrame(turnFrames + 12);
                    canvas.draw(diverSprite, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
                } else if (kickOffFrame < 6) {
                    if (tick % 3 == 0) {
                        kickOffFrame++;
                    }
                    diverSprite.setFrame(kickOffFrame + 18);
                    canvas.draw(diverSprite, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
                } else {
                    canvas.draw(diverSprite, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, angle, effect * 0.25f, flip * 0.25f);
                }

            }
        }
        if (ping || ping_cooldown > 0) {
            canvas.draw(pingTexture, Color.WHITE, origin.x + pingDirection.x,
                    origin.y + pingDirection.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.25f, 0.25f);
            ping_cooldown--;
        }
    }

}
