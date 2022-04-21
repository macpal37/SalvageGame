package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;

import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Door;
import com.xstudios.salvage.game.models.ItemModel;
import com.xstudios.salvage.game.models.Wall;



import com.xstudios.salvage.game.models.*;

import com.xstudios.salvage.util.PooledList;

public class CollisionController {


    public void startContact(Body b1, Body b2) {

        // set contact with wall
        startDiverToObstacle(b1, b2);

        if (b1.getUserData().getClass() == DiverModel.class) {
            DiverModel d1 = (DiverModel) b1.getUserData();
            if (b2.getUserData().getClass() == ItemModel.class) {
//                if(d1.getItem() !=  b2.getUserData()) {
                    d1.addPotentialItem((ItemModel) b2.getUserData());
//                }
            }
        }
    }

    public void endContact(Body b1, Body b2) {

        // end contact with wall
        endDiverToObstacle(b1, b2);
    }

    /**
     * adds item to list of potential items for diver to pick up
     * @param diver diver object
     * @param item item that diver's colliding with
     */
    public static void pickUp(DiverModel diver, ItemModel item){
        diver.printPotentialItems();
//        if(diver.getItem() !=  item) {
            diver.addPotentialItem(item);

//        }
    }

    /**
     * Checks to see if the diver can unlock the door, if so return true
     * @param diver diver object
     * @param door door currently colliding with
     */
    public static boolean attemptUnlock(DiverModel diver, Door door){
        if(diver.getItem()!=null) {
            if (diver.getItem().getID()== door.getID()) {
                return true;
            }
        }
        return false;
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
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof Wall) {
            ((DiverModel) b1.getUserData()).setTouchingObstacle(true);
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof Wall) {
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
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof Wall) {
            ((DiverModel) b1.getUserData()).setTouchingObstacle(false);
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof Wall) {
            ((DiverModel) b2.getUserData()).setTouchingObstacle(false);
        }
    }

    /**
     * adds item to list of potential items for diver to pick up
     * @param diver diver object
     * @param door door that diver's colliding with
     * @return whether win condition is met (diver is carrying body)
     */
    public static boolean winGame(DiverModel diver, GoalDoor door){
        return diver.hasBody();//diver.getItem() != null
//                && diver.getItem().getItemType() == ItemType.DEAD_BODY;
    }

    public static float staticHazardCollision(DiverModel diver, HazardModel hazard){
        if (!diver.getStunned()) {
            diver.setStunned(true);
            diver.setStunCooldown(hazard.getStunDuration());
        }
        return hazard.getOxygenDrain();

    }
}
