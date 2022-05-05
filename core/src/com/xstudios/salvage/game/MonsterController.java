/*
 * AIController.java
 *
 * This class is an inplementation of InputController that uses AI and pathfinding
 * algorithms to determine the choice of input.
 *
 * NOTE: This is the file that you need to modify.  You should not need to
 * modify any other files (though you may need to read Board.java heavily).
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package com.xstudios.salvage.game;


import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.xstudios.salvage.game.models.*;
import com.xstudios.salvage.util.PooledList;

import java.util.ArrayList;
import java.util.Random;

/**
 * InputController corresponding to AI control.
 * <p>
 * REMEMBER: As an implementation of InputController you will have access to
 * the control code constants in that interface.  You will want to use them.
 */
public class MonsterController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /**
         * The monster is idle
         */
        IDLE,
        /**
         * The monster is transitioning to being aggravated and warning the player
         */
        GONNA_POUNCE,
        /**
         * The monster is aggravated
         */
        AGGRIVATED,
        /**
         * The monster is attacking the player
         */
        ATTACK
    }

    private Rectangle bounds;
    // Instance Attributes
    /**
     * The ship being controlled by this AIController
     */
    private Monster monster;
    /**
     * The ship's current state in the FSM
     */
    private FSMState state;
    /**
     * The ship's current state in the FSM
     */
    private float tick;

    private int pounce_time = 0;
    private int MAX_POUNCE_TIME = 50;

    private float MAX_IDLE_TENTACLES = 10;
    private float MAX_ATTACK_TENTACLES = 5;


    private float MAX_TARGET_DIST = 4;
    private float RAND_DIST_RANGE = 8;

    private int MAX_INVINCIBILITY = 50;

    private PooledList<Vector2> targetLocations;

    private ArrayList<Tentacle> tentacles = new ArrayList<>();

    private float aggrivation_threshold = 16.0f;

    private Vector2 target_pos;
    private Vector2 curr_pos;

    /**
     * Creates an AIController for the ship with the given id.
     *
     * @param monster the monster for the game
     */
    public MonsterController(Monster monster, Rectangle bounds) {
        this.monster = monster;
        targetLocations = new PooledList<>();
        targetLocations.push(monster.getPosition());
        tick = 0;
        state = FSMState.IDLE;
        target_pos = new Vector2();
        curr_pos = new Vector2();
        this.bounds = bounds;
    }


    public Monster getMonster() {
        return monster;
    }

    public void wallCollision() {
        float agg = monster.getAggrivation();
        if (agg < aggrivation_threshold) {
            monster.setAggrivation(agg + 3f);
        }
    }

    /**
     * Change the state of the monster based on aggrivation levels
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        float aggrivation = monster.getAggrivation();

        // Next state depends on current state.
        switch (state) {

            case IDLE:
                if (aggrivation > aggrivation_threshold) {
                    state = FSMState.GONNA_POUNCE;
                    pounce_time = 0;
                }
                break;
            case GONNA_POUNCE:
                if (pounce_time > MAX_POUNCE_TIME) {
                    state = FSMState.AGGRIVATED;
                    monster.setAggressiveLength((int) (MAX_INVINCIBILITY * aggrivation /aggrivation_threshold));
                } else {
                    pounce_time++;
                }
                break;

            case AGGRIVATED:
                if (aggrivation <= aggrivation_threshold || monster.getAggressiveLength() <= 0) {
//                    monster.reduceInvincibilityTime();
                    state = FSMState.IDLE;
                } else {
                    monster.reduceAggressiveLength();
                }
                break;

            //TODO: doesn't seem like the attack state is being used so commenting it out to avoid confusion
//            case ATTACK:
//                state = FSMState.AGGRIVATED;
//                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.IDLE; // If debugging is off
                break;
        }
        System.out.println("STATE " + state);
    }


    /**
     * Controls the movement of the monster
     */
    public void travelToPosition(Vector2 target) {
        float travelSpeed = 0.1f;
        Vector2 dist = new Vector2(target.x - monster.getPosition().x, target.y - monster.getPosition().y);
        float angle = dist.angleRad();
        Vector2 step = new Vector2(travelSpeed * (float) Math.cos(angle), travelSpeed * (float) Math.sin(angle));

        monster.setPosition(monster.getPosition().add(step));

    }


    /**
     * Change the state of the monster based on aggrivation levels
     */
    public void update(float aggrivationDrain, DiverModel diver) {
        tick++;
        System.out.println("AGGRIVATION " + monster.getAggrivation());
        if (tick % 50 == 0) {
            if (monster.getAggrivation() > 0.0f) {
                float aggrivation = monster.getAggrivation() - .2f;
                monster.setAggrivation(aggrivation);
            }
        }

        monster.moveMonster(diver.getPosition());
        changeStateIfApplicable();
//        System.out.println(state);

        float goal_x = diver.getX() + diver.getVX();
        float goal_y = diver.getY() + diver.getVY();

        switch (state) {

            case IDLE:
                if (tick % 250 == 0) {
                    if(curr_pos.dst(target_pos) <  MAX_TARGET_DIST){
                        Random rand = new Random();
                        float xpos = rand.nextFloat()*RAND_DIST_RANGE;
                        float ypos = rand.nextFloat()*RAND_DIST_RANGE;
                        target_pos = diver.getPosition().cpy().add(xpos, ypos);
                    } else {
                        curr_pos = (target_pos.cpy().sub(curr_pos).nor()).add(curr_pos);
                    }
                    if(monster.getIdleTentacles().size() < MAX_IDLE_TENTACLES) {
                        Wall final_loc = null;
                        for (Wall wall : monster.getSpawnLocations()) {
                            if (wall.canSpawnTentacle()) {
                                final_loc = wall;
                            }
                        }
                        if (final_loc != null) {
                            //System.out.println(final_loc);
                            monster.addIdleTentacle(final_loc);
                            //monster.setAggrivation(0.0f);
                        }
                    }
                }
                break;
            case AGGRIVATED:
                if (tick % 250 == 0) {
                    float best_distance = 10000.0f;
                    float temp_distance = 0.0f;
                    Wall final_loc = null;
                    for (Wall wall : monster.getSpawnLocations()) {
                        if (wall.canSpawnTentacle()) {
                            Vector2 location = wall.getPosition();
                            temp_distance = (float) Math.sqrt(
                                    Math.pow((double) (goal_x - location.x), 2) +
                                            Math.pow((double) (goal_y - location.y), 2)
                            );
                            if (temp_distance < best_distance) {
                                best_distance = temp_distance;
                                final_loc = wall;
                            }
                        }
                    }
                    if (final_loc != null) {
                        //System.out.println(final_loc);
                        monster.addTentacle(final_loc);
                        //monster.setAggrivation(0.0f);
                    }
                }
                break;
            default:

        }


    }


}
