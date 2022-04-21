package com.xstudios.salvage.game.models;

import box2dLight.Light;
import box2dLight.PointLight;
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

import static com.xstudios.salvage.game.models.ItemType.KEY;

public class ItemModel extends DiverObjectModel {

    /** Type of item*/
    private ItemType item_type;

    /** The current horizontal movement of the item */
    private Vector2 movement;

    private Light light;

    private final int HOVER_LIGHT_RADIUS = 2;

    private Color light_color;

    public static final Color[] COLOR_OPTIONS = {Color.BLUE, Color.RED, Color.CHARTREUSE, Color.CYAN};
    Color item_color;


    public ItemModel(float x, float y, JsonValue data, ItemType item_type){

        super(x,y,data);

        this.item_type = item_type;

        try {
            item_color = COLOR_OPTIONS[getID()];
        } catch (Exception e){
            item_color = Color.WHITE;
        }
        setName(item_type + "" + getID());
        movement = new Vector2();
        light_color = new Color(1f,0.5f,0.5f,0.5f);
    }


    @Override
    public void setID(int id) {
        super.setID(id);
        item_color = COLOR_OPTIONS[getID()];
        setName(item_type + "" + id);
    }

    public void initLight(RayHandler rayHandler){

        light =  new PointLight(rayHandler,100, light_color, HOVER_LIGHT_RADIUS,getX(),getY());
        Filter f = new Filter();
        f.categoryBits = 0x0002;
        f.maskBits =0x0004;
        f.groupIndex = 1;
        light.setContactFilter(f);
        light.setSoft(true);
        light.setActive(false);
    }


    public Color getColor() {
        return ItemModel.COLOR_OPTIONS[getID()];
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
                canvas.draw(texture, ItemModel.COLOR_OPTIONS[getID()], origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.5f, 0.5f);
            }
            if(!carried&&isTouched){
                canvas.drawText("Press q",GameController.displayFont,(getX()-getWidth()*1.25f) * drawScale.x, (getY()+getHeight()*1.5f)  * drawScale.y);
                light.setPosition(getX(),getY());
                light.setActive(true);
            } else {
                light.setActive(false);
            }

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
