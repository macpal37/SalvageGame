/*
 * Ship.java
 *
 * This class tracks all of the state (position, velocity, rotation) of a 
 * single ship. In order to obey the separation of the model-view-controller 
 * pattern, controller specific code (such as reading the keyboard) is not 
 * present in this class.
 * 
 * Looking through this code you will notice certain optimizations. We want
 * to eliminate as many "new" statements as possible in the draw loop. In
 * game programming, it is considered bad form to have "new" statements in 
 * an update or a graphics loop if you can easily avoid it.  Each "new" is 
 * a potentially  expensive memory allocation. 
 *
 * To get around this, we have predeclared some Vector2 objects.  These are 
 * used by the draw method to position the objects on the screen. As we know
 * we will need that memory animation frame, it is better to have them
 * declared ahead of time (even though we are not taking state across frame
 * boundaries).
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/3/2015
 */
package com.xstudios.salvage.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.xstudios.salvage.util.*;

/**
 * Model class representing an alien ship.
 * 
 * Note that the graphics resources in this class are static.  That
 * is because all ships share the same image file, and it would waste
 * memory to load the same image file for each ship.
 */
public class Ship {

	/** The frame number for a ship that is not turning */
    public static final int SHIP_IMG_FLAT = 9;


    // Private constants to avoid use of "magic numbers"

    /** The amount to offset the shadow image by */
	private static final float SHADOW_OFFSET = 10.0f;

	/** Amount to decay forward thrust over time */
	private static final float FORWARD_DAMPING = 0.9f;
    // Modify this as part of the lab
    
	/** Position of the ship */
	private Vector2 pos;
	/** Velocity of the ship */
	private Vector2 vel;
	/** Color to tint this ship (red or blue) */
	private Color  tint;
	/** Color of the ships shadow (cached) */
	private Color stint;
	
	/** Mass/weight of the ship. Used in collisions. */
	private float mass;
	
	// The following are protected, because they have no accessors
//	/** Offset of the ships target */
//    protected Vector2 tofs;
	/** Current angle of the ship */
    protected float ang;
	/** Accumulator variable to turn faster as key is held down */
    protected float dang;
    /** Countdown to limit refire rate */
    protected int refire;
    /** size of the ship */
    protected int size;

    protected int type;

	protected  float speed =1f;

	// Asset references.  These should be set by GameMode
	/** Reference to ship's sprite for drawing */
    private FilmStrip shipSprite;
//	/** Texture for the target reticule */
//	private Texture targetTexture;

	/** The maximum oxygen of the diver **/
	private int MAX_OXYGEN;
	private float oxygen_level;

    // ACCESSORS
    /**
     * Returns the image filmstrip for this ship
     * 
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image texture for this ship
     */
    public FilmStrip getFilmStrip() {
    	return shipSprite;
    }
    
    /**
     * Sets the image texture for this ship
     * 
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for this ship
     */
    public void setFilmStrip(FilmStrip value) {
    	shipSprite = value;
    	shipSprite.setFrame(SHIP_IMG_FLAT);
    }
    
    /**
     * Returns the image texture for the target reticule
     * 
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image texture for the target reticule
     */
//    public Texture getTargetTexture() {
//    	return targetTexture;
//    }
    


	public void setSpeed(float speed){
		this.speed = speed;
	}

	/**
	 * Returns the position of this ship.
	 *
	 * This is location of the center pixel of the ship on the screen.
	 *
	 * @return the position of this ship
	 */
	public Vector2 getPosition() { 
		return pos;  
	}
	
	/**
	 * Sets the position of this ship.
	 *
	 * This is location of the center pixel of the ship on the screen.
	 *
	 * @param value the position of this ship
	 */
	public void setPosition(Vector2 value) { 
		pos.set(value);
	}

	/**
	 * Returns the velocity of this ship.
	 *
	 * This value is necessary to control momementum in ship movement.
	 *
	 * @return the velocity of this ship
	 */
	public Vector2 getVelocity() {
		return vel;  
	}

	/**
	 * Sets the velocity of this ship.
	 *
	 * This value is necessary to control momementum in ship movement.
	 *
	 * @param value the velocity of this ship
	 */
	public void setVelocity(Vector2 value) { 
		vel.set(value);
	}
	
	/** 
	 * Returns the angle that this ship is facing.
	 *
	 * The angle is specified in degrees, not radians.
	 *
	 * @return the angle of the ship
	 */
	public float getAngle() {
		return ang;
	}
	
	/** 
	 * Sets the angle that this ship is facing.
	 *
	 * The angle is specified in degrees, not radians.
	 *
	 * @param value the angle of the ship
	 */

	//TODO: change to 8 possible directions
	public void setAngle(float value) {
		ang = value;
	}

	/**
	 * Returns the tint color for this ship.
	 *
	 * We can change how an image looks without loading a new image by 
	 * tinting it differently.
	 *
	 * @return the tint color
	 */
	public Color getColor() {
		return tint;  
	}
	
	/**
	 * Sets the tint color for this ship.
	 *
	 * We can change how an image looks without loading a new image by 
	 * tinting it differently.
	 *
	 * @param value the tint color
	 */
	public void setColor(Color value) { 
		tint.set(value);
	}
	
	/**
	 * Resets the reload counter so the ship cannot fire again immediately.
	 *
	 * The ship must wait RELOAD_RATE steps before it can fire.
	 */
	public void reloadWeapon() {
		refire = 0;
	}

	/**
	 * Returns the mass of the ship.
	 *
	 * This value is necessary to resolve collisions.
	 *
	 * @return the ship mass
	 */
	public float getMass() {
		return mass;
	}

