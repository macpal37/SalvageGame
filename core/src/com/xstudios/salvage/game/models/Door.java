package com.xstudios.salvage.game.models;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.xstudios.salvage.game.GameCanvas;

import java.util.Arrays;
import java.util.Optional;

public class Door extends Wall {


    private TextureRegion openDoor;
    private TextureRegion closedDoor;
    private boolean toUnlock;


    public Door(float[] points) {
        this(points, 0, 0);

    }

    public Door(float[] points, float x, float y) {
        super(points, x, y);
        toUnlock = false;
    }

    public void addTextures(TextureRegion closed, TextureRegion open) {
        openDoor = open;
        closedDoor = closed;
        origin.set(open.getRegionWidth() / 2.0f, open.getRegionHeight() / 2.0f);
    }

    public boolean isActive() {
        return body != null && body.isActive();
    }

    public void setUnlock(boolean unlock) {
        toUnlock = unlock;
    }

    public boolean getUnlock(ItemModel key) {
        if (key == null) {
            return false;
        }

        return toUnlock && key.isCarried() && key.getID() == getID();
    }

    public Vector2 doorScale = new Vector2();

    public void setDoorScale(float w, float h) {
        doorScale.set(w, h);
    }


    public void draw(GameCanvas canvas) {

        if (region != null) {
            if (openDoor != null && closedDoor != null) {
                float x = vertices[0];
                float y = vertices[1];


                if (isActive()) {
                    canvas.draw(closedDoor, ItemModel.COLOR_OPTIONS[getID()], 0, 0, x * drawScale.x, (y) * drawScale.y, getAngle(), doorScale.x, doorScale.y);
//                    canvas.draw(closedDoor, ItemModel.COLOR_OPTIONS[getID()], origin.x, 0, x * drawScale.x, (y) * drawScale.y + doorDimension.y / 2f, getAngle(), 1, 1);
                } else {
                    canvas.draw(openDoor, ItemModel.COLOR_OPTIONS[getID()], 0, 0, x * drawScale.x, (y) * drawScale.y, getAngle(), doorScale.x, doorScale.y);
                }
            }
        }

    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixtures
        for (int ii = 0; ii < shapes.length; ii++) {
            fixture.shape = shapes[ii];
            fixture.filter.categoryBits = 0x002;
            fixture.filter.groupIndex = 0x004;
            fixture.filter.maskBits = -1;
            geoms[ii] = body.createFixture(fixture);

        }
        markDirty(false);
    }

}
