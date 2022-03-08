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

	/** Impulse for giving collisions a slight bounce. */
	public static final float COLLISION_COEFF = 0.1f;
	
	/** Caching object for computing normal */
	private Vector2 normal;

	/** Caching object for computing net velocity */
	private Vector2 velocity;
	
	/** Caching object for intermediate calculations */
	private Vector2 temp;

	/**
     * Contruct a new controller. 
     * 
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
	public CollisionController() { 
		velocity = new Vector2();
		normal = new Vector2();
		temp = new Vector2();
	}

	/** 
	 *  Handles collisions between ships, causing them to bounce off one another.
	 * 
	 *  This method updates the velocities of both ships: the collider and the 
	 *   Therefore, you should only call this method for one of the
	 *  ships, not both. Otherwise, you are processing the same collisions twice.
	 * 
	 *
	 */
	public void checkForCollision(Ship ship, ObstacleContainer o) {
		// Calculate the normal of the (possible) point of collision
		float diverX = ship.getPosition().x;
		float diverY = ship.getPosition().y;

		Array<Rectangle> obstacle = o.getAllObstacles();

		for (int ii = 0; ii < obstacle.size; ii++) {
			Rectangle wall = obstacle.get(ii);

			normal.set(ship.getPosition()).sub(wall.getX(), wall.getY());

			float distance = normal.len();

			float impactDistanceW = (ship.getDiameter() + wall.getWidth()) / 2f;
			float impactDistanceH = (ship.getDiameter() + wall.getHeight()) / 2f;

			normal.nor();
			float impactDistance;

			if (impactDistanceW < impactDistanceH) impactDistance = impactDistanceW;
			else impactDistance = impactDistanceH;

			if (distance < impactDistance) {
				temp.set(normal).scl((impactDistance - distance) / 2);
				ship.getPosition().add(temp);

				temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
				wall.getPosition(new Vector2(wall.getX(), wall.getY())).sub(temp);

				velocity.set(ship.getVelocity()).sub(new Vector2(0, 0));

				float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
						(normal.dot(normal) * (1 / ship.getMass() + 1 / 3.0f));

				temp.set(normal).scl(impulse / ship.getMass());
				ship.getVelocity().add(temp);
			}
		}
	}

	/**
	 * Nudge the ship to ensure it does not do out of view.
	 *
	 * This code bounces the ship off walls.  You will replace it as part of
	 * the lab.
	 *
	 * @param ship		They player's ship which may have collided
	 * @param bounds	The rectangular bounds of the playing field
	 */
	public void checkInBounds(Ship ship, Rectangle bounds) {
		//Ensure the ship doesn't go out of view. Bounce off walls.
		float rad = ship.getDiameter()/2;
		if (ship.getPosition().x <= bounds.x+rad) {
			ship.getPosition().set(bounds.x+rad,ship.getPosition().y);
		} else if (ship.getPosition().x >= bounds.width-rad) {
			ship.getPosition().set(bounds.width-rad,ship.getPosition().y);

		}

		if (ship.getPosition().y <= bounds.y+rad) {
			ship.getPosition().set(ship.getPosition().x,bounds.y+rad);
		} else if (ship.getPosition().y >= bounds.height-rad) {
			ship.getPosition().set(ship.getPosition().x,bounds.height-rad);
		}
	}

	public void checkForObjectCollision(Ship ship, GameObject obj){
		float diverX = ship.getPosition().x;
		float diverY = ship.getPosition().y;
		if(diverX >= obj.getX()-obj.getRadius() && diverX <= obj.getX()+obj.getRadius()&&
				diverY >= obj.getY()-obj.getRadius() && diverY <= obj.getY()+obj.getRadius()){
			obj.setDestroyed(true);
		}


	}
//
//	public void checkForCollision(Ship ship, PhotonQueue photon_q){
//		// iterate over each photon
//		for (int ii = 0; ii < photon_q.size; ii++) {
//			// Find the position of this photon.
//			int idx = ((photon_q.head + ii) % photon_q.MAX_PHOTONS);
//			// Find the photon
//			PhotonQueue.Photon photon = photon_q.queue[idx];
//
//			// if the ship has photons of its own type, ignore them
//			if (ship.type != photon.type) {
//
//				// Calculate the normal of the (possible) point of collision
//				Vector2 photon_position = new Vector2(photon.x, photon.y);
//				Vector2 photon_velocity = new Vector2(photon.vx, photon.vy);
//				normal.set(ship.getPosition()).sub(photon_position);
//				float distance = normal.len();
//				float impactDistance = (ship.getDiameter() + photon_q.getTexture().getHeight()) / 2f;
//				normal.nor();
//
//				// If this normal is too small, there was a collision
//				if (distance < impactDistance) {
//					// "Roll back" time so that the ships are barely touching (e.g. point of impact).
//					// We need to use temp, as the method scl would change the contents of normal!
//					temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
//					ship.getPosition().add(temp);
//
//					temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
//					photon_position.sub(temp);
//
//					// Now it is time for Newton's Law of Impact.
//					// Convert the two velocities into a single reference frame
//					velocity.set(ship.getVelocity()).sub(photon_velocity); // v1-v2
//
//					// Compute the impulse (see Essential Math for Game Programmers)
//					float photon_mass = 1.0f;
//					float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
//							(normal.dot(normal) * (1 / ship.getMass() + 1 / photon_mass));
//
//					// Change velocity of the two ships using this impulse
//					temp.set(normal).scl(impulse / ship.getMass());
//					ship.getVelocity().add(temp);
//
//					temp.set(normal).scl(impulse / photon_mass);
//					photon.vx -= temp.x;
//					photon.vy -= temp.y;
//				}
//			}
//		}
	}
