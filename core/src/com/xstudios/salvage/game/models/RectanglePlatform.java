package com.xstudios.salvage.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;

public class RectanglePlatform extends GameObject {




    public RectanglePlatform(int x, int y, int widt, int height ){


    }


    @Override
    public boolean activatePhysics(World world) {
        return false;
    }

    @Override
    public void deactivatePhysics(World world) {

    }

    @Override
    public void draw(GameCanvas canvas) {

    }

    @Override
    public void drawDebug(GameCanvas canvas) {

    }
}
