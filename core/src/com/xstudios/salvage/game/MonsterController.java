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
    private float ticks;
    private FilmStrip filmStrip;
    private TextureRegion texture;
    private Vector2 scale;

    /**
     * Creates an AIController for the ship with the given id.
     *
     * @param insert_monster the monster for the game
     */
    public MonsterController(Monster insert_monster) {
        monster = insert_monster;
        //filmStrip = new FilmStrip(tentacleFilmStrip, 1, 29, 29);
    }

    public Monster getMonster() {
        return monster;
    }

    public void insertAssets(FilmStrip tentacleFilmStrip, TextureRegion tentacleTexture, Vector2 tentacleScale) {
        filmStrip = tentacleFilmStrip;
        texture = tentacleTexture;
        scale = tentacleScale;
    }

    /**
     * Change the state of the monster based on aggrivation levels
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        float aggrivation = monster.getAggrivation();
        if (monster.getAggrivation() < 0.25f) {
            state = FSMState.IDLE;
        } else if (aggrivation < 1.0f) {
            state = FSMState.AGGRIVATED;
        } else if (aggrivation >= 1.0f) {
            state = FSMState.ATTACK;
        }

        // Next state depends on current state.
        switch (state) {

            case IDLE:
                break;

            case AGGRIVATED:
                break;

            case ATTACK:
                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.IDLE; // If debugging is off
                break;
        }
    }

    /**
     * Change the state of the monster based on aggrivation levels
     */
    public void update(DiverModel diver, GameController gameController) {
        float aggrivation = monster.getAggrivation();
        if (aggrivation < 1.0f) {
            monster.setAggrivation(monster.getAggrivation() - 0.001f);
        }
        changeStateIfApplicable();

        float current_x = diver.getX();
        float current_y = diver.getY();
        float tentacle_x = -10000.0f;
        float tentacle_y = -10000.0f;

//        for (GameObject object : gameController.objects){
//            if (object instanceof Wall){
//                if(current_x < object.getX() +10.0f && current_x > object.getX() - 10.0f){
//                    if (Math.random() < 0.5){
//                        tentacle_x = object.getX();
//                    }
//                    else {
//                        tentacle_x = object.getX();
//                    }
//                    tentacle_y = current_y;
//                    tentacle_x = current_x;
//                }
//            }}
        if (true) {//monster.getTentacles().size() < 10) {
            Tentacle tentacle = new Tentacle(tentacle_x, tentacle_y);
            tentacle.setFilmStrip(filmStrip);
            tentacle.setTexture(texture);
            tentacle.setDrawScale(scale);
            monster.addTentacle(tentacle);
            //tentacle.setBodyType(BodyDef.BodyType.DynamicBody);
            gameController.addObject(tentacle);
        }
//        if (state == FSMState.AGGRIVATED){
//
//            }
//
//
//        }
        ArrayList<Tentacle> removable = new ArrayList<>();
        for (Tentacle tentacle : monster.getTentacles()) {
            /**if (tentacle.getLife() < 100){
             tentacle.update();
             }
             else {
             removable.add(tentacle);
             }*/
            System.out.println(tentacle.getBody());
            tentacle.update();
        }
        /**for (Tentacle tentacle : removable){
         monster.removeTentacle(tentacle);
         tentacle.dispose();
         }*/
    }


}
