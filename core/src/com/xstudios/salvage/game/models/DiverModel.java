package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;

public class DiverModel extends GameObject {
    /** The physics body for Box2D. */
//    protected Body body;


    /** The texture for the shape. */
//    protected TextureRegion texture;


    /** The texture origin for drawing */
//    protected Vector2 origin;


    /** Shape information for this box */
    protected PolygonShape shape;
    /** The physics shape of this object */
    private PolygonShape sensorShape;
    /** The width and height of the box */
    private Vector2 dimension;
    /** A cache value for when the user wants to access the dimensions */
    private Vector2 sizeCache;
    /** A cache value for the fixture (for resizing) */
    private Fixture geometry;
    /** Cache of the polygon vertices (for resizing) */
    private float[] vertices;
    /** The current horizontal movement of the character */
    private Vector2   movement;

    /** The factor to multiply by the input */
    private final float force;
    /** The amount to slow the character down */
    private final float damping;
    /** The maximum character speed */
    private final float maxspeed;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
    /** item that diver is currently carrying */
    private ItemModel current_item;
    /** whether user is pinging*/
    private boolean ping;
    /** whether user is pinging*/
    private Vector2 pingDirection;
    

    public DiverModel(JsonValue data, float width, float height){
        super(data.get("pos").getFloat(0),
                data.get("pos").getFloat(1));

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
        faceRight = true;;
        resize(width/4, height/4);
        resize(1, 1);
        setMass(1);
        resetMass();
        setName("diver");

        current_item = null;
        ping = false;
        movement = new Vector2();
    }
    public Body getBody() {
        return body;
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    private void resize(float width, float height) {
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


    public void setHorizontalMovement(float value) {
        movement.x = value;
        // Change facing if appropriate
        if (movement.x < 0) {
            faceRight = false;
        } else if (movement.x > 0) {
            faceRight = true;
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

    /**
     * Sets the ping direction for drawing purposes.
     *
     * @param bodypos the ping direction for drawing purposes.
     */
    public void setPingDirection(Vector2 bodypos) {
        pingDirection.set(bodypos).sub(getPosition());
    }

    public boolean activatePhysics(World world) {

        if (!super.activatePhysics(world)) {
            return false;
        }
        // Make a body, if possible
//        bodyinfo.active = true;
//        body = world.createBody(bodyinfo);
//        body.setUserData(this);

//        body.setFixedRotation(false);
//        body.setType(BodyDef.BodyType.DynamicBody);
        // Only initialize if a body was created.
//        if (body != null) {
//            createFixtures();
//            return true;
//        }
////
//        bodyinfo.active = true;
//        return true;
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
        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    @Override
    public void draw(GameCanvas canvas) {
//        body.applyAngularImpulse(1f,false);
//        System.out.println("Mass: " + body.getMass());
        float effect = faceRight ? 1.0f : -1.0f;
        effect =1;
        if (texture != null) {

            canvas.draw(texture, Color.WHITE,origin.x,origin.y,body.getPosition().x,body.getPosition().y,getAngle(),effect*0.5f,0.5f);

//            System.out.println(getX() + " " + getY());
//            canvas.draw(texture, Color.WHITE,origin.x, origin.y,
//                getX()*drawScale.x,origin.y /*getY()*drawScale.y*/,getAngle(),
//                effect*0.5f,0.5f);

        }
        if(ping) {
            // DRAW SOME SPRITE
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
    public float getMaxSpeed() {
        return maxspeed;
    }


//    public void applyUpwardForce(float dir){
//        body.applyForce(new Vector2(0,1000*dir),getPosition(),true);
//    }

    public void applyForce() {
        if (!isActive()) {
            return;
        }
        if (getHorizontalMovement() == 0f) {
            System.out.println("VX: " + body.getLinearVelocity().x);
            forceCache.x = -getDamping()*getVX();
            body.applyForce(forceCache,getPosition(),true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX())*getMaxSpeed());
        } else {
            forceCache.x = getHorizontalMovement();
        }

        if (Math.abs(getVY()) >= getMaxSpeed() &&
                Math.signum(getVY()) == Math.signum(getVerticalMovement())) {
            setVY(Math.signum(getVY())*getMaxSpeed());
        } else {
            forceCache.y = getVerticalMovement();
        }

        body.applyForce(forceCache,getPosition(),true);
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
//        canvas.drawPhysics(shape,Color.GREEN,origin.x, origin.y);
    }

    /**
     * Returns the x-coordinate for this physics body
     *
     * @return the x-coordinate for this physics body
     */
    public float getX() {
        return (body != null ? body.getPosition().x : super.getX());
    }

    /**
     * Returns the y-coordinate for this physics body
     *
     * @return the y-coordinate for this physics body
     */
    public float getY() {
        return (body != null ? body.getPosition().y : super.getY());
    }

    /**
     * Set the current item the diver is carrying
     */
    public void setItem(ItemModel i) {
        current_item = i;
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
}
