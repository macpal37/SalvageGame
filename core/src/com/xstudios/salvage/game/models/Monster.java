package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

import java.util.ArrayList;

public class Monster extends GameObject {

    private float maxAggression = 100;
    public float agression = 0;
    private ArrayList<Tentacle> tentacles;
    private float aggrivationRate = 0.75f;
    private final float RADIUS = 15;
    private CircleShape radialPresence;
    private FilmStrip tentacleSprite;
    /**
     * A cache value for the fixture (for resizing)
     */
    protected Fixture geometry;

    public Monster(float x, float y) {
        super(x, y);
        System.out.println("SUMMO!");
        radialPresence = new CircleShape();
        setFixedRotation(true);

        setDimension(RADIUS, RADIUS);
        setName("monster");

        tentacles = new ArrayList<>();
    }

    @Override
    protected void resize(float width, float height) {

    }


    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();
        // Create the fixture
        // create a sensor to detect wall collisions
        FixtureDef monsterDef = new FixtureDef();
        monsterDef.isSensor = true;
        // we don't want this fixture to collide, just act as a sensor
        monsterDef.filter.groupIndex = -1;
        radialPresence = new CircleShape();
        radialPresence.setRadius(RADIUS);
        monsterDef.shape = radialPresence;
        Fixture hitboxFixture = body.createFixture(monsterDef);
        hitboxFixture.setUserData("MonsterRadius");
        markDirty(false);
    }

    @Override
    protected void releaseFixtures() {

    }

    private int startingFrame = 0;


    public void setTentacleSprite(FilmStrip value) {
        tentacleSprite = value;
        tentacleSprite.setFrame(11);
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(tentacleSprite, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.5f, 0.5f);


    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(radialPresence, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }


    public void spawnTenctacle(DiverModel diver) {
        Tentacle tentacle = new Tentacle(diver.getX(), diver.getY());
        tentacle.setFilmStrip(tentacleSprite.copy());

    }

    public void addTentacle(Tentacle tentacle) {
        tentacles.add(tentacle);
    }

    public void removeTentacle(Tentacle tentacle) {
        tentacles.remove(tentacle);
    }

    public ArrayList<Tentacle> getTentacles() {
        return tentacles;
    }

    public void setAggrivation(float temp_aggrivation) {
        aggrivationRate = temp_aggrivation;
    }

    public float getAggrivation() {
        return aggrivationRate;
    }
}

