package com.xstudios.salvage.game.models;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameController;
import com.xstudios.salvage.util.FilmStrip;

public class TreasureModel extends ObstacleModel {


    public boolean isOpened() {
        return opened;
    }


    private boolean opened = false;

    private float openRadius = 5;

    private CircleShape treasureRadius;

    private Vector2 origin;


    private Color lightColor;

    public TreasureModel(float[] points, float x, float y, float ox, float oy, float div) {
        super(points, x + ox / div, y + oy / div);
        origin = new Vector2(ox, oy);
        lightColor = new Color(255 / 255f, 239 / 255f, 161 / 255f, 0.0f);

    }

    @Override
    public boolean activatePhysics(World world) {
        setGravityScale(0.5f);
        return super.activatePhysics(world);
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
            geoms[ii].setUserData("Box");
        }
        FixtureDef treasureRadDef = new FixtureDef();
        treasureRadDef.isSensor = true;
        // we don't want this fixture to collide, just act as a sensor
        treasureRadDef.filter.maskBits = -1;
        treasureRadius = new CircleShape();
        treasureRadius.setRadius(openRadius);
        treasureRadDef.shape = treasureRadius;
        Fixture hitboxFixture = body.createFixture(treasureRadDef);
        hitboxFixture.setUserData("Treasure");

        markDirty(false);
    }


    @Override
    public void setFilmStrip(FilmStrip value) {
        super.setFilmStrip(value);
        value.setFrame(0);
    }


    public void openChest() {
        sprite.setFrame(1);
        opened = true;
    }

    int tick = 0;

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(treasureRadius, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }

    boolean nearChest;

    public void setNearChest(boolean flag) {
        nearChest = flag;
    }

    private Light light;

    public void initLight(RayHandler rayHandler) {
        light = new PointLight(rayHandler, 100, lightColor, 10, getX(), getY());
        Filter f = new Filter();
        f.categoryBits = 0x0002;
        f.maskBits = 0x0004;
        f.groupIndex = 1;
        light.setContactFilter(f);
        light.setSoft(true);
        light.setActive(false);
    }


    @Override
    public void draw(GameCanvas canvas) {
        tick++;
        if (nearChest) {
            light.setActive(true);
            if (lightColor.a < 0.5f)
                lightColor.add(0, 0, 0, 0.01f);

            canvas.drawText("Press E", GameController.displayFont, (getX() - getWidth() / 3 * 2) * drawScale.x, (getY() + getHeight() / 3 * 2) * drawScale.y);
        } else if (lightColor.a > 0)
            lightColor.add(0, 0, 0, -0.01f);
        if (lightColor.a != 0 || lightColor.a != 0.5f)
            light.setColor(lightColor);
        float scaleZ = 32f;
        if (sprite != null) {
            if (opened && tick % 6 == 0) {
                sprite.setFrame(sprite.getFrame() + 1);
            }
            canvas.draw(sprite, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), scale.x, scale.y);
        }


    }
}
