/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.game;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.audio.SoundBuffer;
import com.xstudios.salvage.util.*;
//import com.xstudios.salvage.audio.SoundBuffer;

import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.assets.AssetManager;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements ModeController {
	/** Number of rows in the ship image filmstrip */
	private static final int SHIP_ROWS = 4;
	/** Number of columns in this ship image filmstrip */
	private static final int SHIP_COLS = 5;
	/** Number of elements in this ship image filmstrip */
	private static final int SHIP_SIZE = 18;
	/** Thickness of wall*/
	private static final int WALL_THICKNESS=15;

	/** container for walls*/
	private ObstacleContainer obstacleContainer;

	/** Texture of wall*/
	private Texture wallTexture;
	/** The background image for the battle */
	private Texture background;
	/** The image for a single proton */
	private Texture photonTexture;
	/** Texture for the ship (colored for each player) */
	private Texture shipTexture;
	/** Texture for the target reticule */
	private Texture targetTexture;
	/** Texture of light*/
	private Texture light;
	/** Radius of Light*/
	private float lightRadius = 0.95f;
	private  float defSpeed = .7f;
	private Texture bodyTexture;

	private Vector2 temp;

	private float MAX_DIST = 30f;

	private int ticks = 0;


    // Instance variables
	/** Read input for red player from keyboard or game pad (CONTROLLER CLASS) */
	protected InputController redController;
    /** Handle collision and physics (CONTROLLER CLASS) */
    protected CollisionController physicsController;

	/** Location and animation information for blue ship (MODEL CLASS) */
//	protected Ship shipBlue;
	/** Location and animation information for red ship (MODEL CLASS) */
	protected Ship shipRed;
	/** Shared memory pool for photons. (MODEL CLASS) */
	protected PhotonQueue photons;

	protected  DeadBody deadBody;

	/** Store the bounds to enforce the playing region */
	private Rectangle bounds;


	/** used to display messages to the screen */
	private BitmapFont displayFont;
	/** Offset for the oxygen message on the screen */
	private static final float TEXT_OFFSET   = 40.0f;

	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;
	/**
	 * Track the current state of the game for the update loop.
	 */
	public enum GameState {
		/** Before the game has started */
		INTRO,
		/** While we are playing the game */
		PLAY,
		/** When the ships is dead (but shells still work) */
		OVER
	}

	/**
	 * Creates a new game with a playing field of the given size.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 *
	 * @param width 	The width of the game window
	 * @param height 	The height of the game window
	 * @param assets	The asset directory containing all the loaded assets
	 */
	public GameMode(float width, float height, AssetDirectory assets) {
		// Extract the assets from the asset directory.  All images are textures.
		background = assets.getEntry("background", Texture.class );
		shipTexture = assets.getEntry( "diver", Texture.class );
		targetTexture = assets.getEntry( "target", Texture.class );
		photonTexture = assets.getEntry( "photon", Texture.class );
		wallTexture=assets.getEntry("wall", Texture.class);
		light = assets.getEntry("light", Texture.class);
		bodyTexture = assets.getEntry("body", Texture.class);

		//Initialize obstacle container
		obstacleContainer=new ObstacleContainer(wallTexture);

		//Initialize top wall
		obstacleContainer.addRectangle(0, height, width*2,2* WALL_THICKNESS);
////
////		//Initialize bottom wall
		obstacleContainer.addRectangle(0,0,width*2, 2*WALL_THICKNESS);
////
////		//Initialize left wall
		obstacleContainer.addRectangle(0,WALL_THICKNESS, WALL_THICKNESS*2, height*2);
////
////		//Initialize right wall
		obstacleContainer.addRectangle(width-WALL_THICKNESS, WALL_THICKNESS, 2*WALL_THICKNESS, height*2);

//		float wallHeight = height/3;
//
		obstacleContainer.addRectangle(width/4, height*0.4f, WALL_THICKNESS, height*0.2f);

//		obstacleContainer.addRectangle(100, 100, 240, 440);

		//Initialize lowest vertical wall
		obstacleContainer.addRectangle(150, 150, WALL_THICKNESS, height*0.2f);
//
//		//Initialize second-lowest vertical wall
		obstacleContainer.addRectangle(width*0.75f, height*0.3f, WALL_THICKNESS, height*0.2f);
//
//		//Initialize third lowest vertical wall
		obstacleContainer.addRectangle(width/4, height*0.6f, WALL_THICKNESS, height*0.3f);
//
//		//Initialize fourth lowest vertical wall
		obstacleContainer.addRectangle(width*0.6f, height*0.9f, WALL_THICKNESS, height*0.6f);
//
//		//Initialize last-minute vertical wall
		obstacleContainer.addRectangle(width*0.5f, height*0.1f, WALL_THICKNESS, height*0.3f);
//
//		//Initialize leftmost horizontal wall
		obstacleContainer.addRectangle(width*0.1f, height*0.6f, width*0.25f, WALL_THICKNESS);
//
//		//Initialize second-to-left horizontal wall
		obstacleContainer.addRectangle(width*0.305f, height*0.45f,width*0.15f, WALL_THICKNESS);
//
//		//Initialize third-to-left horizontal wall
		obstacleContainer.addRectangle(width*0.845f, height*0.4f, width*0.25f, WALL_THICKNESS);
//
//		//Initialize last minute horizontal wall
		obstacleContainer.addRectangle(width*0.9f, height*0.75f, width*0.15f, WALL_THICKNESS);




		// Initialize the photons.
		photons = new PhotonQueue();
		photons.setTexture(photonTexture);
		bounds = new Rectangle(0,0,width,height);

		// Create the two ships and place them across from each other.

        // Diver

		shipRed  = new Ship(100 ,100, 0, 40, 1, 100);

		shipRed.setFilmStrip(new FilmStrip(shipTexture,SHIP_ROWS,SHIP_COLS,SHIP_SIZE));
//		shipRed.setTargetTexture(targetTexture);
		shipRed.setColor(new Color(1.0f, 0.25f, 0.25f, 1.0f));  // Red, but makes texture easier to see

		// Body
		deadBody = new DeadBody(1200,575,0.5f);
		deadBody.setTexture(bodyTexture);
		// Create the input controllers.
		redController  = new InputController(0);
        physicsController = new CollisionController();

		gameState = GameState.INTRO;
        displayFont = assets.getEntry("times", BitmapFont.class);
		temp = new Vector2();
	}

	/**
	 * Read user input, calculate physics, and update the models.
	 *
	 * This method is HALF of the basic game loop.  Every graphics frame
	 * calls the method update() and the method draw().  The method update()
	 * contains all of the calculations for updating the world, such as
	 * checking for collisions, gathering input, and playing audio.  It
	 * should not contain any calls for drawing to the screen.
	 */
	@Override
	public void update() {
		// Read the keyboard for each controller.
		redController.readInput ();
		shipRed.setSpeed(redController.getSpeed()*defSpeed);

		// Move the ships forward (ignoring collisions)
		shipRed.move(redController.getForward(),   redController.getUp());
		photons.move(bounds);

		// This call handles BOTH ships.
		physicsController.checkInBounds(shipRed, bounds);

		// handles collisions of each ship with photons

		//physicsController.checkForCollision(shipRed, obstacleContainer);

//		physicsController.checkForObjectCollision(shipRed,deadBody);

		java.awt.Rectangle hit = obstacleContainer.getIntersectingObstacle(shipRed.getHitbox());
		if(hit!=null){
//			System.out.println("Aalskfashdfbasbdfjhasbdfa");
			physicsController.checkForCollision(shipRed,hit,redController.getForward(),redController.getUp());
		}

		// updates oxygen level
		shipRed.changeOxygenLevel(redController.getOxygenRate());

		if(redController.getOrDropObject() &&ticks % 10 == 0&&
				!shipRed.isCarryingObject() &&
				physicsController.checkForObjectCollision(shipRed, deadBody)){

				shipRed.setCarriedObject(true, deadBody);
		} else if (redController.getOrDropObject() && ticks % 10 == 0&&
				shipRed.isCarryingObject() ) {
			shipRed.setCarriedObject(false, null);
		}
		ticks++;
		// Test whether to reset the game.
		switch (gameState) {
			case INTRO:
				gameState = GameState.PLAY;
				break;
			case OVER:
				if (redController.didReset()) {
					gameState = GameState.PLAY;
//					shipRed.setOxygenLevel(shipRed.MAX_OXYGEN);
//					shipRed.setPosition(shipRed.getStartPosition());
					resetGame();
				}
				break;
			case PLAY:
				temp.set(shipRed.getPosition()).sub(shipRed.getStartPosition());
				if(shipRed.getOxygenLevel() <= 0 ||
						(shipRed.isCarryingObject() && temp.len() <= MAX_DIST)) {
					gameState = GameState.OVER;
//					deadBody.setDestroyed(false);
				} else if (redController.didReset()) {
					resetGame();
//					shipRed.setOxygenLevel(shipRed.MAX_OXYGEN);
//					shipRed.setPosition(shipRed.getStartPosition());
				}
				break;
			default:
				break;
		}

	}

	private void resetGame(){
		shipRed.setOxygenLevel(shipRed.MAX_OXYGEN);
		shipRed.setPosition(shipRed.getStartPosition());
		deadBody.setPosition(deadBody.getStartPosition().cpy());
		shipRed.setCarriedObject(false,null);
	}
	Vector2 mapPosition = new Vector2(0f,0f);

	/**
	 * Draw the game on the provided GameCanvas
	 *
	 * There should be no code in this method that alters the game state.  All
	 * assignments should be to local variables or cache fields only.
	 *
	 * @param canvas The drawing context
	 */
	@Override
	public void draw(GameCanvas canvas) {
		// could also use canvas.setColor()
		canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);

		canvas.drawMap(background, true,background.getWidth()/2,background.getHeight()/2-shipRed.getDiameter()/2);


		// First drawing pass (ships + shadows)
		shipRed.drawShip(canvas);

		// Draw Dead Body
		if(!deadBody.isDestroyed())
			deadBody.draw(canvas);

		// Second drawing pass (photons)
		canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);

		obstacleContainer.drawWalls(obstacleContainer.getAllObstacles(), canvas);

		canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);
		// Drawing Light and Darkness
		float diverX = shipRed.getPosition().x+shipRed.getDiameter()/2;
		float diverY = shipRed.getPosition().y+shipRed.getDiameter()/2;

		canvas.drawLight(light, diverX,diverY,redController.getLightRange()*lightRadius);

		//draw text
		canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);

		String msg = "Oxygen level: " + (int)shipRed.getOxygenLevel();
		System.out.println(msg);
		canvas.drawText(msg, displayFont, TEXT_OFFSET, canvas.getHeight()-TEXT_OFFSET);
		canvas.drawText("Light Level: "+redController.getLightRange()*lightRadius, displayFont, TEXT_OFFSET, canvas.getHeight()-TEXT_OFFSET*3);
		canvas.drawText("Speed: "+redController.getSpeed()*defSpeed, displayFont, TEXT_OFFSET, canvas.getHeight()-TEXT_OFFSET*4);
		canvas.drawText("Oxygen Depletion Rate: "+redController.getOxygenRate(), displayFont, TEXT_OFFSET, canvas.getHeight()-TEXT_OFFSET*2);
		canvas.drawText("Carrying Body: "+shipRed.isCarryingObject(), displayFont, TEXT_OFFSET, canvas.getHeight()-TEXT_OFFSET*5);
		if(deadBody.isDestroyed()) {
			canvas.drawTextCentered("You Won!", displayFont, 0);
		}
		if(gameState == GameState.OVER) {
			canvas.drawTextCentered("Press R to Restart", displayFont, -50);
		}

	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		// Garbage collection here is sufficient.  Nothing to do
	}



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
	public void resize(int width, int height) {
		bounds.set(0,0,width,height);
	}


}
