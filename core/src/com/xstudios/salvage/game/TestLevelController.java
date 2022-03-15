package com.xstudios.salvage.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.models.DiverModel;

public class TestLevelController extends LevelController  {


public TestLevelController(){


}

    @Override
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity() );
        world = new World(gravity,false);
        resetLevel();
    }

    private void resetLevel() {
        diver = new DiverModel(100,100);
        diver.setTexture(diverTexture);
        diver.setDrawScale(scale);
        diver.setName("diver");

        addObject(diver);
    }

    @Override
    public void update(float dt) {
        InputController input = InputController.getInstance();

    }
}
