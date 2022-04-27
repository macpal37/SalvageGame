package com.xstudios.salvage.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.util.ScreenListener;

import java.awt.*;

public class GDXRoot extends Game implements ScreenListener {

//    /**
//     * AssetManager to load game assets (textures, sounds, etc.)
//     */
//    AssetDirectory directory;
//    /**
//     * Drawing context to display graphics (VIEW CLASS)
//     */
//    private GameCanvas canvas;
//    /**
//     * Player mode for the asset loading screen (CONTROLLER CLASS)
//     */
//    private LoadingMode loading;
//    /**
//     * List of all WorldControllers
//     */
//    private GameController controller;
//
//    private GameOverController game_over_controller;
//
//    private MenuController menu_controller;
//
//    private CameraController cameraController;
//
////    private CameraController levelCameraController;
//
//    private LevelSelectController level_select_controller;
//
//    /**
//     * Called when the Application is first created.
//     * <p>
//     * This is method immediately loads assets for the loading screen, and prepares
//     * the asynchronous loader for all other assets.
//     */
//    @Override
//    public void create() {
////        cameraController = new CameraController(32, 18);
//        cameraController = new CameraController(64, 36);
//        canvas = new GameCanvas(cameraController);
//        loading = new LoadingMode("assets.json", canvas, 1);
//
//        controller = new GameController();
//        controller.setCameraController(cameraController);
//
//        game_over_controller = new GameOverController(controller.getWorldBounds());
//        menu_controller = new MenuController();
//
//        level_select_controller = new LevelSelectController();
//
//        level_select_controller.setCameraController(cameraController, canvas.getWidth(), canvas.getHeight());
//
//        loading.setScreenListener(this);
//        controller.setScreenListener(this);
//        game_over_controller.setScreenListener(this);
//        menu_controller.setScreenListener(this);
//        level_select_controller.setScreenListener(this);
//
//        setScreen(loading);
//    }
//
//    /**
//     * Called when the Application is destroyed.
//     * <p>
//     * This is preceded by a call to pause().
//     */
//    @Override
//    public void dispose() {
//        setScreen(null);
//        controller.dispose();
//
//        canvas.dispose();
//        canvas = null;
//
//        // Unload all of the resources
//        // Unload all of the resources
//        if (directory != null) {
//            directory.unloadAssets();
//            directory.dispose();
//            directory = null;
//        }
//        super.dispose();
//
//    }
//
//    /**
//     * Called when the Application is resized.
//     * <p>
//     * This can happen at any point during a non-paused state but will never happen
//     * before a call to create().
//     *
//     * @param width  The new width in pixels
//     * @param height The new height in pixels
//     */
//    public void resize(int width, int height) {
//
//        canvas.resize();
//        super.resize(width, height);
//        cameraController.resize(width, height);
//    }
//
//    @Override
//    public void exitScreen(Screen screen, int exitCode) {
//
//
//        System.out.println("CAMERA: " + cameraController.getCameraPosition2D().toString());
////        cameraController.setCameraPosition(720.0f, 450.0f - 50);
//        cameraController.setCameraPosition(640.0f, 360.0f);
//
//        if (screen == loading) {
//            directory = loading.getAssets();
//            if (exitCode == 0) {
//                menu_controller.gatherAssets(directory);
//                menu_controller.setCanvas(canvas);
//                menu_controller.setActive();
//                setScreen(menu_controller);
//            }
//            loading.dispose();
//            loading = null;
//        } else if (screen == controller) {
//            if (exitCode <= 1) {
//                game_over_controller.create();
//                game_over_controller.gatherAssets(directory);
//                game_over_controller.setCanvas(canvas);
//                game_over_controller.setTextPos(
//                        cameraController.getCameraPosition2D().x,
//                        cameraController.getCameraPosition2D().y);
//                game_over_controller.setWin(exitCode == 0);
//                setScreen(game_over_controller);
//            }
//            if (exitCode == 2) {
//
//                level_select_controller.dispose();
//                level_select_controller.gatherAssets(directory);
//                level_select_controller.setCanvas(canvas);
//                level_select_controller.setActive();
////                level_select_controller.readjustCamera();
//
//                setScreen(level_select_controller);
//
//
//            }
//        } else if (screen == game_over_controller) {
//            controller.setCanvas(canvas);
//            controller.reset();
//            setScreen(controller);
//        } else if (screen == menu_controller) {
//            if (exitCode == 0) {
//                level_select_controller.dispose();
//                level_select_controller.gatherAssets(directory);
//                level_select_controller.setCanvas(canvas);
//                level_select_controller.setActive();
//                setScreen(level_select_controller);
//            }
//            if (exitCode == 1) {
//                controller.gatherAssets(directory);
//                controller.setCanvas(canvas);
//                controller.reset();
//
//                setScreen(controller);
//            }
//            if (exitCode == 2) {
//                Gdx.app.exit();
//            }
//
//        } else if (screen == level_select_controller) {
//            if (exitCode == 0) {
//                menu_controller.dispose();
//                menu_controller.gatherAssets(directory);
//                menu_controller.setCanvas(canvas);
//                menu_controller.setActive();
//                setScreen(menu_controller);
//            } else {
//                controller.setLevel(exitCode - 1);
//                controller.gatherAssets(directory);
//                controller.setCanvas(canvas);
//                controller.reset();
//                setScreen(controller);
//            }
//        }
//    }

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

