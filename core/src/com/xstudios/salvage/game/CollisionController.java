package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.ItemModel;
import com.xstudios.salvage.util.PooledList;

public class CollisionController {



    public void startContact(Body b1, Body b2) {
        if (b1.getUserData().getClass() == DiverModel.class) {
            DiverModel d1 = (DiverModel) b1.getUserData();
            if (b2.getUserData().getClass() == ItemModel.class) {
                if(d1.getItem() !=  b2.getUserData()) {
                    d1.addPotentialItem((ItemModel) b2.getUserData());
                }
            }
        }
        System.out.println("USER DATA: " + b1.getUserData().getClass());
        System.out.println("USER DATA: " + b1.getUserData());

    }

    public void endContact(Body b1, Body b2) {
        if (b1.getUserData().getClass() == DiverModel.class) {
            DiverModel d1 = (DiverModel) b1.getUserData();
            if (b2.getUserData().getClass() == ItemModel.class) {
                if(d1.containsPotentialItem((ItemModel) b2.getUserData())) {
                    d1.removePotentialItem((ItemModel) b2.getUserData());
                    ((ItemModel)b2.getUserData()).setVX(0);
                    ((ItemModel)b2.getUserData()).setVY(0);
                }
            }
        }

    }
}
