/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX; it must extend ApplicationAdapter to work properly. 
 *
 * We prefer to keep this class fairly lightweight.  We want the ModeControllers to
 * do the hard work.  This class should just schedule the ModeControllers and allow
 * the player to switch between them. We will see more on this in a later lab. 
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package com.xstudios.salvage.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.ApplicationAdapter;
<<<<<<< HEAD
import com.badlogic.gdx.graphics.*;
import com.xstudios.salvage.assets.AssetDirectory;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 *
 * All of the methods of ApplicationAdapter are extremely important.  You should study
 * how each one is used.
 */
public class GDXRoot extends ApplicationAdapter {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	
	/** Drawing context to display graphics (VIEW CLASS) */
	GameCanvas  canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	LoadingMode loading;
	/** Player mode for the the game proper (CONTROLLER CLASS) */
	GameMode    playing;
	/** Polymorphic reference to the active player mode */
	ModeController controller;

	/**
	 * Creates a new game application root
	 */
	public GDXRoot() {}
	
	/** 
	 * Called when the Application is first created.
	 * 
	 * This method should always initialize the drawing context and begin asset loading.
	 */
	@Override
	public void create () {
		// Create the drawing context
		canvas  = new GameCanvas();
		
		// Start loading with the asset manager
		loading = new LoadingMode("assets.json",1);
		playing = null; // No game just yet
		// Make the loading screen the active player mode
		controller = loading;
	}
	
	/**
	 * Called when the Application should render itself.
	 *
	 * In class we will talk about breaking the game loop into two parts: the update
	 * and the draw part.  In LibGDX, these are lumped together into a single step:
	 * render().  We do not like this organization, so we have split this up for the
	 * ModeControllers.  In particular, it allows us to add special code in between
	 * update() and draw().
	 */
	@Override
	public void render () {
		if (loading != null && loading.isReady()) {
			directory = loading.getAssets();
			loading.dispose(); // This will NOT dispose the assets.
			loading = null;
			controller = new GameMode(canvas.getWidth(),canvas.getHeight(),directory);
		}
		
		// Update the game state
		controller.update();
		
		// Draw the game
		Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		canvas.begin();
		controller.draw(canvas);
		canvas.end();
	}
	
	/**
	 * Called when the Application is destroyed.
	 *
	 * It is your responsibility to dispose of all assets when this happened.  Relying
	 * on Java garbage collection is NOT GOOD ENOUGH.  If you loaded any assets, you 
	 * must unload them.
	 */
	@Override
	public void dispose() {
		controller.dispose();
=======
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	/** List of all WorldControllers */
	private LevelController[] controllers;

	private CameraController cameraController;

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	@Override
	public void create () {
		cameraController = new CameraController(32,18);
		canvas = new GameCanvas(cameraController);
		loading = new LoadingMode("assets.json", canvas, 1);
		// Initialize the three game worlds
		controllers = new LevelController[1];
		controllers[0] = new TestLevelController();
		controllers[0].setCameraController(cameraController);
		current = 0;
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	@Override
	public void dispose () {
		setScreen(null);
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].dispose();
		}

		canvas.dispose();
		canvas = null;

		// Unload all of the resources
		// Unload all of the resources
>>>>>>> ce1ef31a1771b0f028ee781f41846a30b0980a12
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
<<<<<<< HEAD
	}
	
	/**
	 * Called when the Application is resized.
	 * 
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create()
	 *
	 * @param width The window width
	 * @param height The window height
	 */	 
	@Override
	public void resize(int width, int height) {
		if (controller != null) {
			controller.resize(width,height);
		}
		// Canvas knows the size, but not that it changed
		canvas.resize();
=======
		super.dispose();

	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
		cameraController.resize(width,height);
	}

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {

			directory = loading.getAssets();
			controllers[0].gatherAssets(directory);
			controllers[0].setCanvas(canvas);
			controllers[current].reset();

			setScreen(controllers[current]);

			loading.dispose();
			loading = null;
		}
>>>>>>> ce1ef31a1771b0f028ee781f41846a30b0980a12
	}
}