    private LevelSelectController level_select_controller;

    private Player player;

    private Pixmap pm;
    private Pixmap no_pm;
    private int current;

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
        canvas = new GameCanvas(cameraController);
        loading = new LoadingMode("assets.json", canvas, 1);

        controller = new GameController();
        controller.setCameraController(cameraController);

        game_over_controller = new GameOverController(controller.getWorldBounds());
        menu_controller = new MenuController();

        level_select_controller = new LevelSelectController();
        level_select_controller.setCameraController(cameraController, canvas.getWidth(), canvas.getHeight());

        loading.setScreenListener(this);
        controller.setScreenListener(this);
        game_over_controller.setScreenListener(this);
        menu_controller.setScreenListener(this);
        level_select_controller.setScreenListener(this);


        pm = new Pixmap(Gdx.files.internal("ui/cursor.png"));
        no_pm = new Pixmap(Gdx.files.internal("ui/no_cursor.png"));
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));

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
        pm.dispose();
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

    @Override
    public void exitScreen(Screen screen, int exitCode) {
        if (screen == loading) {
            System.out.println("loading");
            directory = loading.getAssets();
            player = new Player(directory);
            if (exitCode == 0) {
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
                controller.setCameraPositionNormal();
                menu_controller.dispose();
                menu_controller.gatherAssets(directory);
                menu_controller.setCanvas(canvas);
                menu_controller.setActive();
                setScreen(menu_controller);
            }
            loading.dispose();
            loading = null;
        } else if (screen == controller) {
            if (exitCode == 2) {
                controller.setCameraPositionNormal();
                menu_controller.dispose();
                menu_controller.gatherAssets(directory);
                menu_controller.setCanvas(canvas);
                menu_controller.setActive();
                setScreen(menu_controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
            } else {
                System.out.println("controller");
                System.out.println(exitCode);
                System.out.println("this happened");
                controller.setCameraPositionNormal();
                game_over_controller.dispose();
                if (directory == null) {
                    System.out.println("DIRECTORY IS NULL!");
                }
                game_over_controller.setWin(exitCode == 0);
                game_over_controller.gatherAssets(directory);
                game_over_controller.create();
                game_over_controller.setCanvas(canvas);
                setScreen(game_over_controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
            }

        } else if (screen == game_over_controller) {
            System.out.println("game_over_controller");
            if (exitCode == 0) {
                controller.setCanvas(canvas);
                controller.reset();
                setScreen(controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(no_pm, 0, 0));
            }
            if (exitCode == 1) {
                controller.setCameraPositionNormal();
                menu_controller.dispose();
                menu_controller.gatherAssets(directory);
                menu_controller.setCanvas(canvas);
                menu_controller.setActive();
                setScreen(menu_controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
            }
            if (exitCode == 2) {
                System.out.println("current: " + current);
                if (current > 0) {
                    game_over_controller.dispose();
                    controller.setCameraPositionNormal();
                    menu_controller.dispose();
                    menu_controller.gatherAssets(directory);
                    menu_controller.setCanvas(canvas);
                    menu_controller.setActive();
                    setScreen(menu_controller);
                    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
                } else {
                    current++;
                    if (player.getLevel() == current) player.nextLevel();
                    player.save();
                    controller.setLevel(current);
                    controller.gatherAssets(directory);
                    controller.setCanvas(canvas);
                    controller.reset();

                    setScreen(controller);
                    Gdx.graphics.setCursor(Gdx.graphics.newCursor(no_pm, 0, 0));
                }
            }
        } else if (screen == menu_controller) {
            if (exitCode == 0) {
                level_select_controller.setLocked(player.getLevel());
                level_select_controller.dispose();
                level_select_controller.gatherAssets(directory);
                level_select_controller.setCanvas(canvas);
                level_select_controller.setActive();
                setScreen(level_select_controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
            }
            if (exitCode == 1) {
                controller.gatherAssets(directory);
                controller.setCanvas(canvas);
                controller.reset();

                setScreen(controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(no_pm, 0, 0));
            }
            if (exitCode == 2) {
                player.save();
                Gdx.app.exit();
            }

        } else if (screen == level_select_controller) {
            if (exitCode == 0) {
                controller.setCameraPositionNormal();
                menu_controller.dispose();
                menu_controller.gatherAssets(directory);
                menu_controller.setCanvas(canvas);
                menu_controller.setActive();
                setScreen(menu_controller);
                Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
            } else {
                System.out.println("exitCode= " + exitCode);
                current = exitCode - 1;
                if (current > 1) {
                    controller.setCameraPositionNormal();
                    menu_controller.dispose();
                    menu_controller.gatherAssets(directory);
                    menu_controller.setCanvas(canvas);
                    menu_controller.setActive();
                    setScreen(menu_controller);
                    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0));
                } else {
                    controller.setLevel(exitCode - 1);
                    controller.gatherAssets(directory);
                    controller.setCanvas(canvas);
                    controller.reset();
                    setScreen(controller);
                    Gdx.graphics.setCursor(Gdx.graphics.newCursor(no_pm, 0, 0));
                }
            }
        }
    }

}
