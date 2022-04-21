package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;

public class ObstacleModel extends Wall {


    public ObstacleModel(float[] points, float x, float y) {
        super(points, x, y);

    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        for (int ii = 0; ii < shapes.length; ii++) {
//        fixture.filter.categoryBits = 0x002;
//        fixture.filter.groupIndex = 0x004;
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
            canvas.draw(texture, Color.WHITE, 0, 0, getX() * drawScale.x, getY() * drawScale.y, getAngle(), scale.x, scale.y);


        }


    }
}
