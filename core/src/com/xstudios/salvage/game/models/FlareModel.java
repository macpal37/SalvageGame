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
import com.xstudios.salvage.util.FilmStrip;

public class FlareModel extends DiverObjectModel {


    /**
     * The current horizontal movement of the item
     */
    private Vector2 movement;


    private PointLight light;
    private PointLight redLight;

    private FilmStrip flareSprite;

    private int FLARE_LIGHT_RADIUS = 10;
    private int MIN_LIGHT_RADIUS = 1;
    private float LIGHT_RADIUS_SCALE = .1f;

    private Color light_color;
    private Color white_light;

    private float white_alpha_val = .4f;
    private float color_alpha_val = .6f;

    private boolean isActivated;
    private float MAX_SPEED = .7f;

    private RayHandler rayHandler;

    public static final Color[] COLOR_OPTIONS = {Color.BLUE, Color.RED, Color.CHARTREUSE, Color.CYAN};
    Color item_color;

    private CircleShape radialPresence;

    public FlareModel(JsonValue data) {
        this(0, 0, data);
    }

    public FlareModel(float x, float y, JsonValue data) {

        super(x, y, data);

        try {
            item_color = COLOR_OPTIONS[getID()];
        } catch (Exception e) {
            item_color = Color.WHITE;
        }
        setFriction(data.getFloat("flare_friction", 2));
        setFixedRotation(false);
        movement = new Vector2();
        light_color = new Color(1f, 0.5f, 0.5f, color_alpha_val);//Color.BLACK;
        white_light = new Color(1f, 1f, 1f, white_alpha_val);
        setCarried(true);
        drawScale.set(40, 40);
        isActivated = false;
        shape.setAsBox(.27f, .05f, new Vector2(-.15f, 0), 0);
    }


    @Override
    public void setID(int id) {
        super.setID(id);
        item_color = COLOR_OPTIONS[getID()];
        setName("flare" + getID());
    }

