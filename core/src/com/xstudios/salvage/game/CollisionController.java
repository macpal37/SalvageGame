package com.xstudios.salvage.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.xstudios.salvage.audio.AudioController;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Door;
import com.xstudios.salvage.game.models.ItemModel;
import com.xstudios.salvage.game.models.Wall;

import com.xstudios.salvage.game.models.*;

import com.xstudios.salvage.util.PooledList;

public class CollisionController {

    AudioController audio;

    public void setAudio(AudioController a){
        audio = a;
    }
    /**
     * remove body from list of potential bodies that diver left and right sensors are touching
     *
     * @param diver diver object
     * @param fix1  fixture 1 in the collision
     * @param fix2  fixture 2 in the collision
     */
    public void removeDiverSensorTouching(DiverModel diver, Fixture fix1, Fixture fix2) {
        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();
        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();
        GObject bd1 = (GObject) body1.getUserData();
        GObject bd2 = (GObject) body2.getUserData();
        if ((diver.getSensorNameLeft().equals(fd2) && diver != bd1) ||
                (diver.getSensorNameLeft().equals(fd1) && diver != bd2)) {

            if (diver != bd1)
                diver.removeTouching(diver.getSensorNameLeft(), bd1);
            else
                diver.removeTouching(diver.getSensorNameLeft(), bd2);
        }
        if ((diver.getSensorNameRight().equals(fd2) && diver != bd1) ||
                (diver.getSensorNameRight().equals(fd1) && diver != bd2)) {

            if (diver != bd1)
                diver.removeTouching(diver.getSensorNameRight(), bd1);
            else
                diver.removeTouching(diver.getSensorNameRight(), bd2);

        }

    }

    /**
     * adds item to list of potential items for diver to pick up
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */

