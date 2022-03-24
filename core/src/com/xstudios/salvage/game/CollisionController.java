package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Door;
import com.xstudios.salvage.game.models.ItemModel;
import com.xstudios.salvage.game.models.Wall;
import com.xstudios.salvage.util.PooledList;

public class CollisionController {


    public void startContact(Body b1, Body b2) {

        // set contact with wall
        startDiverToObstacle(b1, b2);

        if (b1.getUserData().getClass() == DiverModel.class) {
            DiverModel d1 = (DiverModel) b1.getUserData();
            if (b2.getUserData().getClass() == ItemModel.class) {
                if(d1.getItem() !=  b2.getUserData()) {
                    d1.addPotentialItem((ItemModel) b2.getUserData());
                }
            }
        }
//        System.out.println("USER DATA: " + b1.getUserData().getClass());
//        System.out.println("USER DATA: " + b1.getUserData());

    }

    public void endContact(Body b1, Body b2) {

        // end contact with wall
        endDiverToObstacle(b1, b2);

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

    /**
     * adds item to list of potential items for diver to pick up
     * @param diver diver object
     * @param item item that diver's colliding with
     */
    public static void pickUp(DiverModel diver, ItemModel item){
        if(diver.getItem() !=  item) {
            diver.addPotentialItem(item);
        }
    }

    /**
     * Checks if the diver's ending contact with the item
     * @param diver diver object
     * @param item item object to potentially put down
     */
    public static void putDown(DiverModel diver, ItemModel item){
        if(diver.containsPotentialItem(item)){
            diver.removePotentialItem(item);
            item.setVX(0);
            item.setVY(0);
        }
    }

    /**
     * Used to tell the body when it has collided with an obstacle to do
     * kickpoints.
     *
     * @param b1
     * @param b2
     */
    public void startDiverToObstacle(Body b1, Body b2) {
        if (b1.getUserData().getClass() == DiverModel.class && b2.getUserData().getClass() == Wall.class) {
//            System.out.println("body collided");
            ((DiverModel) b1.getUserData()).setTouchingObstacle(true);
        }
        if (b2.getUserData() == DiverModel.class && b1.getUserData().getClass() == Wall.class) {
//            System.out.println("body collided");
            ((DiverModel) b2.getUserData()).setTouchingObstacle(true);
        }
    }

    /**
     * Used to tell the body when it has stopped colliding with an obstacle
     * to do kickpoints.
     *
     * @param b1
     * @param b2
     */
    public void endDiverToObstacle(Body b1, Body b2) {
        if (b1.getUserData().getClass() == DiverModel.class && b2.getUserData().getClass() == Wall.class) {
            ((DiverModel) b1.getUserData()).setTouchingObstacle(false);
        }
        if (b2.getUserData() == DiverModel.class && b1.getUserData().getClass() == Wall.class) {
            ((DiverModel) b2.getUserData()).setTouchingObstacle(false);
        }
    }
}
