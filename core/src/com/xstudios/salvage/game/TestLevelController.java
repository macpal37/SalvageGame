package com.xstudios.salvage.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.models.DiverModel;

public class TestLevelController extends LevelController  {


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
        diver = new DiverModel(20, 10,100,50);

        diver.setTexture(diverTexture);
        diver.setDrawScale(scale);
        diver.setName("diver");
        diver.setBodyType(BodyDef.BodyType.KinematicBody);

        addObject(diver);

    }

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
}
