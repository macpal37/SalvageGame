package com.xstudios.salvage.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

	private CameraController levelController;

	private LevelSelectController level_select_controller;

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

		controller = new GameController();
		controller.setCameraController(cameraController);

		game_over_controller = new GameOverController(controller.getWorldBounds());
		menu_controller = new MenuController();

		levelController = new CameraController(100, 50);
		level_select_controller = new LevelSelectController();
		level_select_controller.setCameraController(cameraController, canvas.getWidth(), canvas.getHeight());

		loading.setScreenListener(this);
		controller.setScreenListener(this);
		game_over_controller.setScreenListener(this);
		menu_controller.setScreenListener(this);
		level_select_controller.setScreenListener(this);

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

		// Unload all of the resources
		// Unload all of the resources
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

	@Override
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			directory = loading.getAssets();
			if(exitCode == 0){
				menu_controller.gatherAssets(directory);
				menu_controller.setCanvas(canvas);
				menu_controller.setActive();
				setScreen(menu_controller);
			}
			loading.dispose();
			loading = null;
		}
		else if(screen == controller){
			game_over_controller.create();
			if(directory == null) {
				System.out.println("DIRECTORY IS NULL!");
			}
			game_over_controller.gatherAssets(directory);
			game_over_controller.setCanvas(canvas);
			game_over_controller.setTextPos(
					cameraController.getCameraPosition2D().x,
					cameraController.getCameraPosition2D().y);
			game_over_controller.setWin(exitCode == 0);
			setScreen(game_over_controller);
		}
		else if(screen == game_over_controller){
			controller.setCanvas(canvas);
			controller.reset();
			setScreen(controller);
		}
		else if(screen == menu_controller){
			if(exitCode == 0){
				level_select_controller.dispose();
				level_select_controller.gatherAssets(directory);
				level_select_controller.setCanvas(canvas);
				level_select_controller.setActive();
				setScreen(level_select_controller);
			}
			if(exitCode == 1){
				controller.gatherAssets(directory);
				controller.setCanvas(canvas);
				controller.reset();

				setScreen(controller);
			}
			if(exitCode == 2) {
				Gdx.app.exit();
			}

		}
		else if(screen == level_select_controller){
			if(exitCode == 0){
				menu_controller.dispose();
				menu_controller.gatherAssets(directory);
				menu_controller.setCanvas(canvas);
				menu_controller.setActive();
				setScreen(menu_controller);
			}
			else{
				controller.setLevel(exitCode - 1);
				controller.gatherAssets(directory);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(controller);
			}
		}
	}
}
