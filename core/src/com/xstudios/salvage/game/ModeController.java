/*
 * ModeController.java
 *
 * We will talk about the concept of a Player Mode the first few weeks
 * of class. A player mode is often like a self-contained game in
 * its own right. Therefore, it should have its own root (sub)controller.
 * Since the game will often switch back and forth between player modes
 * we would like the modes to have a uniform interface.  That way they
 * can all be handled the same by the true root controller, GDXRoot.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.game;

/**
 * Interface for the root class of a player mode.
 *
 * Each player mode is a game to itself and so it needs an update and a
 * draw cycle.  The class constructor allocates all necessary resources,
 * but the class must have an explicit dispose() to release resources
 * (do not depend on garbage collection)
 */
public interface ModeController {

	/** 
	 * Read user input, calculate physics, and update the models.
	 *
	 * This method is HALF of the basic game loop.  Every graphics frame 
	 * calls the method update() and the method draw().  The method update()
	 * contains all of the calculations for updating the world, such as
	 * checking for collisions, gathering input, and playing audio.  It
	 * should not contain any calls for drawing to the screen.
	 */
	public void update();
	
	/**
	 * Draw the game on the provided GameCanvas
	 *
	 * There should be no code in this method that alters the game state.  All 
	 * assignments should be to local variables or cache fields only.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas);
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose();

	/**
	 * Resize the window for this player mode to the given dimensions.
	 *
	 * This method is not guaranteed to be called when the player mode
	 * starts.  If the window size is important to the player mode, then
	 * these values should be passed to the constructor at start.
	 *
	 * @param width The width of the game window
	 * @param height The height of the game window
	 */
	public void resize(int width, int height);
}
