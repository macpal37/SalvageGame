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
import com.xstudios.salvage.audio.AudioController;
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
         * The monster is roaring before final attack
         */
        ROARING,
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
    private int MAX_POUNCE_TIME = 10;

    private float MAX_IDLE_TENTACLES = 10;
    private float MAX_ATTACK_TENTACLES = 5;


    private float MAX_TARGET_DIST = 1;
    private float RAND_DIST_RANGE = 20;
    private float ATTACK_DIST_RANGE = 6;

    private int MAX_INVINCIBILITY = 50;

    private PooledList<Vector2> targetLocations;

    private ArrayList<Tentacle> tentacles = new ArrayList<>();

    private float aggrivation_threshold = 16.0f;

    private boolean hasRoared;
    private boolean isRoaring;

    private AudioController audio;

    private Vector2 target_pos;
    private Vector2 curr_pos;

    private int temp_tick;
    private float roar_pause;

    public boolean isMonsterActive() {
        return monster.isActive();
    }

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
        target_pos = new Vector2(bounds.x / 2, bounds.y / 2);
        curr_pos = new Vector2(bounds.x / 2, bounds.y / 2);
        this.bounds = bounds;
        hasRoared = false;
        isRoaring = false;
        roar_pause = 0;
    }

    public void setAudio(AudioController a) {
        audio = a;
    }

    public Monster getMonster() {
        return monster;
    }

    public boolean isKillState() {
        return state == FSMState.ATTACK;
    }

    public void wallCollision() {
        if (monster != null) {
            float agg = monster.getAggravation();
            if (agg < monster.getAggroLevel() + 1) {
                monster.setAggravation(agg + monster.getAggravationRate());
            }
        }

    }

    /**
     * Change the state of the monster based on aggravation levels
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        float aggravation = monster.getAggravation();

        System.out.println("aggravation: " + monster.getAggravation() + " threshold " + monster.getAggroLevel());
        // Next state depends on current state.
        switch (state) {


            case IDLE:
                if (aggravation > monster.getAggroLevel()) {
                    state = FSMState.GONNA_POUNCE;
                    pounce_time = 0;
                }
                break;

            case GONNA_POUNCE:
                if (pounce_time > MAX_POUNCE_TIME) {
                    state = FSMState.AGGRIVATED;
                    monster.setAggressiveLength((int) (MAX_INVINCIBILITY * aggravation / aggrivation_threshold));
                } else {
                    pounce_time++;
                }
                break;

            case AGGRIVATED:
                if (aggravation <= monster.getAggroLevel() || monster.getAggressiveLength() <= 0 && state != FSMState.ROARING) {
//                    monster.reduceInvincibilityTime();
                    state = FSMState.IDLE;

                } else if (aggravation > (monster.getAggroLevel() * 20.0f) && aggravation > 15.0f) {

                    state = FSMState.ATTACK;
                }
//               else if (aggravation > 10.0f) {
//                    state = FSMState.ATTACK;
//               }
                else {
                    monster.reduceAggressiveLength();
                }
                break;

            case ATTACK:
                monster.setAggravation(100000.0f);
                break;


            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.IDLE; // If debugging is off
                break;
        }
//        System.out.println("STATE " + state);
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
     * Change the state of the monster based on aggravation levels
     */
    public void update(float aggravationDrain, DiverModel diver) {
        tick++;
        if (tick % 50 == 0) {
            if (monster.getAggravation() > 0.0f && state != FSMState.GONNA_POUNCE) {
                float aggravation = monster.getAggravation() - 0.5f;
                monster.setAggravation(aggravation);
            }
        }

//

        changeStateIfApplicable();
//        System.out.println(state);

        float goal_x = diver.getX() + diver.getVX();
        float goal_y = diver.getY() + diver.getVY();

        switch (state) {

            case IDLE:
                monster.moveMonster(curr_pos);
                if (tick % 5 == 0) {
                    if (curr_pos.dst(target_pos) < MAX_TARGET_DIST) {
                        int ctr = 0;
                        while (curr_pos.dst(target_pos) < 10 && ctr < 10) {
                            Random rand = new Random();
                            int x_change = rand.nextInt(2) * 2 - 1;
                            int y_change = rand.nextInt(2) * 2 - 1;
                            float xpos = rand.nextFloat() * RAND_DIST_RANGE * x_change;
                            float ypos = rand.nextFloat() * RAND_DIST_RANGE * y_change;
                            target_pos = diver.getPosition().cpy().add(xpos, ypos);
                            ctr++;
                        }
//                        System.out.println("////////////////////////////////////////////////////");
                    } else {
                        curr_pos = (target_pos.cpy().sub(curr_pos).nor()).add(curr_pos);
                    }

                    if (tick % 250 == 0) {
                        if (monster.getIdleTentacles().size() < MAX_IDLE_TENTACLES) {
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
                }
                break;
            case AGGRIVATED:
                if (tick % 5 == 0) {
                    if (curr_pos.dst(target_pos) < MAX_TARGET_DIST) {
                        int ctr = 0;
                        while (curr_pos.dst(target_pos) < 10 && ctr < 10) {
                            Random rand = new Random();
                            float xpos = rand.nextFloat() * ATTACK_DIST_RANGE;
                            float ypos = rand.nextFloat() * ATTACK_DIST_RANGE;
                            target_pos = diver.getPosition().cpy().add(xpos, ypos);
                            ctr++;
                        }
//                        System.out.println("rippppppppppppppppppppppppppppppppppppp");
                    } else {
                        curr_pos = (target_pos.cpy().sub(curr_pos).nor()).add(curr_pos);
//                        System.out.println("sadddddddddddddddddddddddddddddddddd");
                    }
                }
                if (tick % 100 == 0) {
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
                        //monster.setAggravation(0.0f);
                    }
                }
                break;

            case GONNA_POUNCE:
                audio.attack_roar();


            case ATTACK:
                if (!hasRoared) {
                    roar_pause = tick;
                    audio.loud_roar_play(hasRoared);
                    monster.setVisionRadius(10);
                    diver.setStunned(true);
                    diver.setStunCooldown(500);
                    hasRoared = true;
                    roar_pause = tick;
                    monster.setAggravation(100000.0f);
                } else if (tick - roar_pause > 500) {
                    monster.setAggravation(100000.0f);
                    diver.changeOxygenLevel(2);
                    monster.moveMonster(diver.getPosition());
                    audio.chase();
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
                        //monster.setAggravation(0.0f);
                    }
                }
                break;
            default:

        }

//        System.out.println("CURR_POS " + curr_pos);
//        System.out.println("TARGET POS " + target_pos);
//        System.out.println("DIVER POS " + diver.getPosition());
    }


}
