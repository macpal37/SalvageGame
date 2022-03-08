/*
 * CollisionController.java
 *
 * Unless you are making a point-and-click adventure game, every single
 * game is going to need some sort of collision detection.  In a later
 * lab, we will see how to do this with a physics engine. For now, we use
 * custom physics.
 *
 * This class is an example of subcontroller.  A lot of this functionality
 * could go into GameMode (which is the primary controller).  However, we
 * have factored it out into a separate class because it makes sense as a
 * self-contained subsystem.  Note that this class needs to be aware of
 * of all the models, but it does not store anything as fields.  Everything
 * it needs is passed to it by the parent controller.
 *
 * This class is also an excellent example of the perils of heap allocation.
 * Because there is a lot of vector mathematics, we want to make heavy use
 * of the Vector2 class.  However, every time you create a new Vector2
 * object, you must allocate to the heap.  Therefore, we determine the
 * minimum number of objects that we need and pre-allocate them in the
 * constructor.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

/**
 * Controller implementing simple game physics.
 *
 * This is the simplest of physics engines.  In later labs, we
 * will see how to work with more interesting engines.
 */
public class CollisionController {

	/**
	 * Impulse for giving collisions a slight bounce.
	 */
	public static final float COLLISION_COEFF = 0.1f;

	/**
	 * Caching object for computing normal
	 */
	private Vector2 normal;

	/**
	 * Caching object for computing net velocity
	 */
	private Vector2 velocity;

	/**
	 * Caching object for intermediate calculations
	 */
	private Vector2 temp;

	/**
	 * Contruct a new controller.
	 * <p>
	 * This constructor initializes all the caching objects so that
	 * there is no heap allocation during collision detection.
	 */
	public CollisionController() {
		velocity = new Vector2();
		normal = new Vector2();
		temp = new Vector2();
	}

	/**
	 * Handles collisions between ships, causing them to bounce off one another.
	 * <p>
	 * This method updates the velocities of both ships: the collider and the
	 * Therefore, you should only call this method for one of the
	 * ships, not both. Otherwise, you are processing the same collisions twice.
	 * <p>
	 * <<<<<<< HEAD
	 */


	public void checkForCollision(Ship ship, java.awt.Rectangle wall, float x, float y) {
		float rad = ship.getDiameter() / 2;
		float xrad = wall.width / 2;
		float yrad = wall.height / 2;


		int a = 6;
		if (ship.getPosition().x - wall.x < 0 && x==1) {
			ship.move(-1, 0);
			ship.move(-a, 0);
		}else
		if (ship.getPosition().x - wall.x > 0 && x==-1) {
			ship.move(1, 0);
			ship.move(a, 0);
		}

		if (ship.getPosition().y - wall.y < 0 && y == 1) {
			ship.move(0, -1);
			ship.move(0, -a);
		} else if (ship.getPosition().y - wall.y > 0 && y == -1) {
			ship.move(0, 1);
			ship.move(0, a);
		}
	}

	/**
	 * Nudge the ship to ensure it does not do out of view.
	 * <p>
	 * This code bounces the ship off walls.  You will replace it as part of
	 * the lab.
	 *
	 * @param ship   They player's ship which may have collided
	 * @param bounds The rectangular bounds of the playing field
	 */
	public void checkInBounds(Ship ship, Rectangle bounds) {
		//Ensure the ship doesn't go out of view. Bounce off walls.
		float rad = ship.getDiameter() / 2;
		if (ship.getPosition().x <= bounds.x + rad) {
			ship.getPosition().set(bounds.x + rad, ship.getPosition().y);
		} else if (ship.getPosition().x >= bounds.width - rad) {
			ship.getPosition().set(bounds.width - rad, ship.getPosition().y);
		}
		if (ship.getPosition().y <= bounds.y + rad) {
			ship.getPosition().set(ship.getPosition().x, bounds.y + rad);
		} else if (ship.getPosition().y >= bounds.height - rad) {
			ship.getPosition().set(ship.getPosition().x, bounds.height - rad);
		}
	}

	public boolean checkForObjectCollision(Ship ship, GameObject obj){

		float diverX = ship.getPosition().x;
		float diverY = ship.getPosition().y;
		if(diverX >= obj.getX()-obj.getRadius() && diverX <= obj.getX()+obj.getRadius()&&
				diverY >= obj.getY()-obj.getRadius() && diverY <= obj.getY()+obj.getRadius()){
			//obj.setDestroyed(true);
			return true;
		}
	return false;

	}
}
