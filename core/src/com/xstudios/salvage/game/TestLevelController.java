package com.xstudios.salvage.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.models.DiverModel;

public class TestLevelController extends LevelController implements ContactListener {


    public TestLevelController( ){
    }

    @Override
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity() );
        for(GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }

        world = new World(gravity,false);
        resetLevel();
    }

    private void resetLevel() {
        diver = new DiverModel(20, 10);

        diver.setTexture(diverTexture);
        diver.setDrawScale(scale);
        diver.setName("diver");
        diver.setBodyType(BodyDef.BodyType.KinematicBody);
        addObject(diver);
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
//        float diverWidth = diverTexture.getRegionWidth();
//        float diverHeight = diverTexture.getRegionHeight();

        // add the diver
        diver = new DiverModel(0, 0);
        diver.setTexture(diverTexture);
        addObject(diver);

    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt	Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        InputController input = InputController.getInstance();


        cameraController.render();
    }


    /// CONTACT LISTENER METHODS
    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        // Call CollisionController to handle collisions
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.
     */
    public void endContact(Contact contact) {}

    /**
     * Handles any modifications necessary before collision resolution
     *
     * This method is called just before Box2D resolves a collision.
     */
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    private final Vector2 cache = new Vector2();

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}

}
