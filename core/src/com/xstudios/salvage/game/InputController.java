package com.xstudios.salvage.game;

/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.*;

import com.badlogic.gdx.utils.Array;
import com.xstudios.salvage.util.*;

/**
 * Class for reading player input.
 * <p>
 * This supports both a keyboard and X-Box controller. In previous solutions, we only
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
    /**
     * The singleton instance of the input controller
     */
    private static InputController theController = null;

    /**
     * Are we carrying an object?
     */
    private boolean carryingObject;
    private boolean carryingObjectPrevious;

    /**
     * Are we carrying the body?
     */
    private boolean dropFlarePressed;
    private boolean dropFlarePrevious;

    /**
     * did we open a chest?
     */
    private boolean openChest;
    private boolean openChestPrevious;


    /**
     * do we want to grab onto the wall?
     */
    private boolean kickOffPressed;
    private boolean kickOffPrevious;

    private boolean pausePressed;
    private boolean pausePrevious;

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    // Fields to manage buttons
    /**
     * Whether the reset button was pressed.
     */
    private boolean resetPressed;
    private boolean resetPrevious;
    /**
     * Whether the debug toggle was pressed.
     */
    private boolean debugPressed;
    private boolean debugPrevious;


    /**
     * Whether the debug toggle was pressed.
     */
    private boolean menuPressed;
    private boolean menuPrevious;


    /**
     * How much did we move horizontally?
     */
    private float horizontal;
    /**
     * How much did we move vertically?
     */
    private float vertical;

    /**
     * An X-Box controller (if it is connected)
     */
    XBoxController xbox;

    /**
     * Returns the amount of sideways movement.
     * <p>
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     * <p>
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }


    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    /**
     * Returns whether we are carrying an object
     *
     * @return whether we are carrying an object.
     */
    public boolean getOrDropObject() {
        return carryingObject && !carryingObjectPrevious;
    }

    /**
     * Returns whether we are carrying the body
     *
     * @return whether we are carrying the body.
     */
    public boolean dropFlare() {
        return dropFlarePressed && !dropFlarePrevious;
    }

    /**
     * Returns true if the player wants to go toggle the debug mode.
     *
     * @return true if the player wants to go toggle the debug mode.
     */
    public boolean didDebug() {
        return debugPressed && !debugPrevious;
    }

    /**
     * Returns true if the player wants to open a chest
     *
     * @return true if the player wants to open a chest
     */
    public boolean didOpenChest() {
        return openChest && !openChestPrevious;
    }

    /**
     * @return true when the player is trying to grab or kick off a wall
     */
    public boolean didKickOff() {
        return kickOffPressed && !kickOffPrevious;
    }

    public boolean isPause() {
        return pausePressed && !pausePrevious;
    }


    /**
     * Creates a new input controller
     * <p>
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public InputController() {
        // If we have a game-pad for id, then use it.
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get(0);
        } else {
            xbox = null;
        }
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     * <p>
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    public void readInput(Rectangle bounds, Vector2 scale) {
        // Copy state from last animation frame
        // Helps us ignore buttons that are held down
        resetPrevious = resetPressed;
//        kickOffPrevious = kickOffPressed;
        debugPrevious = debugPressed;
        menuPrevious = menuPressed;
        carryingObjectPrevious = carryingObject;
        openChestPrevious = openChest;
        dropFlarePrevious = dropFlarePressed;
        pausePrevious = pausePressed;


        // Check to see if a GamePad is connected
        if (xbox != null && xbox.isConnected()) {
            readGamepad(bounds, scale);
            readKeyboard(bounds, scale, true); // Read as a back-up
        } else {
            readKeyboard(bounds, scale, false);
        }
    }

    /**
     * Reads input from an X-Box controller connected to this computer.
     * <p>
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    private void readGamepad(Rectangle bounds, Vector2 scale) {
        resetPressed = xbox.getStart();
//        debugPressed  = xbox.getY();

        // Increase animation frame, but only if trying to move
        horizontal = xbox.getLeftX();
        vertical = xbox.getLeftY();
    }

    /**
     * Reads input from the keyboard.
     * <p>
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     * @param secondary true if the keyboard should give priority to a gamepad
     */
    private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
        // Give priority to gamepad results
        resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
        debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.P));

        kickOffPressed = (secondary && kickOffPrevious) || (Gdx.input.isKeyPressed(Keys.SPACE));

        carryingObject = (secondary && carryingObject) || Gdx.input.isKeyPressed(Input.Keys.Q);

        dropFlarePressed = (secondary && dropFlarePressed) || Gdx.input.isKeyPressed(Input.Keys.Z);

        openChest = (secondary && openChest) || Gdx.input.isKeyPressed(Input.Keys.X);

        pausePressed = (secondary && pausePressed) || Gdx.input.isKeyPressed(Keys.ESCAPE);

        // Directional controls
        horizontal = (secondary ? horizontal : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }

        vertical = (secondary ? vertical : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1.0f;
        }

    }


}