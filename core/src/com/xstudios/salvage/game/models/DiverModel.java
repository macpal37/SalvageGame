package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.PooledList;
import sun.security.x509.OtherName;

import java.util.ArrayList;

public class DiverModel extends GameObject {

    /** Shape information for this box */
    protected PolygonShape shape;
    /** The texture for the shape. */
    protected TextureRegion pingTexture;

    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;
    /** Cache of the polygon vertices (for resizing) */
    private float[] vertices;
    /** The movement of the character */
    private Vector2 movement;
    /** The movement of the character from currents */
    private Vector2 drift_movement;

    /** The factor to multiply by the input */
    private final float force;
    /** The amount to slow the character down */
    private final float damping;
    /** The maximum character speed */
    private final float maxspeed;
    /** The maximum character speed when drifting*/
    private final float drift_maxspeed;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
    /** item that diver is currently carrying */
    private ItemModel current_item;

    /** dead body that is the target for the level*/
    private DeadBodyModel dead_body;

    private boolean carrying_body;
    private boolean contact_body;

    /** All the itemModels diver is in contact with */
    protected ArrayList<ItemModel> potential_items  = new ArrayList<ItemModel>();

    /** whether user is pinging*/
    private boolean ping;
    /** ping cooldown */
    private int ping_cooldown;
    /** whether user is pinging*/
    private Vector2 pingDirection;
    private final int MAX_PING_COOLDOWN = 20;
    /** whether user wants to pick up/drop item*/
    private boolean pickUpOrDrop;

    /** Store oxygen level */
    private float oxygenLevel;
    private float MAX_OXYGEN = 150;

    /** Diver Sensor Used to pick up items and open doors*/

    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorNameRight;
    private final String sensorNameLeft;
    /** The physics shape of this object */
    private PolygonShape sensorShapeRight;
    private PolygonShape sensorShapeLeft;

    /** Whether you are touching another GameObject */
    private ArrayList<GObject> touchingRight;
    private ArrayList<GObject> touchingLeft;
//    private boolean isTouchingRight;
//    private boolean isTouchingLeft;

    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;


    // ==================== Player Body==============================

    /** The width and height of the box */
    private Vector2 dimension;
    /** A cache value for when the user wants to access the dimensions */
    private Vector2 sizeCache;




    // ======================== CONSTRUCTORS ================================
    /**
     *
     * @param data
     * @param width
     * @param height
     */

