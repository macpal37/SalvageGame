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

public class DeadBodyModel extends DiverObjectModel {



    public DeadBodyModel(float x, float y,JsonValue data){
        super(x,y,data);
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
                canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.5f, 0.5f);
            }
            if(isTouched) {
                canvas.drawText("Press q", GameController.displayFont, (getX() - getWidth() * 1.25f) * drawScale.x, (getY() + getHeight() * 1.5f) * drawScale.y);
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

}
