package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.xstudios.salvage.game.GameCanvas;

public class Door extends Wall {
    private ItemModel key;

    private TextureRegion openDoor;
    private TextureRegion closedDoor;
    public Door(float[] points, ItemModel key) {
        this(points, 0, 0, key);
    }

    public Door(float[] points, float x, float y, ItemModel key) {
        super(points, x, y);
        this.key = key;
    }

    public void addTextures(TextureRegion closed, TextureRegion open){
        openDoor = open;
        closedDoor = closed;
        origin.set(open.getRegionWidth()/2.0f, open.getRegionHeight()/2.0f);
    }

    public ItemModel getKey() {
        return key;
    }

    public boolean isActive() {
        return body.isActive();
    }

    public void draw(GameCanvas canvas) {

        if (region != null) {
            if (openDoor!=null && closedDoor!=null) {
                float x = vertices[0]+1;
                float y = vertices[1]-2.5f;
                if (isActive()) {
                    canvas.draw(closedDoor, Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 0.8f, 0.8f);
                canvas.draw(region, Color.WHITE, 0, 0, (getX() - anchor.x) * drawScale.x, (getY() - anchor.y) * drawScale.y, getAngle(), 1, 1);
                } else {
                    canvas.draw(openDoor, Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 0.8f, 0.8f);
                canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);
                canvas.draw(region, Color.WHITE, 0, 0, (getX() - anchor.x) * drawScale.x, (getY() - anchor.y) * drawScale.y, getAngle(), 1, 1);
                canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);
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
    for(int ii = 0; ii < shapes.length; ii++) {
        fixture.shape = shapes[ii];
        fixture.filter.groupIndex = -1;
        geoms[ii] = body.createFixture(fixture);

    }
    markDirty(false);
}

}