    public void startDiverItemCollision(Body b1, Body b2) {
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof ItemModel) {
            ((ItemModel) b2.getUserData()).setTouched(true);
            DiverModel diver = (DiverModel) b1.getUserData();
            ItemModel item = (ItemModel) b2.getUserData();
            if (diver.getItem() != item) {
                diver.addPotentialItem(item);
            }
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof ItemModel) {
            ((ItemModel) b1.getUserData()).setTouched(true);
            DiverModel diver = (DiverModel) b2.getUserData();
            ItemModel item = (ItemModel) b1.getUserData();
            if (diver.getItem() != item) {
                diver.addPotentialItem(item);
            }
        }
    }

    /**
     * Handle termination of contact of the diver with an item
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void endDiverItemCollision(Body b1, Body b2) {
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof ItemModel) {
            DiverModel diver = (DiverModel) b1.getUserData();
            ItemModel item = (ItemModel) b2.getUserData();
            putDown(diver, item);
            item.setTouched(false);
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof ItemModel) {
            DiverModel diver = (DiverModel) b2.getUserData();
            ItemModel item = (ItemModel) b1.getUserData();
            putDown(diver, item);
            item.setTouched(false);
        }

    }

    /**
     * Checks to see if the diver can unlock the door, if so return true
     *
     * @param diver diver object
     * @param door  door currently colliding with
     */

    public static boolean attemptUnlock(DiverModel diver, Door door) {
        if (diver.getItem() != null) {
            if (diver.getItem().getID() == door.getID() || diver.getItem().getItemType() == ItemModel.ItemType.DEAD_BODY) {

                return true;
            }
        }
        return false;
    }

    /**
     * handles collision between the diver and the door for unlocking
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void startDiverDoorCollision(Body b1, Body b2) {
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof Door) {
            Door door = (Door) b2.getUserData();
            DiverModel diver = (DiverModel) b1.getUserData();
            door.setUnlock(attemptUnlock(diver, door));
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof Door) {
            Door door = (Door) b1.getUserData();
            DiverModel diver = (DiverModel) b2.getUserData();
            door.setUnlock(attemptUnlock(diver, door));
        }
    }

    /**
     * handles collision between the diver and the dead body
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void startDiverDeadBodyCollision(Body b1, Body b2) {
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof DeadBodyModel) {
            ((DiverModel) b1.getUserData()).setBodyContact(true);
//            ((DeadBodyModel) b2.getUserData()).setActive(false);
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof DeadBodyModel) {
            ((DiverModel) b2.getUserData()).setBodyContact(true);
//            ((DeadBodyModel) b1.getUserData()).setActive(false);
        }
    }

    /**
     * handles termination of collision between the diver and the dead body
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void endDiverDeadBodyCollision(Body b1, Body b2) {
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof DeadBodyModel) {
            ((DiverModel) b1.getUserData()).setBodyContact(false);
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof DeadBodyModel) {
            ((DiverModel) b2.getUserData()).setBodyContact(false);
        }
    }

    /**
     * Handles diver collision with hazards
     *
     * @param f1 one of the colliding fixtures
     * @param f2 the other of the colliding fixtures
     */
    public float startDiverHazardCollision(Fixture f1, Fixture f2, DiverModel diver) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
        // if the diver is touching a hazard (excluding the extra sensors)
        if (b1.getUserData() instanceof DiverModel &&
                diver.getDiverCollisionBox().equals(fd1) &&
                b2.getUserData() instanceof HazardModel) {
            HazardModel hazard = (HazardModel) b2.getUserData();
            return staticHazardCollision(diver, hazard);
        }
        if (b2.getUserData() instanceof DiverModel &&
                diver.getDiverCollisionBox().equals(fd2) &&
                b1.getUserData() instanceof HazardModel) {
            HazardModel hazard = (HazardModel) b1.getUserData();
            return staticHazardCollision(diver, hazard);
        }
        // return 0 if not colliding
        return 0;
    }

    /**
     * Handles the possible tentacle spawn points
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void startMonsterWallCollision(Body b1, Body b2) {
        Object fd1 = b1.getUserData();
        Object fd2 = b2.getUserData();
        if (b1.getUserData() instanceof Wall &&
                b2.getUserData() instanceof Monster) {
            Wall wall = (Wall) b1.getUserData();
            Monster monster = (Monster) b2.getUserData();
            monster.addLocation(wall);

        }
        if (b1.getUserData() instanceof Monster &&
                b2.getUserData() instanceof Wall) {
            Monster monster = (Monster) b1.getUserData();
            Wall wall = (Wall) b2.getUserData();
            monster.addLocation(wall);
        }
    }

    /**
     * Handles the possible tentacle spawn points
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void endMonsterWallCollision(Body b1, Body b2) {
        Object fd1 = b1.getUserData();
        Object fd2 = b2.getUserData();
        if (b1.getUserData() instanceof Wall &&
                b2.getUserData() instanceof Monster) {
            Wall wall = (Wall) b1.getUserData();
            Monster monster = (Monster) b2.getUserData();
            monster.removeLocation(wall);
        }
        if (b1.getUserData() instanceof Monster &&
                b2.getUserData() instanceof Wall) {
            Monster monster = (Monster) b1.getUserData();
            Wall wall = (Wall) b2.getUserData();
            monster.removeLocation(wall);
        }
    }

    /**
     * Checks if the diver's ending contact with the item
     *
     * @param diver diver object
     * @param item  item object to potentially put down
     */
    public static void putDown(DiverModel diver, ItemModel item) {
        if (diver.containsPotentialItem(item)) {
            diver.removePotentialItem(item);
            item.setVX(0);
            item.setVY(0);
        }
    }

    /**
     * Used to tell the body when it has collided with an obstacle to do
     * kickpoints.
     *
     * @param f1
     * @param f2
     * @param diver
     */

    public void startDiverToObstacle(Fixture f1, Fixture f2, DiverModel diver, MonsterController monsterController) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();


        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof Wall) {

            diver.setTouchedWall((Wall) b2.getUserData());
            diver.setTouchingObstacle(true);

            //AudioController.getInstance().wall_collision(diver.getForce());
            monsterController.wallCollision();
            audio.wood_collision(diver.getForce());

        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof Wall) {
            diver.setTouchedWall((Wall) b1.getUserData());

            ((DiverModel) b2.getUserData()).setTouchingObstacle(true);
            monsterController.wallCollision();
            audio.wood_collision(diver.getForce());
        }

        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof Wall &&
                diver.getDiverCollisionBox().equals(fd1)) {
            audio.wall_collision(diver.getForce());
        } else if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof Wall &&
                diver.getDiverCollisionBox().equals(fd2)) {
            audio.wall_collision(diver.getForce());
        }

    }

    /**
     * Used to tell the body when it has stopped colliding with an obstacle
     * to do kickpoints.
     *
     * @param f1
     * @param f2
     * @param diver
     */
    public void endDiverToObstacle(Fixture f1, Fixture f2, DiverModel diver) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
        if (b1.getUserData() instanceof DiverModel && diver.getHitboxSensorName().equals(fd1) && b2.getUserData() instanceof Wall
        ) {
            ((DiverModel) b1.getUserData()).setTouchingObstacle(false);
        }
        if (b2.getUserData() instanceof DiverModel && diver.getHitboxSensorName().equals(fd2) && b1.getUserData() instanceof Wall) {
            ((DiverModel) b2.getUserData()).setTouchingObstacle(false);
        }
    }

    /**
     * return whether the diver is at the goal door or not
     *
     * @param b1    first colliding body
     * @param b2    second colliding body
     * @param diver diver object
     * @return whether win condition is met (diver is carrying body)
     */
    public boolean getWinState(Body b1, Body b2, DiverModel diver) {
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof GoalDoor) {
            System.out.println("GOALl1!");

            return diver.hasBody();
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof GoalDoor) {
            System.out.println("GOALl2!");
            return diver.hasBody();
        }
        return false;

    }


    public static float staticHazardCollision(DiverModel diver, HazardModel hazard) {
        System.out.println("Hazard Contact: " + hazard.getOxygenDrain());

        if (!diver.getStunned()) {
            diver.setStunned(true);
            diver.setStunCooldown(hazard.getStunDuration());
        }

        return hazard.getOxygenDrain();

    }
}
