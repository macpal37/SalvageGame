package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.xstudios.salvage.game.GameCanvas;

public class HazardModel extends Wall {
    /**
     * the amount of oxygen that this hazard drains per frame
     */
    private float oxygenDrain;

    /**
     * number of frames the stun lasts for
     */
    private float stunDuration;

    /**
     * get oxygen drain rate
     */
    public float getOxygenDrain() {
        return oxygenDrain;
    }

    /**
     * get stun duration
     */
    public float getStunDuration() {
        return stunDuration;
    }

    public void setOxygenDrain(float oxygenDrain) {
        this.oxygenDrain = oxygenDrain;
    }

    public void setStunDuration(float stunDuration) {
        this.stunDuration = stunDuration;
    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        for (int ii = 0; ii < shapes.length; ii++) {
            fixture.filter.categoryBits = 0x002;
            fixture.filter.groupIndex = 0x004;
            fixture.filter.maskBits = -1;
            fixture.shape = shapes[ii];
            geoms[ii] = body.createFixture(fixture);
        }

        markDirty(false);
    }

    public HazardModel(float[] points) {
        this(points, 0, 0);
    }

    public HazardModel(float points[], float x, float y) {
        super(points, x, y);
    }

    public Vector2 scale = new Vector2(1, 1);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }


    public void draw(GameCanvas canvas) {
        if (texture != null && !invisible) {
            canvas.draw(region, Color.WHITE, origin.x, origin.y, getX() * drawScale.x - origin.x, getY() * drawScale.y - origin.y, getAngle(), scale.x, scale.y);

        }

    }

}
