package com.xstudios.salvage.game.models;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.audio.AudioController;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameController;
import com.xstudios.salvage.util.FilmStrip;

import java.util.ArrayList;

public class TreasureModel extends ObstacleModel {

    public boolean isMayContainFlare() {
        return false;
    }

    boolean mayContainFlare;

    public void mayContainFlare(boolean value) {
        mayContainFlare = value;
    }

    public enum TreasureType {
        Key, Monster, Flare
    }

    /**
     * The key model
     */
    public ItemModel keyReward;
    /**
     * The tentacle model
     */
    public Tentacle trap;
    /**
     * whether the contents contain a key, monster, or flare
     */
    private TreasureType contents;
    /**
     * whether the chest has been opened or not
     */
    private boolean opened = false;

    private FilmStrip idleSprite;
    private FilmStrip suspenseSprite;
    private float openRadius = 5;
    private CircleShape treasureRadius;
    // doesn't the parent class already define this variable?
    private Vector2 origin;
    private Color lightColor;
    private Light light;
    boolean nearChest;


    public TreasureModel(float[] points, float x, float y, float ox, float oy, float div) {
        super(points, x + ox / div, y + oy / div);
        origin = new Vector2(ox, oy);
        lightColor = new Color(255 / 255f, 239 / 255f, 161 / 255f, 0.0f);
    }

    /**
     * Get the tentacle that spawns from the treasure chest
     *
     * @return
     */
    public Tentacle getTrap() {
        return trap;
    }

    public void setTrap(Tentacle t) {
        t.setActive(false);
        t.setStartGrowing(false);
        t.setGrowRate(10);
        this.trap = t;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        setTentacleSpawnPosition(0, -10f / 32f);

        switch (contents) {

            case Key:
                // if chest contains key and is opened and key anim done, toggle active
                // TODO: Should probably make the animation frames not magic numbers
                if (isOpened() && sprite.getFrame() == 39) {
                    keyReward.setKeyActive(true);
                }
                if (!keyReward.isKeyActive()) {
                    keyReward.updateChestLocation(this.getPosition());
                }
                break;

            case Flare:
                break;
            case Monster:
                break;
        }

    }

    public ItemModel getKeyReward() {
        return keyReward;
    }

    public void setKeyReward(ItemModel keyReward) {
        this.keyReward = keyReward;
    }

    public TreasureType getContents() {
        return contents;
    }

    public void setContents(TreasureType contents) {
        this.contents = contents;
    }

    public boolean isOpened() {
        return opened;
    }

    @Override
    public boolean activatePhysics(World world) {
        setGravityScale(0.5f);
//        if (contents == TreasureType.Key) {
//            System.out.println("KeyReward null?" + (keyReward == null));
//            keyReward.activatePhysics(world);
//        }
        return super.activatePhysics(world);
    }

    @Override
    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
//        if (keyReward != null)
//            keyReward.deactivatePhysics(world);
        light.remove();
    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();
        for (int ii = 0; ii < shapes.length; ii++) {
            fixture.filter.categoryBits = 0x008;
            fixture.filter.maskBits = 0x004;
            fixture.shape = shapes[ii];

            geoms[ii] = body.createFixture(fixture);
            geoms[ii].setUserData("Box");
        }
        FixtureDef treasureRadDef = new FixtureDef();
        treasureRadDef.isSensor = true;
        treasureRadDef.filter.maskBits = -1;
        treasureRadius = new CircleShape();
        treasureRadius.setRadius(openRadius);
        treasureRadDef.shape = treasureRadius;
        Fixture hitboxFixture = body.createFixture(treasureRadDef);
        hitboxFixture.setUserData("Treasure");

