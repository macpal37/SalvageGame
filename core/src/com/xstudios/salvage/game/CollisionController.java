package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Door;
import com.xstudios.salvage.game.models.ItemModel;
import com.xstudios.salvage.game.models.ItemType;
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
     * Checks to see if the diver can unlock the door, if so return true
     * @param diver diver object
     * @param door door currently colliding with
     */
    public static boolean attemptUnlock(DiverModel diver, Door door){
        if(diver.getItem()!=null) {
            if (diver.getItem() == door.getKey() || diver.getItem().getItemType() == ItemType.DEAD_BODY) {
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
}
