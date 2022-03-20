package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.xstudios.salvage.game.models.DiverModel;

public class CollisionController {

    public void manageCollisions(Body b1, Body b2) {
//        switch (b1.getUserData()) {
//            case DiverModel:
//                break;
//
//        }
        System.out.println("USER DATA: " + b1.getUserData());

    }
}
