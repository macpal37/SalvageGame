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

	/** How much up or down are we moving? */
	private float vertical;
	
	/** How much are we moving side to side? */
	private float horizontal;

	/** How wide is the field of vision? */
	private float lightRange =1 ;

	private float MAX_LIGHT_RANGE =2;
	private float MIN_LIGHT_RANGE =0.3f;

	/** How fast are we going? */
	private float speed =1;
	private float MAX_SPEED =5;
	private float MIN_SPEED =0.5f;

	/** How fast are we depleting oxygen? */
	private float oxygenRate = -.01f;

	/** Are we resetting? */
	private boolean resetGame = false;

	/** Did we press the fire button? */
	private boolean pressedFire;

	/** Are the controls options shown? */
	private boolean controlOptions = false;

	private long ticks;

	/** Are we carrying an object? */
	private boolean carryingObject;

	/** 
	 * Returns the amount of horizontal forward movement.
	 * 
	 * -1 = backward, 1 = forward, 0 = still
	 *  
	 * @return amount of forward movement.
	 */
	public float getForward() {
		return horizontal;
	}

	/**
	 * Returns the amount to change range of vision.
	 * 
	 * -1 = decrease, 1 = inrease, 0 = still
	 * 
	 * @return amount to change range of vision.
	 */
	public float getLightRange() {
		return lightRange;
	}

	/**
	 * Returns the amount to change rate of oxygen depletion.
	 *
	 * -1 = decrease, 1 = inrease, 0 = still
	 *
	 * @return amount to change change rate of oxygen depletion.
	 */
	public float getOxygenRate() {
		return oxygenRate;
	}

	/**
	 * Returns the amount to change speed of movement.
	 *
	 * -1 = decrease, 1 = inrease, 0 = still
	 *
	 * @return amount to change speed of movement.
	 */
	public float getSpeed() {
		return speed;
	}


	/**
	 * Returns the amount to move up or down
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return amount to move up or down
	 */
	public float getUp() {
		return vertical;
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
	 * Returns whether we are carrying an object
	 *
	 * @return whether we are carrying an object.
	 */
	public boolean getOrDropObject() {
		return carryingObject;
	}
	/**
	 * Returns whether the reset button was pressed.
	 *
	 * @return whether the reset button was pressed.
	 */
	public boolean didReset() {
		return resetGame;
	}

	public boolean getControlOptionsVisible() {
		return controlOptions;
	}

	public void incrementTicks() {
		ticks++;
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
			horizontal = -xbox.getLeftY();
			horizontal = (horizontal < 0.1 && horizontal > -0.1 ? 0.0f : horizontal);

			vertical = -xbox.getRightX();
			vertical = (vertical < 0.1 && vertical > -0.1 ? 0.0f : vertical);

			pressedFire = xbox.getRightTrigger() > 0.6f;
		} else {
            // Figure out, based on which player we are, which keys
			// control our actions (depends on player).
            int up, left, right, down, shoot, light_increase,
					light_decrease, speed_increase, speed_decrease,
					oxygen_increase, oxygen_decrease, reset, controls, pick_up;

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

			light_increase = Input.Keys.I;
			light_decrease = Input.Keys.K;
			speed_increase = Input.Keys.U;
			speed_decrease = Input.Keys.J;
			oxygen_increase = Input.Keys.Y;
			oxygen_decrease = Input.Keys.H;
			reset = Input.Keys.R;
			controls = Input.Keys.C;
			pick_up = Input.Keys.X;
			
            // Convert keyboard state into game commands
            vertical = horizontal = 0;
            pressedFire = false;

			// Movement forward/backward
			if (Gdx.input.isKeyPressed(up) && !Gdx.input.isKeyPressed(down)) {
				vertical = 1;
			} else if (Gdx.input.isKeyPressed(down) && !Gdx.input.isKeyPressed(up)) {
				vertical = -1;
			}

			// Movement left/right
			if (Gdx.input.isKeyPressed(left) && !Gdx.input.isKeyPressed(right)) {
				horizontal = -1;
			} else if (Gdx.input.isKeyPressed(right) && !Gdx.input.isKeyPressed(left)) {
				horizontal = 1;
			}

			// change lighting range
			if (Gdx.input.isKeyPressed(light_increase) && !Gdx.input.isKeyPressed(light_decrease)) {
				if (lightRange < MAX_LIGHT_RANGE) {
					lightRange += .01f;
				}
			} else if (Gdx.input.isKeyPressed(light_decrease) && !Gdx.input.isKeyPressed(light_increase)) {
				if (lightRange > MIN_LIGHT_RANGE) {
					lightRange -= 0.01f;
				}
			}

			// change speed of movement
			if (Gdx.input.isKeyPressed(speed_increase) && !Gdx.input.isKeyPressed(speed_decrease)) {
				if (speed < MAX_SPEED) {
					speed += .005f;
				}
			} else if (Gdx.input.isKeyPressed(speed_decrease) && !Gdx.input.isKeyPressed(speed_increase)) {
				if (speed > MIN_SPEED) {
					speed -= .005f;
				}
			}

			// change rate of oxygen depletion
			if (Gdx.input.isKeyPressed(oxygen_decrease) && !Gdx.input.isKeyPressed(oxygen_increase)) {
				if (oxygenRate < -0.006) {
					oxygenRate += 0.005;
				}
			} else if (Gdx.input.isKeyPressed(oxygen_increase) && !Gdx.input.isKeyPressed(oxygen_decrease)) {
				oxygenRate -= 0.005;
			}

			// ticks to make toggling smoother
			if (ticks % 5 == 0) {
				// whether to reset oxygen or not
				if (Gdx.input.isKeyPressed(reset)) {
					resetGame = true;
				} else {
					resetGame = false;
				}

				// If controls key is pressed
				if (Gdx.input.isKeyPressed(controls)) {
					controlOptions = !controlOptions;
				}

				if (Gdx.input.isKeyPressed(pick_up)) {
					carryingObject = true;
				} else {
					carryingObject = false;
				}
			}
		}
    }
}