	/**
	 * Returns the diameter of the ship ship.
	 *
	 * This value is necessary to resolve collisions.
	 *
	 * @return the ship diameter
	 */
	public float getDiameter() {
		return this.size;
	}

	/**
	 * Returns the current oxygen level of the diver.
	 *
	 * Updated every cycle.
	 *
	 * @return the current oxygen level
	 */
	public float getOxygenLevel() { return this.oxygen_level; }

	/**
	 * Modifies the current oxygen level of the diver by delta.
	 */
	public void changeOxygenLevel(float delta) {
		this.oxygen_level += delta;
	}
	
	/**
	 * Creates a new ship at the given location with the given facing.
	 *
	 * @param x The initial x-coordinate of the center
	 * @param y The initial y-coordinate of the center
	 * @param ang The initial angle of rotation
	 */
    public Ship(float x, float y, float ang, int size, int type, int max_oxygen) {
        // Set the position of this ship.
        this.pos = new Vector2(x,y);
        this.ang = ang;
        this.size = size;

        // We start at rest.
        vel = new Vector2();
        dang = 0.0f;
        mass = 1.0f;

        // Currently no target sited.
//        tofs = new Vector2();
        refire = 0;

        //Set current ship image
        tint  = new Color(Color.WHITE);
        stint = new Color(0.0f,0.0f,0.0f,0.5f);

        this.type = type;

        this.MAX_OXYGEN = max_oxygen;
        this.oxygen_level = max_oxygen;
    }

	/**
	 * Moves the ship by the specified amount.  
	 * 
	 * Forward is the amount to move forward, while turn is the angle to turn the ship 
	 * (used for the "banking" animation. This method performs no collision detection.  
	 * Collisions are resolved afterwards.
	 *
	 * @param forward	Amount to move forward
	 * @param up		Amount to move upward/downward
	 */
	public void move(float forward, float up){
		// Process the ship turning.
//		processTurn(turn);

		// Process the ship thrust.
		if (up != 0.0f) {
			// Thrust key pressed; increase the ship velocity.
			vel.add(0, up);
			changeOxygenLevel(-.01f);
		}
		if (forward != 0.0f) {
			// Thrust key pressed; increase the ship velocity.
			vel.add(forward, 0);
			changeOxygenLevel(-.01f);
		}
//		else {
//			// Gradually slow the ship down
//			vel.scl(FORWARD_DAMPING);
//		}
		// Gradually slow the ship down
		vel.scl(FORWARD_DAMPING);
		vel.scl(speed);
		// Move the ship, updating it.
		// Adjust the angle by the change in angle
		ang += dang;  // INVARIANT: -360 < ang < 720                                                   
		if (ang > 360)
			ang -= 360;
		if (ang < 0)
			ang += 360;

		// Move the ship position by the ship velocity
		pos.add(vel);

    }

	/**
	 * Aim the target reticule at the opponent
	 *
	 * The target reticule always shows the location of our opponent.  In order
	 * to place it we need to know where our opponent is.  This method is
	 * called by the game engine to let us know the location of our
	 * opponent.
	 *
	 * @param other The opponent
	 */
//    public void acquireTarget(Ship other) {
//        // Calculate vector to 2nd ship
//        tofs.set(other.pos).sub(this.pos);  // tofs = other.pos - this.pos (and not a reference to other.pos)
//
//        // Scale it so we can draw it.
//        tofs.nor();
//        tofs.scl(TARGET_DIST);
//    }

	/**
	 * Draws the ship (and its related images) to the given GameCanvas. 
	 *
	 * You will want to modify this method for Exercise 4.
	 *
	 * This method uses alpha blending, which is set before this method is
	 * called (in GameMode).
	 *
	 * @param canvas The drawing canvas.
	 */
    public void drawShip(GameCanvas canvas) {
    	if (shipSprite == null) {
    		return;
    	}
		// For placement purposes, put origin in center.
        float ox = 0.5f * shipSprite.getRegionWidth();
        float oy = 0.5f * shipSprite.getRegionHeight();

        // How much to rotate the image
        float rotate = -(90+ang);

		// Draw the shadow.  Make a translucent color.
		// Position it offset by 10 so it can be seen.
        float sx = pos.x+SHADOW_OFFSET;
        float sy = pos.y+SHADOW_OFFSET;
        
        // Need to negate y scale because of coordinate access flip.
        // Draw the shadow first
		float ship_scale = this.getDiameter() / 80;
		canvas.draw(shipSprite, stint, ox, oy, sx, sy, rotate, ship_scale, ship_scale);
		// Then draw the ship
		canvas.draw(shipSprite, tint, ox, oy, pos.x, pos.y, rotate, ship_scale, ship_scale);
	}

	/**
	 * Draw the target cursor
	 *
	 * You will want to modify this method for Exercise 4.
	 *
	 * This method uses additive blending, which is set before this method is
	 * called (in GameMode).
	 *
	 * @param canvas The drawing canvas.
	 */
//	public void drawTarget(GameCanvas canvas) {
//		if (targetTexture == null) {
//			return;
//		}
//
//		// Target position
//		float tx = pos.x + tofs.x;
//		float ty = pos.y + tofs.y;
//
//		// For placement purposes, put origin in center.
//        float ox = 0.5f * TARGET_SIZE;
//        float oy = 0.5f * TARGET_SIZE;
//
//		canvas.draw(targetTexture, Color.WHITE, ox, oy, tx, ty, 0, DEFAULT_SCALE, DEFAULT_SCALE);
//    }

}
