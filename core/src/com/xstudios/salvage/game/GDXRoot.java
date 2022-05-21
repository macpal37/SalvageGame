package com.xstudios.salvage.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.audio.AudioController;
import com.xstudios.salvage.util.ScreenListener;

public class GDXRoot extends Game implements ScreenListener {
	/**
	 * AssetManager to load game assets (textures, sounds, etc.)
	 */
	AssetDirectory directory;
	/**
	 * Drawing context to display graphics (VIEW CLASS)
	 */
	private GameCanvas canvas;
	/**
	 * Player mode for the asset loading screen (CONTROLLER CLASS)
	 */
	private LoadingMode loading;
	/**
	 * List of all WorldControllers
	 */
	private GameController controller;

	private GameOverController game_over_controller;

	private MenuController menu_controller;

	private CameraController cameraController;

	private CameraController gameCameraController;

	private LevelSelectController level_select_controller;

	private SettingsController settings_controller;

	private RulesController rules_controller;

	private Player player;

	private int current;

	private int total_levels;

	/**
	 * Called when the Application is first created.
	 * <p>
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	@Override
	public void create() {
		current = 0;
		cameraController = new CameraController(32, 18);

//		gameCameraController = new CameraController(32, 18);

		canvas = new GameCanvas(cameraController);
		loading = new LoadingMode("assets.json", canvas, 1, cameraController);

		game_over_controller = new GameOverController();
		game_over_controller.setCameraController(cameraController);

		menu_controller = new MenuController();
		menu_controller.setCameraController(cameraController);

		settings_controller = new SettingsController();
		settings_controller.setCameraController(cameraController);

		level_select_controller = new LevelSelectController();
		level_select_controller.setCameraController(cameraController, canvas.getWidth(), canvas.getHeight());

		rules_controller = new RulesController();
		rules_controller.setCameraController(cameraController);

		loading.setScreenListener(this);
		game_over_controller.setScreenListener(this);
		menu_controller.setScreenListener(this);
		level_select_controller.setScreenListener(this);
		settings_controller.setScreenListener(this);
		rules_controller.setScreenListener(this);

		AudioController.getInstance().loading_screen();

		setScreen(loading);

	}

	/**
	 * Called when the Application is destroyed.
	 * <p>
	 * This is preceded by a call to pause().
	 */
	@Override
	public void dispose() {
		setScreen(null);
		controller.dispose();

		canvas.dispose();
		canvas = null;

		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();

	}

	/**
	 * Called when the Application is resized.
	 * <p>
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width, height);
		cameraController.resize(width, height);
	}

	// setting the game requires reseting AFTER gathering assets, so we have a separate function
	// (may need to change later)
	public void set_game(AssetDirectory directory, GameCanvas canvas) {
		controller.setLevel(current);
		controller.gatherAssets(directory);
		controller.setCanvas(canvas);
		// rest also loads the level in, probably should change naming...
		controller.reset();
		setScreen(controller);
	}

	/**
	 * Method for setting/switching to a screen
	 *
	 * @param screen
	 * @param directory
	 * @param canvas
	 */
	public void switch_screen(ScreenController screen, AssetDirectory directory, GameCanvas canvas) {
		screen.gatherAssets(directory);
		screen.setCanvas(canvas);
		setScreen(screen);
	}

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		//LOADING
		if (screen == loading) {
			// loads and sets all assets (textures, constants, player json) for the game
			System.out.println("loading");
			directory = loading.getAssets();
			loading.dispose();
			loading = null;
			System.out.println("loaded");
			// loads player, which stores info about the current save and settings
			player = new Player(directory);
			System.out.println("player_loaded");
			//game controller setup

			AudioController.getInstance().setUp(player.getMusic(), player.getSoundEffects());

			controller = new GameController();
			controller.setCameraController(cameraController);
			total_levels = controller.getTotalLevels();
			controller.setScreenListener(this);

			settings_controller.setPlayer(player);

			//set up the cursor
			Pixmap pm = new Pixmap(Gdx.files.internal("ui/cursor.png"));
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
			pm.dispose();

//			if(!AudioController.getInstance().is_loading()){
//				switch_screen(menu_controller, directory, canvas);
//			}

			//loading >> menu
			AudioController.getInstance().initialize();
			switch_screen(menu_controller, directory, canvas);
		}
		//MENU
		else if (screen == menu_controller) {
			// upon leaving the menu_controller we dispose
			menu_controller.dispose();
			menu_controller.setCameraPositionNormal();
			//menu >> level select
			if (exitCode == 0) {
				// add the dispose to the if screen is x_controller and exit screen is called
				// if going to level select, we get the assets, set canvas and setActive (not sure if we need this)
				level_select_controller.setLocked(player.getLevel());
				level_select_controller.setTotalLevels(total_levels);
				switch_screen(level_select_controller, directory, canvas);
			}

			//menu >> setting
			if (exitCode == 1) {
				switch_screen(settings_controller, directory, canvas);
			}

			//menu >> quit
			if (exitCode == 2) {
				player.save();
				Gdx.app.exit();
			}

			if(exitCode == 3){
				switch_screen(rules_controller, directory, canvas);
			}
		}
		//Rules
		else if (screen == rules_controller){
			rules_controller.dispose();

			//rules >> menu
			if(exitCode == 0){
				switch_screen(menu_controller, directory, canvas);
			}
		}
		//Setting
		else if (screen == settings_controller) {
			settings_controller.dispose();

			//settings >> menu
			if (exitCode == 0) {
				switch_screen(menu_controller, directory, canvas);
			}


		}
		//GAME
		else if (screen == controller) {
			//pause >> menu
			controller.setDefaultPosition();
			if (exitCode == 2) {
				AudioController.getInstance().reset();
				switch_screen(menu_controller, directory, canvas);
			}

			//game >> game over
			else {
				game_over_controller.setWin(exitCode == 0);
				switch_screen(game_over_controller, directory, canvas);
			}
		}
		//GAME OVER
		else if (screen == game_over_controller) {
			System.out.println("in gameover");
			//if won, update level progress
			if (game_over_controller.getWin()) {
				// set the current level to be the next level if player clicks next level button
				current++;
				// if beat a new level, store that information in the save file
				if (player.getLevel() == current)
					player.nextLevel();
			}
			// reset game over controller and switch to a new screen
			game_over_controller.dispose();

			//game over >> restart
			if (exitCode == 0) {
				System.out.println("restart");
//				controller.setLevel(current);
				controller.reset();
				set_game(directory, canvas);
			}

			//game over >> main menu
			if (exitCode == 1)
				switch_screen(menu_controller, directory, canvas);

			//game over >> next level, will be main menu if next level doesn't exist
			if (exitCode == 2) {
				//main menu instead
				AudioController.getInstance().reset();
				if (current >= total_levels)
					switch_screen(menu_controller, directory, canvas);
					//next level
				else
					set_game(directory, canvas);
			}
		}

		//LEVEL SELECT
		else if (screen == level_select_controller) {
			level_select_controller.dispose();
			//level select >> main menu
			if (exitCode == 0)
				switch_screen(menu_controller, directory, canvas);

				//level select >> levels
			else {
				current = exitCode - 1;

				//go to menu instead
				if (current > controller.getTotalLevels() - 1)
					switch_screen(menu_controller, directory, canvas);

					//go to levels
				else {
					System.out.println("level select >> game");
					set_game(directory, canvas);
				}
			}
		}
	}
}