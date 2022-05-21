package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.xstudios.salvage.game.GameCanvas;

public class ObstacleModel extends Wall {


    public ObstacleModel(float[] points, float x, float y) {
        super(points, x, y);
        drift_movement = new Vector2(2f, 2f);
    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        for (int ii = 0; ii < shapes.length; ii++) {
            fixture.filter.maskBits = -1;
            fixture.shape = shapes[ii];
            geoms[ii] = body.createFixture(fixture);
        }


        markDirty(false);
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        for (PolygonShape tri : shapes) {
            canvas.drawPhysics(tri, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        }

    }

    private Vector2 drift_movement;
    private float maxSpeed;

    public float getMaxSpeed() {
        return maxSpeed;
    }


    public float getHorizontalDriftMovement() {
        return drift_movement.x;
    }

    public float getVerticalDriftMovement() {
        return drift_movement.y;
    }

    public void setDriftMovement(float x_val, float y_val) {

        drift_movement.x = x_val;
        drift_movement.y = y_val;


    }

    public void applyForce() {
        float desired_xvel = 0;
        float desired_yvel = 0;
        float max_impulse = 15f;
        float max_impulse_drift = 2f;

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
    }


    public Vector2 scale = new Vector2(1, 1);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }

    @Override
    public void draw(GameCanvas canvas) {

        float x = vertices[0];
        float y = vertices[1];

        float scaleZ = 32f;
        if (texture != null) {
            canvas.draw(texture, Color.WHITE, 0, 0, getX() * drawScale.x, getY() * drawScale.y, getAngle(),
                    scale.x * worldDrawScale.x, scale.y * worldDrawScale.y);


        }


    }
}
