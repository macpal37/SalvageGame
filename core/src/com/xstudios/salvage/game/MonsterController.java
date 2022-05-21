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
//        /**
//         * The monster is transitioning to being aggravated and warning the player
//         */
//        GONNA_POUNCE,
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
    private int MAX_POUNCE_TIME = 60;

    private float MAX_IDLE_TENTACLES = 10;
    private float MAX_ATTACK_TENTACLES = 5;
    private float RANDOM_ATTACK_CHANGE = 0.00001f;

    private float MAX_IDLE_LENGTH = 700;
    private float curr_idle_length = 0;


    private float MAX_TARGET_DIST = 10;
    private float RAND_DIST_RANGE = 10;
    private float ATTACK_DIST_RANGE = 0;

    private int MAX_INVINCIBILITY = 50;

    private int total_aggressive_time = 0;
    private int last_aggression = 250;
    private int MAX_AGGRESSIVE_TIME;
    private int AGGRESSIVE_LENGTH = 15;
    private int LAST_AGGRESSIVE_LENGTH = 500;
    private boolean transition_to_aggravated = false;

    private PooledList<Vector2> targetLocations;

    private ArrayList<Tentacle> tentacles = new ArrayList<>();

    private float aggrivation_threshold = 16.0f;

    private boolean hasRoared;
    private boolean isRoaring;

    private AudioController audio;

    private Vector2 target_pos;
    private Vector2 curr_pos;

    private int temp_tick;
    private int attack_tick;
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
        monster.setAggressiveLength(AGGRESSIVE_LENGTH);

        MAX_AGGRESSIVE_TIME = monster.getAggressiveLength() * monster.getAggroStrikes();
        RANDOM_ATTACK_CHANGE = monster.getRandomAttackChance();
    }

    public void reset(){
        tick = 0;
        state = FSMState.IDLE;
        if(monster != null) {
            monster.setAggravation(0);
            monster.setAggressiveLength(AGGRESSIVE_LENGTH);
            MAX_AGGRESSIVE_TIME = monster.getAggressiveLength() * monster.getAggroStrikes();
        }
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

    public boolean isAggravated() {
        return state != FSMState.IDLE || monster.getAggravation() >= 14 * monster.getAggroLevel() / 15;
    }

    public void wallCollision() {
        if (monster != null && state != FSMState.AGGRIVATED) {
            float agg = monster.getAggravation();
//            if (agg < monster.getAggroLevel() + 1) {
            System.out.println("agg rate " + monster.getAggravationRate());
            monster.setAggravation(agg + monster.getAggravationRate());
//            }
        }

    }

    public void transitionToAggravated(boolean b) {
        transition_to_aggravated = b;
        System.out.println("TRANSITION TO AGGRAVATED");
    }

    /**
     * Change the state of the monster based on aggravation levels
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        float aggravation = monster.getAggravation();

//        System.out.println("aggravation: " + monster.getAggravation() + " threshold " + monster.getAggroLevel());
        // Next state depends on current state.
        switch (state) {

            case IDLE:
                curr_idle_length++;
                if (aggravation > monster.getAggroLevel() && last_aggression > LAST_AGGRESSIVE_LENGTH) {
                    AudioController.getInstance().attack_roar();
                    tick = 0;
                    pounce_time = 0;
                    monster.setAggressiveLength(AGGRESSIVE_LENGTH);
                    curr_idle_length = 0;
                    transition_to_aggravated = false;
                    state = FSMState.AGGRIVATED;
                    attack_tick++;
                    //monster.setVisionRadius(30);
                }
                else if ((transition_to_aggravated || ((tick % 25 == 0) && ((int)(Math.random()*1000) <= (int)(RANDOM_ATTACK_CHANGE)))) &&  last_aggression > LAST_AGGRESSIVE_LENGTH) {
                    AudioController.getInstance().attack_roar();
                    tick = 0;
                    pounce_time = 0;
                    curr_idle_length = 0;
                    monster.setAggravation(monster.getAggroLevel() + (monster.getAggravationRate() * 3));
                    transition_to_aggravated = false;
                    state = FSMState.AGGRIVATED;
                    attack_tick++;
                    //curr_idle_length = 0;
                }
                last_aggression++;
                break;

            case AGGRIVATED:
             //   System.out.println("Aggravation length " + monster.getAggressiveLength());
               // if (attack_tick > 10) {
                    if (aggravation <= monster.getAggroLevel() || monster.getAggressiveLength() <= 0) {
                        //                    monster.reduceInvincibilityTime();
                        state = FSMState.IDLE;
                        //monster.setVisionRadius(50);
                        monster.setAggravation((8 * monster.getAggravation()) / 10.0f);
                        last_aggression = 0;
                    } else {
                        monster.reduceAggressiveLength();
                    }
                    if (attack_tick > monster.getAggroStrikes()) {
                        //                    if (aggravation > (monster.getAggroLevel() * 20.0f)) {
                        //                        state = FSMState.ATTACK;
                        //                    }
                        state = FSMState.ATTACK;
                    }
                //}
                //attack_tick++;
                total_aggressive_time++;
                System.out.println("agg time tot " + total_aggressive_time + " " + MAX_AGGRESSIVE_TIME);
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
            if (monster.getAggravation() > 0.0f) { //&& state != FSMState.GONNA_POUNCE) {
                float aggravation = monster.getAggravation() - 0.5f;
                monster.setAggravation(aggravation);
            }
        }

        changeStateIfApplicable();

        float goal_x = diver.getX() + diver.getVX();
        float goal_y = diver.getY() + diver.getVY();

        switch (state) {

            case IDLE:

                if (tick % 50 == 0) {
                    if (tick % 100 == 0) {

                        Random rand = new Random();
                        int x_change = rand.nextInt(2) * 2 - 1;
                        int y_change = rand.nextInt(2) * 2 - 1;
                        float xpos = rand.nextFloat() * RAND_DIST_RANGE * x_change;
                        float ypos = rand.nextFloat() * RAND_DIST_RANGE * y_change;
                        target_pos = diver.getPosition().cpy().add(xpos, ypos);
                        //curr_pos = (target_pos.cpy().sub(curr_pos).nor()).add(curr_pos);
                        monster.moveMonster(target_pos);
                    }
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
                break;

            case AGGRIVATED:
//                if (tick % 5 == 0) {
                monster.moveMonster(diver.getPosition());
                if (tick %2 == 0) {
                    float best_distance = 10000.0f;
                    float temp_distance = 0.0f;
                    Wall final_loc = null;
                    int wall_tick = 0;
                    for (Wall wall : monster.getSpawnLocations()) {
                        if (wall_tick % 2 == 0) {
                            if (wall.canSpawnTentacle()) {
//                                Vector2 location = wall.getPosition();
//                                temp_distance = (float) Math.sqrt(
//                                        Math.pow((double) (goal_x - location.x), 2) +
//                                                Math.pow((double) (goal_y - location.y), 2)
//                                );
//                                if (temp_distance < best_distance) {
//                                    best_distance = temp_distance;
//                                    final_loc = wall;
//                                }
                                monster.addAggTentacle(wall);
                            }
                        }
                        wall_tick++;
                    }
                }
                break;

//            case GONNA_POUNCE:
//                audio.attack_roar();
//                break;

            case ATTACK:
                if (!hasRoared) {
                    roar_pause = tick;
                    audio.loud_roar_play(hasRoared);
                    diver.setStunCooldown(150);
                    diver.setStunned(true);

                    //diver.changeOxygenLevel(-diver.getOxygenLevel() + 3);
                    hasRoared = true;
                    roar_pause = tick;
                    monster.setVisionRadius(15);
//                    monster.setAggravation(100000.0f);
                } else if (tick - roar_pause > 150) {
//                    monster.setAggravation(100000.0f);
//                    diver.changeOxygenLevel(2);
                        monster.moveMonster(diver.getPosition());
                        diver.setStunned(false);
                        diver.setStunCooldown(20);
                        audio.chase();
                        float best_distance = 10000.0f;
                        float temp_distance = 0.0f;
                        Wall final_loc = null;
//                    best_distance = 10000.0f;
//                    temp_distance = 0.0f;
//                    final_loc = null;
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
                            monster.addKillTentacle(final_loc);
                            //monster.setAggravation(0.0f);
                        }
                    }
                    break;

                    default:
                        System.out.println("o no");
                }

//        System.out.println("CURR_POS " + curr_pos);
//        System.out.println("TARGET POS " + target_pos);
//        System.out.println("DIVER POS " + diver.getPosition());
        }

    }
