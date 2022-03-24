package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.xstudios.salvage.game.models.*;
import com.xstudios.salvage.util.PooledList;

public class CollisionController {

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
     * adds item to list of potential items for diver to pick up
     * @param diver diver object
     * @param door door that diver's colliding with
     * @return whether win condition is met (diver is carrying body)
     */
    public static boolean winGame(DiverModel diver, GoalDoor door){
        return diver.getItem() != null;
//                && diver.getItem().getItemType() == ItemType.DEAD_BODY;
    }
}
