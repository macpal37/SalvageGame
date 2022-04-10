package com.xstudios.salvage.game.models;

import box2dLight.Light;
import box2dLight.RayHandler;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameController;
import com.xstudios.salvage.game.GameObject;

import static com.xstudios.salvage.game.models.ItemType.DEAD_BODY;
import static com.xstudios.salvage.game.models.ItemType.KEY;

public class ItemModel extends DiverObjectModel {

    /** Type of item*/
    private ItemType item_type;
    /** unique id of item*/
    private int item_ID;
    /** The current horizontal movement of the item */
    private Vector2 movement;

//    private RayHandler
    private Light light;

    private static final Color[] COLOR_OPTIONS = {Color.BLUE, Color.RED, Color.CHARTREUSE, Color.CYAN};
    Color item_color;


    public ItemModel(JsonValue data, float width, float height, ItemType item_type, int id){

        super(data);

        this.item_type = item_type;
        this.item_ID = id;
        try {
            item_color = COLOR_OPTIONS[item_ID];
        } catch (Exception e){
            item_color = Color.WHITE;
        }
        drawSymbolPos.add(data.getFloat("symbol_dist", 50.0f), 0);
        setName(item_type + "" + item_ID);
        movement = new Vector2();
    }

    public Color getColor() {
        return item_color;
    }
    /**
     * Release the fixtures for this body, resetting the shape
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
        fixture.filter.categoryBits = 0x002;
        fixture.filter.groupIndex = 0x004;
        fixture.filter.maskBits = -1;
        fixture.shape = shape;

        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    public boolean activatePhysics(World world) {

        if (!super.activatePhysics(world)) {
            return false;
        }

        body.setUserData(this);
        return false;
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
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            if(!carried){
                canvas.draw(texture, item_color, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.5f, 0.5f);
            }
            if(!carried&&isTouched)
            canvas.drawText("Press q",GameController.displayFont,(getX()-getWidth()*1.25f) * drawScale.x, (getY()+getHeight()*1.5f)  * drawScale.y);
        }
    }



    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
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

    public void applyForce() {
        if (!isActive()) {
            return;
        }
        forceCache.x = getHorizontalMovement();
        forceCache.y = getVerticalMovement();
        body.applyForce(forceCache,getPosition(),true);
        setHorizontalMovement(0);
        setVerticalMovement(0);
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


    public void setVerticalMovement(float value) {
        movement.y = value;
    }


    public void setHorizontalMovement(float value) {
        movement.x = value;
    }

    public ItemType getItemType() {
        return item_type;
    }

    public int getItemID() {
        return item_ID;
    }

//    public void setCarried(boolean b) {
//        carried = b;
//
//    }
//
//    public boolean isCarried() {
//        return carried;
//    }
//
//
//    public boolean isTouched() {
//        return isTouched;
//    }
//
//    public void setTouched(boolean touched) {
//        isTouched = touched;
//    }
}
