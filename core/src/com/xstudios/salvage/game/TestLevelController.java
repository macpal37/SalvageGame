package com.xstudios.salvage.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.models.DiverModel;

public class TestLevelController extends LevelController implements ContactListener {

public TestLevelController( ){
super();


}

class Diver {
    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    Body body;
    BodyDef bodyinfo;
    PolygonShape shape;

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    TextureRegion texture;

    public Diver(int x, int y, int width, int height){
        shape = new PolygonShape();
        bodyinfo = new BodyDef();
        bodyinfo.position.set (x,y);
        bodyinfo.fixedRotation = true;
        shape = new PolygonShape();
        shape.setAsBox(width/2f,height/2f);



    }

    public void draw(GameCanvas canvas){

        if (texture != null) {
            canvas.draw(texture, Color.WHITE,body.getPosition().x,body.getPosition().y,0,0,0.5f,0.5f);
        }
    }

    public  boolean activatePhysics(World world){
        body = world.createBody(bodyinfo);
        body.createFixture(shape, 1.0f);
        return true;
    }

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
        diver = new DiverModel(20, 10,diverTexture.getRegionWidth(),
            diverTexture.getRegionHeight());

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
        float diverWidth = diverTexture.getRegionWidth();
        float diverHeight = diverTexture.getRegionHeight();

        // add the diver
        diver = new DiverModel(0, 0, diverWidth, diverHeight);
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

        diver.setMovement(input.getHorizontal() *diver.getForce());
        diver.setLinearVelocity(new Vector2(input.getHorizontal(),input.getVertical()));

        diver.applyForce();
//        diver.setPosition(diver.getX()+0.01f,diver.getY());
        cameraController.render();
//        canvas.begin();
//        coolerDiver.draw(canvas);
//        canvas.end();
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