        markDirty(false);
    }


    public void setIdeSuspenseSprite(FilmStrip value, FilmStrip suspense) {
        idleSprite = value;
        suspenseSprite = suspense;
        suspense.setFrame(0);
        idleSprite.setFrame(0);
    }

    public void setTreasureType(TreasureType contents, FilmStrip treasureOpenAnimation) {
        sprite = treasureOpenAnimation;
        this.contents = contents;
        if (contents == TreasureType.Monster) {
            sprite.setFrame(32);
        } else {
            sprite.setFrame(0);
        }

    }

    /**
     * Determines whether a key or monster appears.
     */
    public void openChest() {
        idleSprite.setFrame(1);
        opened = true;
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(treasureRadius, Color.RED, getX(), getY(), drawScale.x, drawScale.y);

        CircleShape test_randomization = new CircleShape();
        test_randomization.setRadius(2);
        if (contents == TreasureType.Key) {
            canvas.drawPhysics(test_randomization, Color.PINK, this.getX(), this.getY(), drawScale.x, drawScale.y);
        } else if (contents == TreasureType.Flare) {
            canvas.drawPhysics(test_randomization, Color.PURPLE, this.getX(), this.getY(), drawScale.x, drawScale.y);
        } else if (contents == TreasureType.Monster) {
            canvas.drawPhysics(test_randomization, Color.BLUE, this.getX(), this.getY(), drawScale.x, drawScale.y);
        }

    }

    public void setNearChest(boolean flag) {
        nearChest = flag;
    }

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
        if (nearChest && suspenseSprite.getFrame() < 35) {
            light.setActive(true);
            if (lightColor.a < 0.4f)
                lightColor.add(0, 0, 0, 0.01f);

            canvas.drawText("Press X", GameController.displayFont, (getX() - getWidth() / 3 * 2) * drawScale.x,
                    (getY() + getHeight() / 3 * 2) * drawScale.y);
        } else if (lightColor.a > 0)
            lightColor.add(0, 0, 0, -0.01f);
        if (lightColor.a != 0 || lightColor.a != 0.5f)
            light.setColor(lightColor);
        float scaleZ = 32f;


        if (idleSprite.getFrame() < 13) {
            if (opened && tick % 6 == 0) {
                idleSprite.setFrame(idleSprite.getFrame() + 1);
            }
            canvas.draw(idleSprite, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x,
                    getY() * drawScale.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y);

        }
//        else if (suspenseSprite.getFrame() < 35) {
//            if (tick % 5 == 0)
//                suspenseSprite.setFrame(suspenseSprite.getFrame() + 1);
//            canvas.draw(suspenseSprite, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x,
//                    getY() * drawScale.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y);
//        }
        else {
            canvas.draw(sprite, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x,
                    getY() * drawScale.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y);

            switch (contents) {
                case Key:


                    if (tick % 6 == 0) {
                        if (sprite.getFrame() == 38)
                            keyReward.setActive(true);
                        if (sprite.getFrame() < 41)
                            sprite.setFrame(sprite.getFrame() + 1);
                    }

                    // call key's draw function
                    // only draw if active, ie the appearing animation is over
//                    if (keyReward.isActive()) {
//                        keyReward.draw(canvas);
//                    }

                    break;
                case Monster:

                    if (sprite.getFrame() < 35)
                        if (tick % 2 == 0) {
                            sprite.setFrame(sprite.getFrame() + 1);


                            if (sprite.getFrame() == 35) {

                                AudioController.getInstance().treasure_chest();
                            }

                        }

                    if (sprite.getFrame() == 33) {
                        trap.setActive(true);
                        trap.setStartGrowing(true);
                    }
                    break;
                case Flare:
                    if (tick % 4 == 0)
                        if (sprite.getFrame() < 13)
                            sprite.setFrame(sprite.getFrame() + 1);
                    break;
            }

        }

        if (trap != null)
            trap.draw(canvas);
        if (keyReward != null && !keyReward.isCarried())
            keyReward.draw(canvas);
        canvas.draw(texture, Color.WHITE, origin.x / scale.x, origin.y / scale.y, getX() * drawScale.x,
                getY() * drawScale.y, getAngle(), scale.x * worldDrawScale.x, scale.y * worldDrawScale.y);

    }
}
