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

import java.util.ArrayList;

public class TreasureModel extends ObstacleModel {

    public enum TreasureType {
        Key, Monster, Flare
    }

    public Tentacle getTrap() {
        return trap;
    }

    public void setTrap(Tentacle trap) {
        trap.setMaxLifeSpan(10);
//        trap.setStartGrowing(false);
        System.out.println("TEN POS: " + trap.getPosition());
        this.trap = trap;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        trap.setPosition(getX(), getY());
        trap.setAngle((float) (getAngle() + Math.PI));
    }

    public Tentacle trap;

    public TreasureType getContents() {
        return contents;
    }

    public void setContents(TreasureType contents) {
        this.contents = contents;
    }

    private TreasureType contents;

    private FilmStrip idleSprite;

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
    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        light.remove();
    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();
        for (int ii = 0; ii < shapes.length; ii++) {
            fixture.filter.categoryBits = 0x004;
            fixture.filter.groupIndex = 0x002;
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


    public void setIdleSprite(FilmStrip value) {
        idleSprite = value;
        idleSprite.setFrame(0);
    }

    public void setTreasureType(TreasureType contents, FilmStrip treasureOpenAnimation) {
        sprite = treasureOpenAnimation;
        this.contents = contents;
        sprite.setFrame(0);
    }


    /**
     * Determines whether a key or monster appears.
     */
    public void openChest() {
        idleSprite.setFrame(1);
        opened = true;
    }

    int tick = 0;

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(treasureRadius, Color.RED, getX(), getY(), drawScale.x, drawScale.y);

//        if (opened && trap != null)
//            trap.drawDebug(canvas);
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


        if (idleSprite.getFrame() < 13) {
            if (opened && tick % 6 == 0) {
                idleSprite.setFrame(idleSprite.getFrame() + 1);
            }
            canvas.draw(idleSprite, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), scale.x, scale.y);
        } else {
            canvas.draw(sprite, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), scale.x, scale.y);

            switch (contents) {
                case Key:
                    if (tick % 6 == 0)
                        if (sprite.getFrame() < 40)
                            sprite.setFrame(sprite.getFrame() + 1);

                    break;
                case Monster:
//                    trap.draw(canvas);
                    if (tick % 2 == 0)
                        if (sprite.getFrame() < 36)
                            sprite.setFrame(sprite.getFrame() + 1);
                    if (sprite.getFrame() == 34)
                        trap.setStartGrowing(true);
                    break;
                case Flare:
                    if (tick % 4 == 0)
                        if (sprite.getFrame() < 13)
                            sprite.setFrame(sprite.getFrame() + 1);
                    break;
            }


        }


    }
}
