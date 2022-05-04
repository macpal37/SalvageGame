package com.xstudios.salvage.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** List of all WorldControllers */
	private GameController controller;

	private GameOverController game_over_controller;

	private MenuController menu_controller;

	private CameraController cameraController;

	private LevelSelectController level_select_controller;

	private SettingsController settings_controller;

	private Player player;

	private int current;

	private int total_levels;

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	@Override
	public void create () {
		current = 0;
		cameraController = new CameraController(32,18);
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

		loading.setScreenListener(this);
		game_over_controller.setScreenListener(this);
		menu_controller.setScreenListener(this);
		level_select_controller.setScreenListener(this);
		settings_controller.setScreenListener(this);

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

	public void set_menu(GameCanvas canvas, AssetDirectory directory){
		menu_controller.dispose();
		menu_controller.setDefaultCamera();
		menu_controller.gatherAssets(directory);
		menu_controller.setCanvas(canvas);
		menu_controller.setActive();
		setScreen(menu_controller);
	}

	public void set_game(GameCanvas canvas, AssetDirectory directory){
		controller.setLevel(current);
		controller.gatherAssets(directory);
		controller.setCanvas(canvas);
		controller.reset();
		setScreen(controller);
	}

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		//LOADING
		if (screen == loading) {
			System.out.println("loading");
			directory = loading.getAssets();
			loading.dispose();
			loading = null;

			player = new Player(directory);

			//game controller setup
			controller = new GameController(player);
			controller.setCameraController(cameraController);
			total_levels = controller.getTotalLevels();
			controller.setScreenListener(this);

			settings_controller.setPlayer(player);
			settings_controller.setAudio(controller.getAudio());

			//set up the cursor
			Pixmap pm = new Pixmap(Gdx.files.internal("core/assets/ui/cursor.png"));
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
			pm.dispose();

			//loading >> menu
			set_menu(canvas, directory);
		}
		//MENU
		else if (screen == menu_controller) {
			//menu >> level select
			if (exitCode == 0) {
				level_select_controller.setLocked(player.getLevel());
				level_select_controller.setTotalLevels(total_levels);
				level_select_controller.dispose();
				level_select_controller.gatherAssets(directory);
				level_select_controller.setCanvas(canvas);
				level_select_controller.setActive();
				setScreen(level_select_controller);
			}

			//menu >> setting
			if (exitCode == 1){
				settings_controller.dispose();
				settings_controller.gatherAssets(directory);
				settings_controller.setCanvas(canvas);
				settings_controller.setActive();
				setScreen(settings_controller);
			}

			//menu >> quit
			if (exitCode == 2) {
				player.save();
				Gdx.app.exit();
			}
		}
		//Setting
		else if (screen == settings_controller){

			//settings >> menu
			if(exitCode == 0){
				set_menu(canvas, directory);
			}

		}
		//GAME
		else if (screen == controller) {
			//pause >> menu
			if (exitCode == 2) {
				set_menu(canvas, directory);
			}

			//game >> game over
			else {
				game_over_controller.dispose();
				game_over_controller.setWin(exitCode == 0);
				game_over_controller.gatherAssets(directory);
				game_over_controller.setCanvas(canvas);
				setScreen(game_over_controller);
			}
		}
		//GAME OVER
		else if (screen == game_over_controller) {
			//game over >> restart
			System.out.println("in gameover");
			if (exitCode == 0)
				System.out.println("restart");
				set_game(canvas, directory);

			//if won update level
			if(game_over_controller.getWin()){
				current++;
				if(player.getLevel() == current)
					player.nextLevel();
			}

			//game over >> main menu
			if (exitCode == 1)
				set_menu(canvas, directory);

			//game over >> next level, will be main menu if no new level
			if(exitCode == 2){
				//main menu instead
				if(current >= total_levels)
					set_menu(canvas, directory);

				//next level
				else
					set_game(canvas, directory);
			}
		}

		//LEVEL SELECT
		else if (screen == level_select_controller) {
			//level select >> main menu
			if (exitCode == 0)
				set_menu(canvas, directory);

			//level select >> levels
			else {
				current = exitCode - 1;

				//go to menu instead
				if(current > controller.getTotalLevels() - 1)
					set_menu(canvas, directory);

				//go to levels
				else
					set_game(canvas, directory);
			}
		}
	}
}
