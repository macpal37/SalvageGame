/* 
 * XBoxController.java
 *
 * Input handler for XBox (360/One) controller
 *
 * This controller is updated to support the new Controller 2.0 framework.
 * This greatly simplifies code, as we do not have to write a separate
 * controller for each OS. Even on the same controllers, Windows and Mac
 * have different button mappings, and this forced us to do a lot of
 * wizardry in this class.  The new ControllerMappings simplify a lot
 * of that work.
 *
 * Mac OS driver support is provided by Colin Munro (updated by RodrigoCard):
 *
 * https://github.com/360Controller/360Controller/releases
 *
 * We have moved this class to a util package so that you do not waste time looking
 * at the code.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 2/8/2021
 */
package com.xstudios.salvage.util;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.ControllerPowerLevel;

/**
 * Class to support an XBox (360/One) controller
 * 
 * This is a wrapper class, which wraps around a Controller object to provide
 * concrete mappings for the buttons, joysticks and triggers.  It is simpler than
 * having to remember the exact device numbers (even as constants) for each button.
 * It is particularly important because different operating systems have different
 * mappings for the buttons.
 *
 * Each controller must have its own instance.  The constructor automatically 
 * determines what OS this controller is running on before assigning the mappings.
 * The constructor DOES NOT verify that the controller is indeed an XBox 360 
 * controller.
 */
public class XBoxController implements Controller, ControllerListener {
	/** Reference the controller mappings for this device. */
	private ControllerMapping mapping;
	/** Reference to base controller object wrapped by this instance. */
	public Controller controller;
	/** How far out the joysticks must be to register */
	private float deadZone = 0.01f;

	/**
	 * This enumerations supports D-pad directional controls.
	 *
	 * Low-level device polling can only ask about individual d-pad buttons.
	 * However, the primary usage of a d-pad is for directional information.
	 * That is encapsulated in this enum.
 	 */
	public enum Direction {
		/** The d-pad is not currently pressed */
		NEUTRAL,
		/** The right d-pad button is pressed, and not other is */
		EAST,
		/** The right and down d-pad buttons are pressed */
		SOUTHEAST,
		/** The down d-pad button is pressed, and not other is */
		SOUTH,
		/** The left and down d-pad buttons are pressed */
		SOUTHWEST,
		/** The left d-pad button is pressed, and not other is */
		WEST,
		/** The left and up d-pad buttons are pressed */
		NORTHWEST,
		/** The up d-pad button is pressed, and not other is */
		NORTH,
		/** The right and up d-pad buttons are pressed */
		NORTHEAST
	}

	/**
	 * Creates a new XBox interface for the given controller.
	 *
	 * An XBox controller must have an OS supported mapping for its buttons.
	 *
	 * @throws IllegalStateException if the controller does not have a supported mapping
	 *
	 * @param controller The controller to wrap
	 */
	public XBoxController(Controller controller) {
		if (controller == null) {
			throw new NullPointerException();
		}
		this.controller = controller;
		mapping = controller.getMapping();
		if (mapping == null) {
			throw new IllegalStateException("Controller does not have a supported mapping");
		}
	}

	/**
	 * Returns the deadzone value for the joysticks.
	 *
	 * Due to joystick sensitivity, we allow some minor dead zone settings.
	 * Any joystick value within this distance of 0 will register as 0.
	 *
	 * @return the deadzone value for the joysticks.
	 */
	public float getDeadZone() {
		return deadZone;
	}

	/**
	 * Sets the deadzone value for the joysticks.
	 *
	 * Due to joystick sensitivity, we allow some minor dead zone settings.
	 * Any joystick value within this distance of 0 will register as 0.
	 *
	 * @param tol 	The deadzone value for the joysticks.
	 */
	public void setDeadZone(float tol) {
		deadZone = tol;
	}

	/**
	 * Returns true if the start button is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the start button is currently pressed
	 */
	public boolean getStart()  {
		if (controller != null) {
			controller.getButton( mapping.buttonStart );
		}
		return false;
	}

	/**
	 * Returns true if the back button is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the back button is currently pressed
	 */
	public boolean getBack()  {
		if (controller != null) {
			return controller.getButton( mapping.buttonBack );
		}
		return false;
	}