    public DiverModel(JsonValue data, float width, float height){
        super(data.get("pos").getFloat(0),
                data.get("pos").getFloat(1));

        shape = new PolygonShape();
        origin = new Vector2();
        body = null;
        vertices = new float[8];

        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
        setLinearDamping(data.getFloat("damping", 0));
        setMass(1);
        setFixedRotation(true);
        maxspeed = data.getFloat("maxspeed", 0);
        drift_maxspeed = data.getFloat("drift_maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);

        dimension = new Vector2();
        sizeCache = new Vector2();

        sensorNameRight = "DiverSensorRight";
        sensorNameLeft = "DiverSensorLeft";
        touchingRight = new ArrayList<>();
        touchingLeft = new ArrayList<>();
        // Initialize
        faceRight = true;
        setDimension(1,1);
        setMass(1);
        resetMass();
        setName("diver");

        this.data = data;
        current_item = null;
        ping = false;
        movement = new Vector2();
        drift_movement = new Vector2();
        oxygenLevel = MAX_OXYGEN;
        pingDirection = new Vector2();
        ping_cooldown = 0;

        carrying_body = false;
        dead_body = null;
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    protected void resize(float width, float height) {
        // Make the box with the center in the center
        vertices[0] = -width/2.0f;
        vertices[1] = -height/2.0f;
        vertices[2] = -width/2.0f;
        vertices[3] =  height/2.0f;
        vertices[4] =  width/2.0f;
        vertices[5] =  height/2.0f;
        vertices[6] =  width/2.0f;
        vertices[7] = -height/2.0f;
        shape.setAsBox(width,height);
    }

    public void setVerticalMovement(float value) {
        movement.y = value;
    }

    private boolean switchDir = false;



    public void setHorizontalMovement(float value) {
        movement.x = value;
        // Change facing if appropriate
        if (movement.x < 0) {
            faceRight = false;
        } else if (movement.x > 0) {
            faceRight = true;
        }
    }

    public void setDriftMovement(float x_val, float y_val) {
        if(movement.isZero()) {
            drift_movement.x = x_val;
            drift_movement.y = y_val;
        }
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
    public boolean hasItem(){
        return potential_items.size()>0;
    }

    public void setDeadBody(DeadBodyModel b) {
        dead_body = b;
    }

    public boolean hasBody() {
        return carrying_body;
    }

    public void updateDeadBodyPos() {
        if(dead_body!=null){
            dead_body.setX(getX());
            dead_body.setY(getY());
        }
    }
    public void setCarryingBody(boolean carryingPressed) {
        if(carryingPressed && contact_body) {
            carrying_body = true;
        } else if (carryingPressed) {
            carrying_body = false;
            updateDeadBodyPos();
        }
    }
    /**
     * Sets the object texture for drawing purposes.
     *
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value  the object texture for drawing purposes.
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
//        if(faceRight) {
//            pingDirection.sub(texture.getRegionWidth(), texture.getRegionHeight());
//        }
        pingDirection.nor();
        pingDirection.scl(getTexture().getRegionWidth());
    }

    public void setPing(boolean p) {
        ping = p;
        if(ping){
            ping_cooldown = MAX_PING_COOLDOWN;
        }
    }


    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {

       if(faceRight){
           return getSensorNameRight();
       }else{
           return  getSensorNameLeft();
       }

    }

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
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorDef.filter.groupIndex = -1;
        sensorShapeRight = new PolygonShape();

        sensorShapeRight.setAsBox( sensorjv.getFloat("width",0),sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                new Vector2(getWidth()+getWidth()/2,0), 0.0f);
        sensorDef.shape = sensorShapeRight;
//        sensorDef.filter.groupIndex=-1;
//        sensorDef.filter.maskBits =  0x0004;
//        sensorDef.filter.categoryBits =  0x0002;
        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorNameRight());



        FixtureDef sensorDef2 = new FixtureDef();
        sensorDef2.density = data.getFloat("density",0);
        sensorDef2.isSensor = true;
        sensorDef2.filter.groupIndex = -1;
        sensorShapeLeft = new PolygonShape();

        sensorShapeLeft.setAsBox( sensorjv.getFloat("width",0),sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                new Vector2(-getWidth()-getWidth()/2,0), 0.0f);
        sensorDef2.shape = sensorShapeLeft;
        // Ground sensor to represent our feet
        Fixture sensorFixture2 = body.createFixture( sensorDef2 );
        sensorFixture2.setUserData(getSensorNameLeft());
        return true;
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
        fixture.filter.categoryBits = 0x002;
        fixture.filter.groupIndex = 0x004;
        fixture.filter.maskBits = -1;
        geometry = body.createFixture(fixture);

        markDirty(false);
    }

    @Override
    public void draw(GameCanvas canvas) {

        float effect = faceRight ? 1.0f : -1.0f;

        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*0.25f,0.25f);

        }
        if(ping || ping_cooldown > 0) {
            canvas.draw(pingTexture, Color.WHITE,origin.x + pingDirection.x,
            origin.y + pingDirection.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),0.25f,0.25f);
            ping_cooldown--;
        }
    }


    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getHorizontalMovement() {
        return movement.x;
    }

    /**
     * Returns up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getVerticalMovement() {
        return movement.y;
    }


    /**
     * Returns how much force to apply to get the dude moving
     *
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
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed(boolean drift_movement) {
        if(drift_movement) {
            return drift_maxspeed;
        } else {
            return maxspeed;
        }
    }


    public void applyForce() {
        if (!isActive()) {
            return;
        }
        float desired_xvel = 0;
        float desired_yvel = 0;
        float max_impulse = 15f;
        float max_impulse_drift = 2f;
        if(movement.x != 0) {
            if(faceRight) {
                desired_xvel = Math.min(getVX() + max_impulse, getMaxSpeed(false));
            } else {
                desired_xvel = Math.max(getVX() - max_impulse, -getMaxSpeed(false));
            }
        } else if (movement.isZero()){
            if(faceRight) {
                desired_xvel = Math.min(getVX() + max_impulse_drift, getMaxSpeed(true));
            } else {
                desired_xvel = Math.max(getVX() - max_impulse_drift, -getMaxSpeed(true));
            }
        }
        if(movement.y > 0) {
            desired_yvel = Math.min(getVY() + max_impulse, getMaxSpeed(false));
        } else if (movement.y < 0) {
            desired_yvel = Math.max(getVY() - max_impulse, -getMaxSpeed(false));
        } else if(movement.isZero() && drift_movement.y > 0) {
            desired_yvel = Math.min(getVY() + max_impulse_drift, getMaxSpeed(true));
        } else if (movement.isZero() && drift_movement.y < 0) {
            desired_yvel = Math.max(getVY() - max_impulse_drift, -getMaxSpeed(true));
        }
        float xvel_change = desired_xvel - getVX();
        float yvel_change = desired_yvel - getVY();

        float x_impulse = body.getMass()*xvel_change;
        float y_impulse = body.getMass()*yvel_change;

        body.applyForce(x_impulse, y_impulse, body.getWorldCenter().x,
                body.getWorldCenter().y, true);
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(sensorShapeRight,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(sensorShapeLeft,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    /**
     * Set the current item the diver is carrying
     */
    public void setItem() {
//        System.out.println("SIZE OF POTENTIAL OBJECTS" + potential_items.size());
        if(pickUpOrDrop) {
            if(current_item!=null){
                System.out.println("SUPPOSED TO DROP OBJECT");
                current_item.setGravityScale(0f);
                current_item.setX(getX());
                current_item.setY(getY());
                current_item.setVerticalMovement(0);
                current_item.setVX(0);
                current_item.setVY(0);
                dropItem();
            }
            else if(potential_items.size() > 0) {
//                System.out.println("SUPPOSED TO PICK UP OBJECT");
                current_item = potential_items.get(0);
//                System.out.println("Current Item: "+current_item);
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
     *
     * @param delta
     */
    public void changeOxygenLevel(float delta) {
        float updatedOxygen = oxygenLevel + delta;
        oxygenLevel = Math.max(Math.min(updatedOxygen, MAX_OXYGEN), 0);
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
        if(contact_body) {
            carrying_body = true;
        }
    }

    /** Player Sensor Stuff*/
    public void addTouching(String name,GObject obj) {

        if(name.equals(sensorNameRight)&&!touchingRight.contains(obj))
        touchingRight.add(obj);
    else if(name.equals(sensorNameLeft)&&!touchingLeft.contains(obj))
            touchingLeft.add(obj);
}
    public void removeTouching(String name,GObject obj) {
        if(name.equals(sensorNameRight))
            touchingRight.remove(obj);
        else if(name.equals(sensorNameLeft))
            touchingLeft.remove(obj);

    }



    public boolean isTouching() {
        System.out.println("Right: "+touchingRight.size());
        System.out.println("Left: "+touchingLeft.size());

        if (faceRight)
            return touchingRight.size()>0;
            else return  touchingLeft.size()>0;
    }




}
