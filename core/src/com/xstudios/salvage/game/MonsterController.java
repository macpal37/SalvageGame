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

import java.util.*;

import static com.badlogic.gdx.math.MathUtils.random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Monster;
import com.xstudios.salvage.game.models.Tentacle;
import com.xstudios.salvage.util.FilmStrip;
import com.xstudios.salvage.util.PooledList;
import com.xstudios.salvage.game.models.Wall;
import com.badlogic.gdx.graphics.Texture;

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
         * The monster is aggravated and warning the player
         */
        AGGRIVATED,
        /**
         * The monster is attacking the player
         */
        ATTACK
    }

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



    private PooledList<Vector2> targetLocations;


    /**
     * Creates an AIController for the ship with the given id.
     *
     * @param monster the monster for the game
     */
    public MonsterController(Monster monster) {
        this.monster = monster;
        targetLocations = new PooledList<>();
        targetLocations.push(monster.getPosition());
        tick = 0;
    }


    public Monster getMonster() {
        return monster;
    }

    public void wallCollision() {
        float agg = monster.getAggrivation();
        if (agg < 7.0f) {
            monster.setAggrivation(agg + 1.0f);
        }
    }

    /**
     * Change the state of the monster based on aggrivation levels
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        float aggrivation = monster.getAggrivation();
        System.out.println(aggrivation);
        if (aggrivation > 6.0f)  {
            state = FSMState.AGGRIVATED;
        }
        else {
            state = FSMState.IDLE;
        }
        // Next state depends on current state.
        switch (state) {

            case IDLE:
                if (aggrivation > 6.0f)  {
                    state = FSMState.AGGRIVATED;
                }
                break;

            case AGGRIVATED:
                if (aggrivation <= 6.0f)  {
                    state = FSMState.IDLE;
                }
                break;

            case ATTACK:
                state = FSMState.AGGRIVATED;
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
     * Change the state of the monster based on aggrivation levels
     */
    public void update(float aggrivationDrain, DiverModel diver) {
        tick++;
        if (tick % 50 == 0) {
            if (monster.getAggrivation() > 0.0f){
            float aggrivation = monster.getAggrivation() - 0.5f;
            monster.setAggrivation(aggrivation);
            }
        }
        monster.moveMonster(diver.getPosition());
        changeStateIfApplicable();

        float goal_x = diver.getX() + diver.getVX();
        float goal_y = diver.getY() + diver.getVY();

        switch (state) {

            case AGGRIVATED:
                if (tick % 250 == 0) {
                    if (monster.getTentacles().size() < 3) {
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
                }
                break;
            default:

        }


    }


}