	/**
	 * Returns true if the X button is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the X button is currently pressed
	 */
	public boolean getX()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonX );
		}
		return false;
	}

	/**
	 * Returns true if the Y button is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the Y button is currently pressed
	 */
	public boolean getY()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonY );
		}
		return false;
	}

	/**
	 * Returns true if the A button is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the A button is currently pressed
	 */
	public boolean getA()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonA );
		}
		return false;
	}

	/**
	 * Returns true if the B button is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the Y button is currently pressed
	 */
	public boolean getB()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonB );
		}
		return false;
	}

	/**
	 * Returns true if the left bumper is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the left bumper is currently pressed
	 */
	public boolean getLBumper()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonL1 );
		}
		return false;
	}

	/**
	 * Returns true if the left analog stick is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the left analog stick is currently pressed
	 */
	public boolean getLStick()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonLeftStick );
		}
		return false;
	}

	/**
	 * Returns true if the right bumper is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the right bumper is currently pressed
	 */
	public boolean getRBumper()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonR1 );
		}
		return false;
	}

	/**
	 * Returns true if the right analog stick is currently pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the right analog stick is currently pressed
	 */
	public boolean getRStick()   {
		if (controller != null) {
			return controller.getButton( mapping.buttonRightStick );
		}
		return false;
	}

	/**
	 * Returns true if the DPad Up button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right,
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the DPad Up button is currently pressed
	 */
	public boolean getDPadUp() {
		if (controller != null) {
			return controller.getButton( mapping.buttonDpadUp ) &&
					!controller.getButton( mapping.buttonDpadLeft ) &&
					!controller.getButton( mapping.buttonDpadRight );
		}
		return false;
	}

	/**
	 * Returns true if the DPad Down button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right,
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the DPad Down button is currently pressed
	 */
	public boolean getDPadDown() {
		if (controller != null) {
			return controller.getButton( mapping.buttonDpadDown ) &&
					!controller.getButton( mapping.buttonDpadLeft ) &&
					!controller.getButton( mapping.buttonDpadRight );
		}
		return false;
	}

	/**
	 * Returns true if the DPad Left button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right,
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the DPad Left button is currently pressed
	 */
	public boolean getDPadLeft() {
		if (controller != null) {
			return controller.getButton( mapping.buttonDpadLeft ) &&
					!controller.getButton( mapping.buttonDpadUp ) &&
					!controller.getButton( mapping.buttonDpadDown );
		}
		return false;
	}

	/**
	 * Returns true if the DPad Right button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right,
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @return true if the DPad Right button is currently pressed
	 */
	public boolean getDPadRight() {
		if (controller != null) {
			return controller.getButton( mapping.buttonDpadRight ) &&
					!controller.getButton( mapping.buttonDpadUp ) &&
					!controller.getButton( mapping.buttonDpadDown );
		}
		return false;
	}

	/**
	 * Returns the current direction of the DPad
	 *
	 * The result will be one of the eight cardinal directions, or
	 * center if the DPad is not actively pressed.
	 *
	 * This method returns NEUTRAL if the controller is disconnected.
	 *
	 * @return the current direction of the DPad
	 */
	public Direction getDPadDirection() {
		if (controller != null) {
			int x = 0;
			int y = 0;
			if (controller.getButton( mapping.buttonDpadLeft )) {
				x--;
			}
			if (controller.getButton( mapping.buttonDpadRight )) {
				x++;
			}
			if (controller.getButton( mapping.buttonDpadUp )) {
				y++;
			}
			if (controller.getButton( mapping.buttonDpadDown )) {
				y--;
			}
			if (x == 0) {
				if (y == 0) {
					return Direction.NEUTRAL;
				} else if (y < 0) {
					return Direction.SOUTH;
				} else {
					return Direction.NORTH;
				}
			} else if (x < 0) {
				if (y == 0) {
					return Direction.WEST;
				} else if (y < 0) {
					return Direction.SOUTHWEST;
				} else {
					return Direction.NORTHWEST;
				}
			} else {
				if (y == 0) {
					return Direction.EAST;
				} else if (y < 0) {
					return Direction.SOUTHEAST;
				} else {
					return Direction.NORTHEAST;
				}
			}
		}
		return Direction.NEUTRAL;
	}

	/**
	 * Returns the X axis value of the left analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is to the left.
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the X axis value of the left analog stick.
	 */
	public float getLeftX() {
		if (controller != null) {
			float value = controller.getAxis( mapping.axisLeftX );
			if (Math.abs( value ) > deadZone) {
				return value < -1.0f ? -1.0f : (value > 1.0f ? 1.0f : value);
			}
		}
		return 0;
	}

	/**
	 * Returns the Y axis value of the left analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is towards the bottom.
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the Y axis value of the left analog stick.
	 */
	public float getLeftY() {
		if (controller != null) {
			float value = controller.getAxis( mapping.axisLeftY );
			if (Math.abs( value ) > deadZone) {
				return value < -1.0f ? -1.0f : (value > 1.0f ? 1.0f : value);
			}
		}
		return 0;
	}

	/**
	 * Returns the value of the left trigger.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the value of the left trigger.
	 */
	public float getLeftTrigger() {
		if (controller != null) {
			float value = controller.getAxis( controller.getAxisCount() - 2 );
			return value < 0.0f ? 0.0f : (value > 1.0f ? 1.0f : value);
		}
		return 0;
	}

	/**
	 * Returns the X axis value of the right analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is to the left.
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the X axis value of the right analog stick.
	 */
	public float getRightX()  {
		if (controller != null) {
			float value = controller.getAxis( mapping.axisRightX );
			if (Math.abs( value ) > deadZone) {
				return value < -1.0f ? -1.0f : (value > 1.0f ? 1.0f : value);
			}
		}
		return 0;
	}

	/**
	 * Returns the Y axis value of the right analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is towards the bottom.
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the Y axis value of the right analog stick.
	 */
	public float getRightY() {
		if (controller != null) {
			float value = controller.getAxis( mapping.axisRightY );
			if (Math.abs( value ) > deadZone) {
				return value < -1.0f ? -1.0f : (value > 1.0f ? 1.0f : value);
			}
		}
		return 0;
	}

	/**
	 * Returns the value of the right trigger.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the value of the right trigger.
	 */
	public float getRightTrigger()  {
		if (controller != null) {
			float value = controller.getAxis( controller.getAxisCount() - 1 );
			return value < 0.0f ? 0.0f : (value > 1.0f ? 1.0f : value);
		}
		return 0;
	}

	// PASS THROUGH METHODS FOR CONTROLLER
	/**
	 * Returns whether the button is pressed.
	 *
	 * This method returns false if the controller is disconnected.
	 *
	 * @param buttonCode The button code
	 *
	 * @return whether the button is pressed. */
	@Override
	public boolean getButton(int buttonCode) {
		if (controller != null) {
			return controller.getButton( buttonCode );
		}
		return false;
	}

	/**
	 * Returns the value of the axis, between -1 and 1
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @param axisCode	The axis code
	 *
	 * @return the value of the axis, between -1 and 1
	 */
	@Override
	public float getAxis(int axisCode) {
		if (controller != null) {
			return getAxis( axisCode );
		}
		return 0;
	}

	/**
	 * Returns the device name
	 *
	 * This method returns null if the controller is disconnected.
	 *
	 * @return the device name
	 */
	@Override
	public String getName() {
		if (controller != null) {
			return getName();
		}
		return null;
	}

	/**
	 * Returns the unique ID for this controller.
	 *
	 * This ID is used to recognize this controller if more than one of the same controller
	 * models are connected. Use this to map a controller to a player, but do not use it to
	 * save a button mapping.
	 *
	 * This method returns null if the controller is disconnected.
	 *
	 * @return the unique ID for this controller.
	 */
	@Override
	public String getUniqueId() {
		if (controller != null) {
			return getUniqueId();
		}
		return null;
	}

	/**
	 * Returns the minimum button index code that can be queried
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the minimum button index code that can be queried
	 */
	@Override
	public int getMinButtonIndex() {
		if (controller != null) {
			return getMinButtonIndex( );
		}
		return 0;
	}

	/**
	 * Returns the maximum button index code that can be queried
	 *
	 * This method returns 0 if the controller is disconnected.
	 *
	 * @return the maximum button index code that can be queried
	 */
	@Override
	public int getMaxButtonIndex() {
		if (controller != null) {
			return getMaxButtonIndex( );
		}
		return 0;
	}

	/**
	 * Returns the number of axes of this controller.
	 *
	 * Axis indices start at 0, so the maximum axis index is one under this value.
	 * On XBoxes, the high end axes are the triggers.
	 *
	 * This method returns 0 if the controller is not connected.
	 *
	 * @return the number of axes of this controller.
	 */
	@Override
	public int getAxisCount() {
		if (controller != null) {
			return getAxisCount( );
		}
		return 0;
	}

	/**
	 * Returns true if this Controller is still connected
	 *
	 * This method returns false if the controller is not connected.
	 *
	 * @return true if this Controller is still connected
	 */	@Override
	public boolean isConnected() {
		return controller != null && controller.isConnected();
	}

	/**
	 * Returns whether the controller can rumble.
	 *
	 * Note that this is no guarantee that the connected controller itself can vibrate.
	 * Simply that the system believes that it can.
	 *
	 * This method returns false if the controller is not connected.
	 *
	 * @return whether the controller can rumble.
	 */
	@Override
	public boolean canVibrate() {
		if (controller != null) {
			return canVibrate( );
		}
		return false;
	}

	/**
	 * Returns true if the controller is currently rumbling
	 *
	 * @return true if the controller is currently rumbling
	 */
	@Override
	public boolean isVibrating() {
		if (controller != null) {
			return isVibrating( );
		}
		return false;
	}

	/**
	 * Starts vibrating this controller, if possible.
	 *
	 * This method does nothing if the controller is not connected.
	 *
	 * @param duration duration, in milliseconds
	 * @param strength value between 0f and 1f
	 */
	@Override
	public void startVibration(int duration, float strength) {
		if (controller != null) {
			startVibration(duration, strength );
		}
	}

	/**
	 * Cancels any running vibration.
	 *
	 * This not be supported by some implementations.
	 * This method does nothing if the controller is not connected.
	 */
	@Override
	public void cancelVibration() {
		if (controller != null) {
			cancelVibration( );
		}
	}

	/**
	 * Returns whether the controller can return and set the player index
	 *
	 * This method returns false if the controller is not connected.
	 *
	 * @return whether the controller can return and set the player index
	 */
	@Override
	public boolean supportsPlayerIndex() {
		if (controller != null) {
			return supportsPlayerIndex( );
		}
		return false;
	}

	/**
	 * Returns 0-based player index of this controller
	 *
	 * This returns PLAYER_IDX_UNSET if none is set, or if the controller
	 * is disconnected.
	 *
	 * @return 0-based player index of this controller
	 */
	@Override
	public int getPlayerIndex() {
		if (controller != null) {
			return getPlayerIndex( );
		}
		return PLAYER_IDX_UNSET;
	}

	/**
	 * Sets the player index of this controller.
	 *
	 * Please note that this does not always set indication lights of controllers.
	 * It is just an internal representation on some platforms.  Set this
	 * to PLAYER_IDX_UNSET to unset the index.
	 *
	 * This method does nothing if the controller is disconnected.
	 *
	 * @param index 0 typically 0 to 3 for player indices, and PLAYER_IDX_UNSET for unset
	 */
	@Override
	public void setPlayerIndex(int index) {
		if (controller != null) {
			setPlayerIndex(index );
		}
	}

	/**
	 * Returns button and axis mapping for this controller (or platform).
	 *
	 * The connected controller might not support all features.	 This method returns
	 * null if the controller is not connected.
	 *
	 * @return button and axis mapping for this controller (or platform).
	 */
	@Override
	public ControllerMapping getMapping() {
		return mapping;
	}

	/**
	 * Returns the value of enum {@link ControllerPowerLevel}
	 *
	 * This value indicates the battery state of the connected controller, or
	 * {@link ControllerPowerLevel#POWER_UNKNOWN} if information is not present
	 * (including when the controller is disconnected)
	 *
	 * @return the value of enum {@link ControllerPowerLevel}
	 */
	@Override
	public ControllerPowerLevel getPowerLevel() {
		if (controller != null) {
			return getPowerLevel( );
		}
		return null;
	}

	/**
	 * Adds a new {@link ControllerListener} to this {@link Controller}.
	 *
	 * The listener will receive calls in case the state of the controller changes. The
	 * listener will be invoked on the rendering thread.
	 *
	 * This method does nothing if the controller is disconnected.
	 *
	 * @param listener	The listener to add
	 */
	@Override
	public void addListener(ControllerListener listener) {
		if (controller != null) {
			controller.addListener( listener );
		}
	}

	/**
	 * Removes the given {@link ControllerListener}
	 *
	 * This method does nothing if the controller is disconnected or the given
	 * listener is not registered.
	 *
	 * @param listener	The listener to remove
	 */
	@Override
	public void removeListener(ControllerListener listener) {
		if (controller != null) {
			controller.removeListener( listener );
		}
	}

	// METHODS FOR CONTROLLER LISTENER

	/** 
	 * A Controller got connected.
	 *
	 * @param controller	The controller interface
	 */
	@Override
	public void connected (Controller controller) {
	}

	/** 
	 * A Controller got disconnected.
	 *
	 * @param controller	The controller interface
	 */
	public void disconnected (Controller controller) {
		if (this.controller == controller) {
			this.controller = null;
			mapping = null;
		}
	}

	/** 
	 * A button on the Controller was pressed. 
	 * 
	 * The buttonCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts button constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param buttonCode	The button pressed
	 * @return whether to hand the event to other listeners. 
	 */
	@Override
	public boolean buttonDown (Controller controller, int buttonCode) { return true; }

	/** 
	 * A button on the Controller was released. 
	 *
	 * The buttonCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts button constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param buttonCode	The button released
	 * @return whether to hand the event to other listeners. 
	 */
	@Override
	public boolean buttonUp (Controller controller, int buttonCode) { return true; }

	/** 
	 * An axis on the Controller moved. 
	 *
	 * The axisCode is controller specific. The axis value is in the range [-1, 1]. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts axes constants for 
	 * known controllers.
	 *
	 * @param controller	The controller interface
	 * @param axisCode		The axis identifier
	 * @param value 		The axis value, -1 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	@Override
	public boolean axisMoved (Controller controller, int axisCode, float value) { return true; }
}
