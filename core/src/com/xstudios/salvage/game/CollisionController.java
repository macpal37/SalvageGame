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


public class CollisionController {
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
            diver.addPotentialItem(item);
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof ItemModel) {
            ((ItemModel) b1.getUserData()).setTouched(true);
            DiverModel diver = (DiverModel) b2.getUserData();
            ItemModel item = (ItemModel) b1.getUserData();
            diver.addPotentialItem(item);
        }
    }


    public void startDiverTreasureCollision(Fixture f1, Fixture f2) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof TreasureModel && f2.getUserData().equals("Treasure")) {
            DiverModel diver = (DiverModel) b1.getUserData();
            TreasureModel treasureModel = (TreasureModel) b2.getUserData();
            if (!treasureModel.isOpened()) {
                treasureModel.setNearChest(true);
                diver.getTreasureChests().add(treasureModel);
            }
        } else if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof TreasureModel && f1.getUserData().equals("Treasure")) {

            DiverModel diver = (DiverModel) b2.getUserData();
            TreasureModel treasureModel = (TreasureModel) b1.getUserData();
            if (!treasureModel.isOpened()) {
                treasureModel.setNearChest(true);
                diver.getTreasureChests().add(treasureModel);
            }

        }
    }

    public void endDiverTreasureCollision(Fixture f1, Fixture f2) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof TreasureModel && f2.getUserData().equals("Treasure")) {
            DiverModel diver = (DiverModel) b1.getUserData();
            TreasureModel treasureModel = (TreasureModel) b2.getUserData();
            if (!treasureModel.isOpened()) {
                treasureModel.setNearChest(false);
                diver.getTreasureChests().remove(treasureModel);
            }
        } else if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof TreasureModel && f1.getUserData().equals("Treasure")) {

            DiverModel diver = (DiverModel) b2.getUserData();
            TreasureModel treasureModel = (TreasureModel) b1.getUserData();
            if (!treasureModel.isOpened()) {
                treasureModel.setNearChest(false);
                diver.getTreasureChests().remove(treasureModel);
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
            ItemModel key = null;
            for (ItemModel i : diver.getItem()) {
                if (i.getItemType() == ItemModel.ItemType.KEY) {
                    key = i;
                    break;
                }
            }
            if (key != null) {
                diver.removeItem(key);
                diver.reduceNumKeys();
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
    public float startDiverHazardCollision(Fixture f1, Fixture f2, DiverModel diver, MonsterController monster) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();

        if (b1.getUserData() instanceof DiverModel &&
                diver.getDiverCollisionBox().equals(fd1) &&
                b2.getUserData() instanceof HazardModel) {
            HazardModel hazard = (HazardModel) b2.getUserData();
            return staticHazardCollision(diver, hazard, f2, (f2.getUserData() instanceof Tentacle), monster);
        }
        if (b2.getUserData() instanceof DiverModel &&
                diver.getDiverCollisionBox().equals(fd2) &&
                b1.getUserData() instanceof HazardModel) {
            HazardModel hazard = (HazardModel) b1.getUserData();
            return staticHazardCollision(diver, hazard, f1, (f1.getUserData() instanceof Tentacle), monster);
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
            if (wall.isWall())
                monster.addLocation(wall);

        } else if (b1.getUserData() instanceof Monster &&
                b2.getUserData() instanceof Wall) {
            Monster monster = (Monster) b1.getUserData();
            Wall wall = (Wall) b2.getUserData();
            if (wall.isWall())
                monster.addLocation(wall);
        }
    }

    public void startDiverMonsterCollision(Body b1, Body b2) {
        Object fd1 = b1.getUserData();
        Object fd2 = b2.getUserData();
        if (b1.getUserData() instanceof DiverModel &&
                b2.getUserData() instanceof Monster) {
            AudioController.getInstance().idle_roar();

        } else if (b1.getUserData() instanceof Monster &&
                b2.getUserData() instanceof DiverModel) {
            AudioController.getInstance().idle_roar();
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
            if (wall.isWall())
                monster.removeLocation(wall);
        } else if (b1.getUserData() instanceof Monster &&
                b2.getUserData() instanceof Wall) {
            Monster monster = (Monster) b1.getUserData();
            Wall wall = (Wall) b2.getUserData();
            if (wall.isWall())
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

        if (b1.getUserData() instanceof DiverModel && f1.getUserData().equals("DiverBox") && b2.getUserData() instanceof Wall) {

            Wall wall = (Wall) b2.getUserData();
            if (wall.isWall()) {
                diver.setTouchedWall(wall);
                diver.setTouchingObstacle(true);
            }
            // only collide with the actual wall if the actual body does, not the sensor
            if (!f1.isSensor() && wall.isCanAlertMonster()) {
                monsterController.wallCollision();
                AudioController.getInstance().wood_collision(diver.getForce());
            }
        } else if (b2.getUserData() instanceof DiverModel && f2.getUserData().equals("DiverBox") && b1.getUserData() instanceof Wall) {
            Wall wall = (Wall) b1.getUserData();
            if (wall.isWall()) {
                diver.setTouchedWall(wall);
                diver.setTouchingObstacle(true);
            }
            if (!f2.isSensor() && wall.isCanAlertMonster()) {
                monsterController.wallCollision();
                AudioController.getInstance().wood_collision(diver.getForce());
            }

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

        if (b1.getUserData() instanceof DiverModel && f1.getUserData().equals("DiverBox") && b2.getUserData() instanceof Wall
        ) {
            diver.setTouchedWall(null);
            ((DiverModel) b1.getUserData()).setTouchingObstacle(false);
        } else if (b2.getUserData() instanceof DiverModel && f2.getUserData().equals("DiverBox") && b1.getUserData() instanceof Wall) {
            diver.setTouchedWall(null);
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
//            System.out.println("GOALl1!");

            return diver.hasBody();
        }
        if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof GoalDoor) {
//            System.out.println("GOALl2!");
            return diver.hasBody();
        }
        return false;

    }

    /**
     * Handles diver collision with hazards
     *
     * @param f1 one of the colliding fixtures
     * @param f2 the other of the colliding fixtures
     */
    public void endDiverHazardCollision(Fixture f1, Fixture f2, DiverModel diver) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
//        System.out.println("END DIVER HAZ COLLISION");
        // if the diver is touching a hazard (excluding the extra sensors)
        if (b1.getUserData() instanceof DiverModel &&
                diver.getDiverCollisionBox().equals(fd1) &&
                b2.getUserData() instanceof HazardModel) {
            diver.setChangeLightFilter(true);
//            System.out.println("CHANGE LIGHT FILTER TRUE");
        }
        if (b2.getUserData() instanceof DiverModel &&
                diver.getDiverCollisionBox().equals(fd2) &&
                b1.getUserData() instanceof HazardModel) {
            diver.setChangeLightFilter(true);
//            System.out.println("CHANGE LIGHT FILTER TRUE");
        }
    }


    public static float staticHazardCollision(DiverModel diver, HazardModel hazard, Fixture fixture, boolean isTentacle, MonsterController monster) {
//        System.out.println("Hazard Contact: " + hazard.getOxygenDrain());
        System.out.println("START HAZARD COLLISION");
        if (!diver.getStunned() && /*!diver.isInvincible() && */ !monster.isKillState()) {
            diver.setStunned(true);
            diver.setStunCooldown(hazard.getStunDuration());
            diver.resetInvincibleTime();
            ;
//            diver.resetInvincibleTime();
        } else if (!diver.getStunned() && /*!diver.isInvincible() && */monster.isKillState()) {
            diver.setStunned(true);
            diver.setStunCooldown(hazard.getStunDuration());
            return (hazard.getOxygenDrain() * 4.0f);
        }

        if (isTentacle) {
            // TODO: @quimey you can add diver tentacle collision sounds in here
            Tentacle tentacle = (Tentacle) fixture.getUserData();
            if (tentacle.getType() == Tentacle.TentacleType.Idle) {
                tentacle.setStartGrowing(false);
                monster.transitionToAggravated(true);
            }
//            else if (tentacle.getType() == Tentacle.TentacleType.KILL){
//                return (hazard.getOxygenDrain() * 7.5f);
//            }
            //AudioController.getInstance().idle_roar();
        } else {
            AudioController.getInstance().metal_collision(diver.getForce());
        }
        diver.setChangeLightFilter(false);

        return hazard.getOxygenDrain();

    }


    /**
     * Handles the possible tentacle spawn points
     *
     * @param b1 one of the colliding bodies
     * @param b2 the other of the colliding bodies
     */
    public void startFlareTentacleCollision(Fixture b1, Fixture b2) {
        Object fd1 = b1.getUserData();
        Object fd2 = b2.getUserData();

        if (b1.getBody().getUserData() instanceof FlareModel &&
                b2.getUserData() instanceof Tentacle) {
            FlareModel flare = (FlareModel) b1.getUserData();
            Tentacle t = (Tentacle) b2.getUserData();
            t.setStartGrowing(false);
//            System.out.println("we got a flare folks");
//            }
        }
        if (b1.getUserData() instanceof Tentacle &&
                b2.getBody().getUserData() instanceof FlareModel) {
            Tentacle t = (Tentacle) b1.getUserData();
            t.setStartGrowing(false);
//            System.out.println("we got a flare folks");
        }
    }


    /**
     * Used to tell if the diver and flare are in range of each other
     *
     * @param b1
     * @param b2
     */

    public void startDiverFlare(Body b1, Body b2) {

        if (b1.getUserData() instanceof FlareModel) {
            if (b2.getUserData() instanceof DiverModel) {
                FlareModel f = (FlareModel) b1.getUserData();

                f.turnOffLight(.53f, 1f);
            }
        } else if (b2.getUserData() instanceof FlareModel) {
            if (b1.getUserData() instanceof DiverModel) {
                FlareModel f = (FlareModel) b2.getUserData();
                f.turnOffLight(.53f, 1f);
//                System.out.println("FLARE DIVER");

            }
        }
    }

    /**
     * Used to tell if the diver and flare are in range of each other
     *
     * @param b1
     * @param b2
     */
    public void endDiverFlare(Body b1, Body b2) {

        if (b1.getUserData() instanceof FlareModel) {
            if (b2.getUserData() instanceof DiverModel) {
                FlareModel f = (FlareModel) b1.getUserData();
                f.turnOnLight();
            }
        } else if (b2.getUserData() instanceof FlareModel) {
            if (b1.getUserData() instanceof DiverModel) {
                FlareModel f = (FlareModel) b2.getUserData();
                f.turnOnLight();
            }
        }
    }


    /**
     * Used to tell if the diver and flare are in range of each other
     *
     * @param b1
     * @param b2
     */

    public void startFlareFlare(Body b1, Body b2) {

        if (b1.getUserData() instanceof FlareModel) {
            if (b2.getUserData() instanceof FlareModel) {
                FlareModel f = (FlareModel) b1.getUserData();
                if (b1 != b2) {
                    f.turnOffLight(.2f, .8f);
                    FlareModel f2 = (FlareModel) b2.getUserData();

                    f2.turnOffLight(.2f, .8f);


//                System.out.println("FLARE Flare");
                }
            }
        }
    }


    /**
     * Used to tell if the diver and flare are in range of each other
     *
     * @param b1
     * @param b2
     */

    public void endFlareFlare(Body b1, Body b2) {

        if (b1.getUserData() instanceof FlareModel) {
            if (b2.getUserData() instanceof FlareModel) {
                if (b1 != b2) {
                    FlareModel f = (FlareModel) b1.getUserData();
                    f.turnOnLight();

                    FlareModel f2 = (FlareModel) b2.getUserData();

                    f2.turnOnLight();
//                System.out.println("end flare flare");
                }
            }
        }
    }


    public void startDiverTextCollision(Fixture f1, Fixture f2) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof TextModel && f2.getUserData().equals("Text")) {
            TextModel textModel = (TextModel) b2.getUserData();
            textModel.setTextActive(true);
            textModel.setDisplay(true);

        } else if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof TextModel && f1.getUserData().equals("Text")) {
            TextModel textModel = (TextModel) b2.getUserData();
            textModel.setTextActive(true);
            textModel.setDisplay(true);
        }
    }

    public void endDiverTextCollision(Fixture f1, Fixture f2) {
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();
        Object fd1 = f1.getUserData();
        Object fd2 = f2.getUserData();
        if (b1.getUserData() instanceof DiverModel && b2.getUserData() instanceof TextModel && f2.getUserData().equals("Text")) {
            TextModel textModel = (TextModel) b2.getUserData();
            textModel.setTextActive(false);
            textModel.setDisplay(false);

        } else if (b2.getUserData() instanceof DiverModel && b1.getUserData() instanceof TextModel && f1.getUserData().equals("Text")) {
            TextModel textModel = (TextModel) b2.getUserData();
            textModel.setTextActive(false);
            textModel.setDisplay(false);
        }


    }
}