    public void initLight(RayHandler rayHandler) {
//        System.out.println("INITIALIZE LIGHT");
        this.rayHandler = rayHandler;
        // White flickering light
        light = new PointLight(rayHandler, 100, white_light, 1, 0, 0);
        Filter f = new Filter();
        f.categoryBits = 0x0002;
        f.maskBits = 0x0004;
        f.groupIndex = 1;
        light.setContactFilter(f);

        light.setActive(false);
        // Red flare glow!
        redLight = new PointLight(rayHandler, 100, light_color, FLARE_LIGHT_RADIUS * 3, 0, 0);
        redLight.setContactFilter(f);

        redLight.setActive(false);
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void setActivated(boolean b) {
        isActivated = b;
        if (!b) {
            light.setActive(false);
            redLight.setActive(false);
        }
    }

    public void removeLights() {
        if (light != null)
            light.remove();

        if (redLight != null)
            redLight.remove();
    }

    public boolean isActivated() {
        return isActivated;
    }


//    public Color getColor() {
//        return ItemModel.COLOR_OPTIONS[getID()];
//    }

    /**
     * Release the fixtures for this body, resetting the shape
     * <p>
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
        fixture.filter.maskBits = 0x004;
        fixture.filter.groupIndex = 5;
        fixture.filter.categoryBits = 0x001;
        fixture.shape = shape;

        geometry = body.createFixture(fixture);

        // Create the fixture
        // create a sensor to detect wall collisions
        FixtureDef monsterDef = new FixtureDef();
        monsterDef.isSensor = true;
        // we don't want this fixture to collide, just act as a sensor
        monsterDef.filter.groupIndex = -1;
        radialPresence = new CircleShape();
        radialPresence.setRadius(FLARE_LIGHT_RADIUS * LIGHT_RADIUS_SCALE);
        monsterDef.shape = radialPresence;
        Fixture hitboxFixture = body.createFixture(monsterDef);
        hitboxFixture.setUserData("FlareRadius");

        markDirty(false);
    }

    public boolean activatePhysics(World world) {

        if (!super.activatePhysics(world)) {
            return false;
        }

        body.setGravityScale(.5f);
        body.setUserData(this);
        return false;
    }

    /**
     * Sets the object texture for drawing purposes.
     * <p>
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value the object texture for drawing purposes.
     */
    public void setTexture(TextureRegion value) {
        texture = value;
        origin.set(texture.getRegionWidth() / 2.0f, texture.getRegionHeight() / 2.0f);
    }

    public float getLightRadius() {
        if (isActivated) {
            return light.getDistance();
        } else {
            return 0;
        }
    }


    @Override
    public void draw(GameCanvas canvas) {

        if (isActivated || !carried) {
            if (!carried) {
//                    if (FLARE_LIGHT_RADIUS > MIN_LIGHT_RADIUS) {
//                        FLARE_LIGHT_RADIUS--;
//                    }
                //Light Flickering
                if (tick % 20 > 10) {
                    light.setDistance(light.getDistance() + 0.01f);
                } else {
                    light.setDistance(light.getDistance() - 0.01f);
                }
                if (tick % 20 == 0 && redLight.getDistance() > FLARE_LIGHT_RADIUS) {
                    redLight.setDistance(redLight.getDistance() - 1f);
                }
                if (tick % 100 == 0 && redLight.getDistance() >= MIN_LIGHT_RADIUS && redLight.getDistance() <= FLARE_LIGHT_RADIUS) {
                    redLight.setDistance(redLight.getDistance() - 0.5f);
                }
                if (redLight.getDistance() > MIN_LIGHT_RADIUS) {
                    if (tick % 5 == 0) {
                        int frame = flareSprite.getFrame();

                        frame++;
                        if (frame >= flareSprite.getSize())
                            frame = 0;
                        flareSprite.setFrame(frame);
                    }
                }

            }
            if (!isCarried())
                canvas.draw(flareSprite, Color.WHITE, origin.x + 50, origin.y + 50, getX() * drawScale.x, getY() * drawScale.y, getAngle() - (float) Math.PI / 2, .36f, .36f);
            light.setPosition(getX() / worldDrawScale.x, getY() / worldDrawScale.y);
//            light.setPosition(getX() + 25 / 32f, getY() - 10 / 32f);
            light.setActive(true);
            redLight.setPosition(light.getX(), light.getY());
            redLight.setActive(true);


        }
    }


    public int getFrame() {
        return flareSprite.getFrame();

    }

    @Override
    public void setCarried(boolean b) {
        carried = b;
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(shape, Color.YELLOW, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
        canvas.drawPhysics(radialPresence, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }

    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    protected void resize(float width, float height) {
        // Make the box with the center in the center
        vertices[0] = -width / 2.0f;
        vertices[1] = -height / 2.0f;
        vertices[2] = -width / 2.0f;
        vertices[3] = height / 2.0f;
        vertices[4] = width / 2.0f;
        vertices[5] = height / 2.0f;
        vertices[6] = width / 2.0f;
        vertices[7] = -height / 2.0f;
        shape.setAsBox(width, height);
    }

    public void applyForce() {
        if (!isActive() || carried) {
            return;
        }
        float desired_xvel = 0;
        float desired_yvel = 0;
        float max_impulse_drift = 2f;

        desired_xvel = getVX() + Math.signum(getHorizontalMovement()) * max_impulse_drift;
        desired_xvel = Math.max(Math.min(desired_xvel, MAX_SPEED), -MAX_SPEED);
        desired_yvel = getVY() + Math.signum(getVerticalMovement()) * max_impulse_drift;

        desired_yvel = Math.max(Math.min(desired_yvel, MAX_SPEED), -MAX_SPEED);

        float xvel_change = desired_xvel - getVX();
        float yvel_change = desired_yvel - getVY();

        float x_impulse = body.getMass() * xvel_change;
        float y_impulse = body.getMass() * yvel_change;

        body.applyForce(x_impulse, y_impulse, body.getWorldCenter().x,
                body.getWorldCenter().y, true);

//        forceCache.x = getHorizontalMovement();
//        forceCache.y = getVerticalMovement();
//        body.applyForce(forceCache, getPosition(), true);
//        setHorizontalMovement(0);
//        setVerticalMovement(0);
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
     * <p>
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getHorizontalMovement() {
        return movement.x;
    }

    /**
     * Returns up/down movement of this character.
     * <p>
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

    public PointLight getLight() {
        return light;
    }

    public void turnOffLight(float white_color_percent, float color_percent) {
//        light_color.a = .6f;
        light_color.a = color_percent * color_alpha_val;
        white_light.a = white_color_percent * white_alpha_val;
//        float temp_color = light_color.a * color_percent;
//        if(temp_color <= 0){
//            light_color.a = .05f;// color_alpha_val;
//        } else {
//            light_color.a = temp_color;
//        }
//
//        float temp_white = white_light.a * white_color_percent;
//        if(temp_white <= 0){
//            white_light.a = .05f;// color_alpha_val;
//        } else {
//            white_light.a = temp_white;
//        }

//        white_light.a *= white_color_percent;
//        light_color.a *= color_percent;
        light.setColor(white_light);
        redLight.setColor(light_color);
    }

    public void turnOnLight() {
        float temp_color = light_color.a * 1.6f;
        if (temp_color > color_alpha_val) {
            light_color.a = color_alpha_val;
        } else {
            light_color.a = temp_color;
        }

        float temp_white = white_light.a * 1.6f;
        if (temp_white > white_alpha_val) {
            white_light.a = white_alpha_val;
        } else {
            white_light.a = temp_white;
        }

        light.setColor(white_light);
        redLight.setColor(light_color);
    }

    public void setFilmStrip(FilmStrip value) {
        flareSprite = value;
        flareSprite.setFrame(0);
    }

}
