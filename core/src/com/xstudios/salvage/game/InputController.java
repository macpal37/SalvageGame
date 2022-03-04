/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameMode does not have to keep track of the current
 * key mapping.
 *
 * This class is NOT a singleton. Each input device is its own instance,
 * and you may have multiple input devices attached to the game.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.*;
import com.xstudios.salvage.util.*;
import com.badlogic.gdx.controllers.Controller;

/**
 * Device-independent input manager.
 *
 * This class supports both a keyboard and an X-Box controller.  Each player is
 * assigned an ID.  When the class is created, we check to see if there is a 
 * controller for that ID.  If so, we use the controller.  Otherwise, we default
 * the the keyboard.
 */
public class InputController {
	
    /** Player id, to identify which keys map to this player */
	protected int player;

    /** X-Box controller associated with this player (if any) */
	protected XBoxController xbox;

	/** How much forward are we going? */
	private float forward;				
	
	/** How much are we turning? */
	private float turning;
	
	/** Did we press the fire button? */
	private boolean pressedFire;
	
	/** 
	 * Returns the amount of forward movement.
	 * 
	 * -1 = backward, 1 = forward, 0 = still
	 *  
	 * @return amount of forward movement.
	 */
	public float getForward() {
		return forward;
	}

	/**
	 * Returns the amount to turn the ship.
	 * 
	 * -1 = clockwise, 1 = counter-clockwise, 0 = still
	 * 
	 * @return amount to turn the ship.
	 */
	public float getTurn() {
		return turning;
	}

	/**
	 * Returns whether the fire button was pressed.
	 * 
	 * @return whether the fire button was pressed.
	 */
	public boolean didPressFire() {
		return pressedFire;
	}

	/**
	 * Creates a new input controller for the specified player.
	 * 
	 * The game supports two players working against each other in hot seat mode. 
	 * We need a separate input controller for each player. In keyboard, this is 
	 * WASD vs. Arrow keys.  We also support multiple X-Box game controllers.
	 * 
	 * @param id Player id number (0..4)
	 */
	public InputController(int id) {
		player = id;
		
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > id) {
			xbox = controllers.get(id);
		} else {
			xbox = null;
		}
	}

	/**
	 * Reads the input for this player and converts the result into game logic.
	 *
	 * This is an example of polling input.  Instead of registering a listener,
	 * we ask the controller about its current state.  When the game is running,
	 * it is typically best to poll input instead of using listeners.  Listeners
	 * are more appropriate for menus and buttons (like the loading screen). 
	 */
	public void readInput() {
		// If there is a game-pad, then use it.
		if (xbox != null) {
			forward = -xbox.getLeftY();
			forward = (forward < 0.1 && forward > -0.1 ? 0.0f : forward);

			turning = -xbox.getRightX();
			turning = (turning < 0.1 && turning > -0.1 ? 0.0f : turning);

			pressedFire = xbox.getRightTrigger() > 0.6f;
		} else {
            // Figure out, based on which player we are, which keys
			// control our actions (depends on player).
            int up, left, right, down, shoot;
			if (player == 0) {
                up    = Input.Keys.UP; 
                down  = Input.Keys.DOWN;
                left  = Input.Keys.LEFT; 
                right = Input.Keys.RIGHT;
                shoot = Input.Keys.SPACE;
			} else {
                up    = Input.Keys.W; 
                down  = Input.Keys.S;
                left  = Input.Keys.A; 
                right = Input.Keys.D;
                shoot = Input.Keys.X;
            }
			
            // Convert keyboard state into game commands
            forward = turning = 0;
            pressedFire = false;

            // Movement forward/backward
			if (Gdx.input.isKeyPressed(up) && !Gdx.input.isKeyPressed(down)) {
                forward = 1;
			} else if (Gdx.input.isKeyPressed(down) && !Gdx.input.isKeyPressed(up)) {
                forward = -1;
			}
			
            // Movement left/right
			if (Gdx.input.isKeyPressed(left) && !Gdx.input.isKeyPressed(right)) {
                turning = 1;
			} else if (Gdx.input.isKeyPressed(right) && !Gdx.input.isKeyPressed(left)) {
                turning = -1;
			}

            // Shooting
			if (Gdx.input.isKeyPressed(shoot)) {
                pressedFire = true;
			}

			if (player == 0) {
				int m_shoot = Input.Buttons.LEFT;

				// move up/down and left/right
				if (Gdx.input.getInputProcessor().mouseMoved(0, 0)) {
					forward -= Gdx.input.getDeltaY() * 0.3f;
					turning -= Gdx.input.getDeltaX() * 0.1f;
				}

				// Mouse Support
				if (Gdx.input.isButtonPressed(m_shoot)) {
					pressedFire = true;
				}
			}
			
		}
    }
}