package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.audio.AudioController;
import com.xstudios.salvage.game.levels.LevelBuilder;
import com.xstudios.salvage.game.levels.LevelModel;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Wall;
import com.xstudios.salvage.game.models.*;
import com.xstudios.salvage.util.FilmStrip;
import com.xstudios.salvage.util.PooledList;
import com.xstudios.salvage.util.ScreenListener;


import java.util.Iterator;
import java.util.Queue;

public class GameController implements Screen, ContactListener {

    public int Level = 0;


    // Assets
    JsonValue constants;
    /**
     * Ocean Background Texture
     */
    protected TextureRegion background;


    protected Texture monsterTenctacle;
    protected Texture flareAnimation;
    protected TextureRegion hud;
    protected TextureRegion oxygen;
    protected TextureRegion depletedOxygen;
    protected TextureRegion oxygenText;
    protected TextureRegion bodyHud;
    protected TextureRegion keyHud;
    protected TextureRegion flareHud;
    protected TextureRegion keys;
    private Vector3 tempProjectedHud;
    private Vector3 tempProjectedOxygen;


    //pause
    protected TextureRegion pause_screen;
    protected TextureRegion black_spot;
    protected TextureRegion resume;
    protected TextureRegion restart;
    protected TextureRegion main_menu;

    /**
     * The font for giving messages to the player
     */

    public static BitmapFont displayFont;

    /**
     * how much the object that's stunning the level.getDiver() is draining their oxygen by
     */
    float hostileOxygenDrain = 0.0f;


    // Models to be updated
//    protected DiverModel level.getDiver();
//
//    protected ItemModel key;
//    //    protected ItemModel dead_body;
//    protected DeadBodyModel dead_body;

//    protected ArrayList<GoalDoor> goalArea = new ArrayList<>();
//
//    private Array<Door> doors = new Array<Door>();
//
//    /**
//     * All the objects in the world.
//     */
//    protected PooledList<GameObject> objects = new PooledList<GameObject>();
//
//    protected PooledList<GameObject> aboveObjects = new PooledList<GameObject>();
    /**
     * Queue for adding objects
     */
    protected PooledList<GameObject> addQueue = new PooledList<GameObject>();

    /**
     * Camera centered on the player
     */
    protected CameraController cameraController;

    /**
     * manages collisions
     */
    protected CollisionController collisionController;

    /**
     * manages the Monster AI
     */
    protected MonsterController monsterController;

    /**
     * The rate at which oxygen should decrease passively
     */
    protected float passiveOxygenRate;
    protected float activeOxygenRate;

    /**
     * How many frames after winning/losing do we continue?
     */
    public static final int EXIT_COUNT = 120;
    /**
     * The amount of time for a physics engine step.
     */
    public static final float WORLD_STEP = 1 / 60.0f;
    /**
     * Number of velocity iterations for the constrain solvers
     */
    public static final int WORLD_VELOC = 6;
    /**
     * Number of position iterations for the constrain solvers
     */
    public static final int WORLD_POSIT = 2;

    /**
     * Width of the game world in Box2d units
     */
    protected static final float DEFAULT_WIDTH = 32.0f;
    /**
     * Height of the game world in Box2d units
     */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /**
     * Aspect ratio of the world
     */
    protected static final float ASPECT_RATIO = DEFAULT_WIDTH / DEFAULT_HEIGHT;
    /**
     * The default value of gravity (going down)
     */

//    protected static final float DEFAULT_GRAVITY = -0.01f;//-4.9f;

    protected static final float DEFAULT_GRAVITY = -1f;


    /**
     * Reference to the game canvas
     */
    protected GameCanvas canvas;

    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;
    private Stage stage;

    /**
     * The Box2D world
     */
    protected World world;
    /**
     * The boundary of the world
     */
    protected Rectangle bounds;
    /**
     * The world scale
     */
    protected Vector2 scale;
    /**
     * The symbol scale
     */
    protected Vector2 symbol_scale;

    /**
     * Whether or not this is an active controller
     */
    private boolean active;
    /**
     * Whether or not debug mode is active
     */
    private boolean debug;

    private Vector2 forceCache;
    //    private AudioController audioController;
    private PhysicsController physicsController;

    private boolean reach_target = false;
    /**
     * ================================LEVELS=================================
     */

    /**
     * Summary of levels
     * asher_1 - swim to end goal
     * asher_2 - teach kickpoints
     * t_easy_1 - teach hitting hazards, keys, and explore to find body
     * t_easy_2 - introduce monster disturbance
     * beta_1 - teach hazards and flares
     * beta_2 - teach doors, keys, flares, longer level requires tracing back steps
     * beta_3 - monster disturbance
     * beta_5 - kick points and monster disturbance
     * <p>
     * level1 - retracing steps and avoiding hazards
     * level4 - insanely hard key level
     */
    // Beta Release Setup
    private String[] levels = {"test_level", "beta_1", "beta_2", "beta_3", "beta_5"};


    private int curr_level;

    private TextureRegion test;


    private enum state {
        PLAYING,
        WIN_GAME,
        LOSE_GAME,
        RESTART,
        PAUSE,
        QUIT
    }

    // TODO: when we add other screens we can actually implement code to support pausing and quitting
    private state game_state;
    private boolean pause;
    boolean resume_game;
    boolean restart_game;
    boolean exit_home;
    boolean clicked;

    private LevelBuilder levelBuilder;
    private LevelModel level;

    private PointLight light;
    private RayHandler rayHandler;

    private PointLight wallShine;

    private short no_hazard_collision_mask = 0x0004;
    private short no_hazard_collision_category = 0x0002;
    private short no_hazard_collision_group = 1;

    private short hazard_collision_mask = 0x000c;
    private short hazard_collision_category = 0x0002;
    private short hazard_collision_group = 1;


    // ======================= CONSTRUCTORS =================================

    /**
     * Creates a new game world with the default values.
     * <p>
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected GameController() {
        this(new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT),
                new Vector2(0, DEFAULT_GRAVITY));
        curr_level = 0;
        pause = false;
        resume_game = false;
        restart_game = false;
        exit_home = false;
    }

    /**
     * Creates a new game world
     * <p>
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width   The width in Box2d coordinates
     * @param height  The height in Box2d coordinates
     * @param gravity The downward gravity
     */
    protected GameController(float width, float height, float gravity) {
        this(new Rectangle(0, 0, width, height), new Vector2(0, gravity));
    }

    protected Monster monster;

//    private RayHandler rayHandlerFlare;


    /**
     * Creates a new game world
     * <p>
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds  The game bounds in Box2d coordinates
     * @param gravity The gravitational force on this Box2d world
     */
    protected GameController(Rectangle bounds, Vector2 gravity) {
        this.tempProjectedHud = new Vector3(0, 0, 0);
        this.tempProjectedOxygen = new Vector3(0, 0, 0);
        world = new World(gravity.scl(1), false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1, 1);
        this.symbol_scale = new Vector2(.4f, .4f);
        debug = false;
        active = false;
        // TODO: oxygen rate should be a parameter loaded from a json
        passiveOxygenRate = -.01f;
        activeOxygenRate = -.02f;

        forceCache = new Vector2(0, 0);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(.015f);
//        rayHandlerFlare = new RayHandler(world);
//        rayHandlerFlare.setAmbientLight(1f);
//        RayHandler.useDiffuseLight(true);

        light = new PointLight(rayHandler, 100, Color.BLACK, 15, 0, 0);
        wallShine = new PointLight(rayHandler, 100, Color.BLUE, 8, 0, 0);
        wallShine.setSoft(true);

        int r = 225, g = 103, b = 30;

        wallShine.setColor(78f / 255f, 180f / 255f, 82f / 255f, 0.85f);
        Filter f2 = new Filter();
        f2.categoryBits = 0x0004;
        f2.maskBits = 0x0002;
        f2.groupIndex = -1;

        Filter f = new Filter();
        f.categoryBits = 0x0002;
        f.maskBits = 0x0004;
        f.groupIndex = 1;

        wallShine.setContactFilter(f2);
        light.setContactFilter(f);
//        audioController = new AudioController(100.0f);
        AudioController.getInstance().initialize();
        collisionController = new CollisionController();
        physicsController = new PhysicsController(10, 5);
        world.setContactListener(this);

        levelBuilder = new LevelBuilder();
        level = new LevelModel();


        game_state = state.PLAYING;

    }

    public void setCameraPositionNormal() {
        cameraController.setCameraPosition(640, 360);
        cameraController.render();
    }

    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive() {
        return active;
    }

    public void setLevel(int l) {
        curr_level = l;
    }

    /**
     * Returns the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    public void setCameraController(CameraController cameraController) {
        this.cameraController = cameraController;
        cameraController.setBounds(0, 0, 5400 * 2 / 5, 3035 * 2 / 5);
    }

    /**
     * Sets the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth() / bounds.getWidth();
        this.scale.y = canvas.getHeight() / bounds.getHeight();
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for (GameObject obj : level.getAllObjects()) {
            obj.deactivatePhysics(world);
        }

        addQueue.clear();
        world.dispose();
        rayHandler.dispose();
//        rayHandlerFlare.dispose();

        level.dispose();
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        canvas = null;
        pause = false;
        resume_game = false;
        restart_game = false;
        exit_home = false;
        clicked = false;
    }

    Texture plantAnimation;

    /**
     * Gather the assets for this controller.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
        test = new TextureRegion(directory.getEntry("m", Texture.class));
        levelBuilder.setDirectory(directory);

        levelBuilder.gatherAssets(directory);

        flareAnimation = directory.getEntry("models:flare_animation", Texture.class);
        background = new TextureRegion(directory.getEntry("background:ocean", Texture.class));
//        itemTexture = new TextureRegion(directory.getEntry("models:key", Texture.class));
        constants = directory.getEntry("models:constants", JsonValue.class);

        displayFont = directory.getEntry("fonts:lightpixel", BitmapFont.class);

//        deadBodyTexture = new TextureRegion(directory.getEntry("models:dead_body", Texture.class));
        hud = new TextureRegion(directory.getEntry("hud", Texture.class));
        depletedOxygen = new TextureRegion(directory.getEntry("oxygen_depleted", Texture.class));
        oxygenText = new TextureRegion(directory.getEntry("oxygen_text", Texture.class));
        bodyHud = new TextureRegion(directory.getEntry("body_hud", Texture.class));
        keyHud = new TextureRegion(directory.getEntry("key_hud", Texture.class));
        flareHud = new TextureRegion(directory.getEntry("flare_hud", Texture.class));
        oxygen = new TextureRegion(directory.getEntry("oxygen", Texture.class));
        monsterTenctacle = directory.getEntry("models:monster1", Texture.class);
        plantAnimation = directory.getEntry("models:plant", Texture.class);

        //pause
        pause_screen = new TextureRegion(directory.getEntry("pause", Texture.class));
        black_spot = new TextureRegion(directory.getEntry("black_spot", Texture.class));
        resume = new TextureRegion(directory.getEntry("resume", Texture.class));
        restart = new TextureRegion(directory.getEntry("restart", Texture.class));
        main_menu = new TextureRegion(directory.getEntry("main_menu_pause", Texture.class));
    }

    /**
     * Adds a physics object in to the insertion queue.
     * <p>
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     * <p>
     * param obj The object to add
     */
    public void addQueuedObject(GameObject obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    /**
     * Immediately adds the object to the physics world
     * <p>
     * param obj The object to add
     */
    protected void addObject(GameObject obj) {
        assert inBounds(obj) : "Object is not in bounds";
        obj.activatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     * <p>
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     * @return true if the object is in bounds.
     */
    public boolean inBounds(GameObject obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    public void reset() {
        game_state = state.PLAYING;
        pause = false;
        resume_game = false;
        restart_game = false;
        exit_home = false;
        clicked = false;
        Vector2 gravity = new Vector2(world.getGravity());
        for (GameObject obj : level.getAllObjects()) {
            obj.deactivatePhysics(world);
        }

        level.getAllObjects().clear();
        level.getAboveObjects().clear();
        addQueue.clear();
        AudioController.getInstance().reset();
        populateLevel();
    }


    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        cameraController.setZoom(1.0f);
        levelBuilder.createLevel(levels[curr_level], level, scale, symbol_scale, rayHandler);
        pause = false;

        // TODO: will this have the same effect as going through each type, casting, then adding?
        for (GameObject obj : level.getAllObjects()) {
            addObject(obj);
        }
        monster = new Monster(level.getDiver().getX(), level.getDiver().getY());
        monster.setTentacleSprite(new FilmStrip(monsterTenctacle, 1, 30, 30));
        monster.setDrawScale(scale);
        monster.setName("Monster");
        monsterController = new MonsterController(monster);
        level.addObject(monster);
        level.getDiver().initFlares(rayHandler);
        level.getDiver().setFlareFilmStrip(new FilmStrip(flareAnimation, 1, 4, 4));
        addObject(monster);

    }

    private void updateGameState() {
        if (level.getDiver().getOxygenLevel() <= 0) {
            game_state = state.LOSE_GAME;
        } else if (reach_target) {
            game_state = state.WIN_GAME;
        } else if (pause) {
            game_state = state.PAUSE;
        } else {
            game_state = state.PLAYING;
        }
    }


    private void updatePlayingState() {
        // apply movement
        InputController input = InputController.getInstance();
        if (input.isPause()) pause();
        level.getDiver().setHorizontalMovement(input.getHorizontal() * level.getDiver().getForce());
        level.getDiver().setVerticalMovement(input.getVertical() * level.getDiver().getForce());

        // stop boosting when player has slowed down enough
        if (level.getDiver().getLinearVelocity().len() < 15 && level.getDiver().isBoosting()) {
            level.getDiver().setBoosting(false);
        }

        // store the facing direction, which cannot be 0, 0
        if (input.getHorizontal() != 0 || input.getVertical() != 0) {
            level.getDiver().setFacingDir(input.getHorizontal(), input.getVertical());
        }
        level.getDiver().reduceInvincibleTime();
//        System.out.println("invincible time " + level.getDiver().getInvincibleTime());

        // set latching and boosting attributesf
        // latch onto obstacle when key pressed and close to an obstacle
        // stop latching and boost when key is let go
        // TODO: or when it is pressed again? Have had some issues with key presses being missed
        // otherwise, stop latching

        if (input.didKickOff() && !level.getDiver().isLatching() && level.getDiver().isTouchingObstacle()) {
            //System.out.println("Player Coords: " + level.getDiver().getPosition());
            //System.out.println("Wall Coords: " + level.getDiver().getTouchedWall().getPosition());
            int playerX = (int) level.getDiver().getPosition().x - 1;
            int playerY = (int) level.getDiver().getPosition().y - 1;
            int wallX = (int) level.getDiver().getTouchedWall().getPosition().x;
            int wallY = (int) level.getDiver().getTouchedWall().getPosition().y;
            if (Math.abs(level.getDiver().targetAngleY) >= 44) {
//                if (level.getDiver().getVerticalMovement() != 0) {
                if (playerY > wallY) {
                    level.getDiver().setTargetAngle(-1, 90);
                } else if (playerY <= wallY) {
                    level.getDiver().setTargetAngle(-1, -90);
                }
            } else {
                if (playerX > wallX) {
                    level.getDiver().setTargetAngle(0, 0);
                } else if (playerX <= wallX) {
                    level.getDiver().setTargetAngle(180, 0);
                }
            }

        }

        if (input.didKickOff() && level.getDiver().isTouchingObstacle()) {
            level.getDiver().setLatching(true);
        } else if (!input.didKickOff() && level.getDiver().isLatching()) {
            level.getDiver().setLatching(false);
            level.getDiver().setBoosting(true);
            level.getDiver().boost(); // boost according to the current user input
        }

        // set forces from ocean currents
        level.getDiver().setDriftMovement(physicsController.getCurrentVector(level.getDiver().getPosition()).x,
                physicsController.getCurrentVector(level.getDiver().getPosition()).y);
        // apply forces for movement
        if (!level.getDiver().getStunned())
            level.getDiver().applyForce();

        // do the ping
        level.getDiver().setPing(input.didPing());
        level.getDiver().setPingDirection(level.getDeadBody().getPosition());

        // flare management
        if (input.dropFlare()) {
            level.getDiver().dropFlare(input.dropFlare());
        }
        level.getDiver().updateFlare();
        // manage items/dead body

        level.getDiver().setPickUpOrDrop(input.getOrDropObject());
        level.getDiver().setItem();

        level.getDeadBody().setCarried(level.getDiver().hasBody());


        if (!level.getDiver().getStunned()) {
            // decrease oxygen from movement
            if ((Math.abs(input.getHorizontal()) > 0 || Math.abs(input.getVertical()) > 0) &&
                    level.getDiver().getLinearVelocity().len() > 0) {
                level.getDiver().changeOxygenLevel(activeOxygenRate);
                // TODO: faster oxygen drain while carrying the body
            } else {
                level.getDiver().changeOxygenLevel(passiveOxygenRate);
            }
        }


        // update audio according to oxygen level

        AudioController.getInstance().update(level.getDiver().getOxygenLevel(), level.getDiver().getMaxOxygen());


        if (level.getDiver().getBody() != null && !pause) {
            cameraController.setCameraPosition(
                    level.getDiver().getX() * level.getDiver().getDrawScale().x, level.getDiver().getY() * level.getDiver().getDrawScale().y);

            light.setPosition(
                    (level.getDiver().getX() * level.getDiver().getDrawScale().x) / 40f,
                    (level.getDiver().getY() * level.getDiver().getDrawScale().y) / 40f);
            wallShine.setPosition(
                    (level.getDiver().getX() * level.getDiver().getDrawScale().x) / 40f,
                    (level.getDiver().getY() * level.getDiver().getDrawScale().y) / 40f);
        }

        // TODO: why wasnt this in marco's code?

        cameraController.render();
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }


    /**
     * Returns whether to process the update loop
     * <p>
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        InputController input = InputController.getInstance();
        input.readInput(bounds, scale);
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;

        }
        if (input.didMenu()) {
            cameraController.setCameraPosition(640.0f, 360.0f);
            cameraController.setZoom(1f);
            listener.exitScreen(this, 2);
            pause = true;
        }

        // Handle resets
        if (input.didReset() || game_state == state.RESTART) {
            reset();
        }
        return true;


    }


    int tentacleSpawn = 0;

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        for (Door door : level.getDoors()) {
            door.setActive(!door.getUnlock(level.getDiver().getItem()));
        }

        rayHandler.setCombinedMatrix(cameraController.getCamera().combined.cpy().scl(40f));
//        rayHandlerFlare.setCombinedMatrix(cameraController.getCamera().combined.cpy().scl(40f));

        //monsterController.update(level.getDiver());


        monsterController.update(hostileOxygenDrain, level.getDiver());
        Queue<Wall> tentacles = monsterController.getMonster().getTentacles();
//        Wall add_wall = tentacles.poll();
//        if (add_wall != null && add_wall.canSpawnTentacle()) {
//            Tentacle t = levelBuilder.createTentcle(add_wall, new FilmStrip(monsterTenctacle, 1, 30, 30), scale);
//            addQueuedObject(t);
//        }
        while (tentacles.size() > 0) {
            Wall add_wall = tentacles.poll();
            if (add_wall.canSpawnTentacle() && add_wall != null) {

                Tentacle t = levelBuilder.createTentcle(monster.getAggrivation(), 1f, add_wall, new FilmStrip(monsterTenctacle, 1, 30, 30));
                addQueuedObject(t);
                AudioController.getInstance().roar();
            }
        }
//        for (Tentacle t : monsterController.getMonster().getTentacles()) {
//            Wall w = t.getSpawnWall();
//            System.out.println(w);
//            if (w.canSpawnTentacle()) {
//                t = levelBuilder.createTentcle(t, new FilmStrip(monsterTenctacle, 1, 30, 30), scale);
//                addQueuedObject(t);
//            }
//        }

        //** ADDING TENTACLES TO WalL!
//        if (level.getDiver().getTouchedWall() != null && level.getDiver().getTouchedWall().canSpawnTentacle()) {
//            Wall w = level.getDiver().getTouchedWall();
//            Tentacle t = levelBuilder.createTentcle(monster.getAggrivation(), 1f, w, new FilmStrip(monsterTenctacle, 1, 30, 30));
//            addQueuedObject(t);
//        }


        switch (game_state) {
            case PLAYING:
                updatePlayingState();
                break;
            case PAUSE:
                InputController input = InputController.getInstance();
                if (input.isPause()) {
                    resume();
                }
                break;
            // could be useful later but currently just has updates for PLAYING state
//            case WIN_GAME:
//
//            break;
//            case LOSE_GAME:
//
//            break;
        }


        // apply movement


        updateGameState();

        //deal with hazard stun

        if (level.getDiver().isInvincible()) {
            level.getDiver().setHazardInvincibilityFilter();
//            light.setContactFilter(no_hazard_collision_category, no_hazard_collision_group, no_hazard_collision_mask);
        } else {
            level.getDiver().setHazardCollisionFilter();
//            light.setContactFilter(hazard_collision_category, hazard_collision_group, hazard_collision_mask);
        }
//        if(level.getDiver().getChangeLightFilter()){
//            System.out.println("CHANGE LIGHT FILTER");
//            light.setContactFilter(hazard_collision_category, hazard_collision_group, hazard_collision_mask);
//        } else {
//            System.out.println("CHANGE LIGHT FILTER to no hazard collisions");
//            light.setContactFilter(no_hazard_collision_category, no_hazard_collision_group, no_hazard_collision_mask);
//        }
//        System.out.println("STUN: " + level.getDiver().getStunCooldown());
        if (level.getDiver().getStunCooldown() > 0) {
//            System.out.println("PAIN: " + hostileOxygenDrain);
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
            level.getDiver().setStunCooldown(level.getDiver().getStunCooldown() - 1);

        } else {

            level.getDiver().setStunned(false);
            hostileOxygenDrain = 0.0f;
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
//            level.getDiver().setHazardCollisionFilter();
//            System.out.println("SETTING STUNNED TO FALSE");
        }


    }

    /**
     * Processes physics
     * <p>
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            GameObject go = addQueue.poll();
            addObject(go);
            level.addObject(go);
        }

        if (level.getDiver().getDeadBody() != null && level.getDiver().getDeadBody().isCarried()) {
            level.getDeadBody().setActive(false);
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<GameObject>.Entry> iterator = level.getAllObjects().entryIterator();
        while (iterator.hasNext()) {
            PooledList<GameObject>.Entry entry = iterator.next();
            GameObject obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
        iterator = level.getAboveObjects().entryIterator();
        while (iterator.hasNext()) {
            PooledList<GameObject>.Entry entry = iterator.next();
            GameObject obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
    }

    int tick = 0;

    public boolean pointer1(int x, int y, int width, int height, float scale) {
        int pX = Gdx.input.getX();
        int pY = Gdx.input.getY();
        System.out.println("touched: " + Gdx.input.isTouched());
        // Flip to match graphics coordinates
        y = canvas.getHeight() - y;
        float y1 = (float) y - (int) (360 - cameraController.getCameraPosition2D().y);
        float x1 = (float) x - (int) (640 - cameraController.getCameraPosition2D().x);
        float w = scale * width;
        float h = scale * height;

        System.out.println("pointer: " + pX + " " + pY);
        if ((x1 + w > pX && x1 - w < pX) && (y1 + h > pY && y1 - h < pY)) {
            if (Gdx.input.isTouched()) clicked = true;
            return true;
        }
        return false;
    }

//    public boolean pointer2(int x, int y, int width, int height, float scale) {
//        int pX = Gdx.input.getX();
//        int pY = Gdx.input.getY();
//        // Flip to match graphics coordinates
//        y = canvas.getHeight() - y;
//        float y1 = (float)y - (int)(360 - cameraController.getCameraPosition2D().y);
//        float x1 = (float)x - (int)(640 - cameraController.getCameraPosition2D().x);
//        float w = scale * width;
//        float h = scale * height;
//
//        System.out.println("pointer: " + pX + " " + pY);
//        if((x1 + w > pX && x1 - w < pX) && (y1 + h > pY && y1 - h < pY)){
//            if(Gdx.input.isTouched())
//                System.out.println("touch");
//                return true;
//        }
//        return false;
//    }

    /**
     * Draw the physics objects to the canvas
     * <p>
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     * <p>
     * The method draws all objects in the order that they were added.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void draw(float dt) {
        tick++;
        canvas.clear();

        canvas.begin();

        // draw game objects
        canvas.draw(background, com.badlogic.gdx.graphics.Color.WHITE, 0, 0, -500, -250, 0, 4, 4);

        for (GameObject obj : level.getAllObjects()) {
            if (obj instanceof Tentacle) {
//                System.out.println("YeAH TENTACLE!!!");
            }

            if (!(obj instanceof DiverModel))
                if (!(obj instanceof DecorModel))
                    obj.draw(canvas);
        }

        for (GameObject obj : level.getAboveObjects()) {

            obj.draw(canvas);
        }
        level.getDiver().draw(canvas);
        canvas.end();
        if (!debug) {
            rayHandler.updateAndRender();
//            rayHandlerFlare.updateAndRender();
        }
        canvas.begin();

        switch (game_state) {
            case PLAYING:

                tempProjectedHud.x = (float) canvas.getWidth() / 2;
                tempProjectedHud.y = 0f;
                tempProjectedHud = cameraController.getCamera().unproject(tempProjectedHud);

                //draw hud background

                canvas.draw(hud, Color.WHITE, hud.getRegionWidth() / 2, hud.getRegionHeight(),
                        tempProjectedHud.x, tempProjectedHud.y,
                        0.0f, 1, 0.5f);

                //draw remaining oxygen
                tempProjectedOxygen.x = (float) canvas.getWidth() / 5.5f;
                tempProjectedOxygen.y = (45 * canvas.getHeight()) / 1080;
                tempProjectedOxygen = cameraController.getCamera().unproject(tempProjectedOxygen);

                if (level.getDiver().getStunCooldown() % 20 > 10) {

                    canvas.draw(oxygen, Color.BLUE, 0, (float) oxygen.getRegionHeight() / 2,
                            (float) tempProjectedOxygen.x,
                            tempProjectedOxygen.y,
                            0.0f,
                            1f * (level.getDiver().getOxygenLevel() / (float) level.getDiver().getMaxOxygen()),
                            0.5f
                    );

                } else if (level.getDiver().getOxygenLevel() < level.getDiver().getMaxOxygen() / 4 && tick % 25 > (level.getDiver().getOxygenLevel() / 2)) {
                    canvas.draw(oxygen, Color.BLUE, 0, (float) oxygen.getRegionHeight() / 2,
                            (float) tempProjectedOxygen.x,
                            tempProjectedOxygen.y,
                            0.0f,
                            1f * (level.getDiver().getOxygenLevel() / (float) level.getDiver().getMaxOxygen()),
                            0.5f
                    );
                } else {
                    canvas.draw(oxygen, Color.WHITE, 0, (float) oxygen.getRegionHeight() / 2,
                            (float) tempProjectedOxygen.x,
                            tempProjectedOxygen.y,
                            0.0f,
                            1f * (level.getDiver().getOxygenLevel() / (float) level.getDiver().getMaxOxygen()),
                            0.5f
                    );
                }


                canvas.draw(oxygenText, Color.WHITE, (float) oxygenText.getRegionWidth() / 2, (float) oxygenText.getRegionHeight() / 2,
                        tempProjectedHud.x, tempProjectedOxygen.y,
                        0.0f, 0.5f, 0.5f);

                //draw inventory indicator
                if (level.getDiver().carryingItem()) {

                    canvas.draw(keyHud, level.getDiver().getItem().getColor(), (float) keyHud.getRegionWidth(), (float) keyHud.getRegionHeight() / 2,
                            tempProjectedOxygen.x - 50,
                            tempProjectedOxygen.y,
                            0.0f, 0.35f, 0.35f);

                }

                //draw body indicator
                if (level.getDiver().hasBody()) {
                    canvas.draw(bodyHud, Color.WHITE, 0, (float) bodyHud.getRegionHeight() / 2,
                            tempProjectedHud.x + 50 + (cameraController.getCameraPosition2D().x - tempProjectedOxygen.x),
                            tempProjectedOxygen.y,
                            0.0f, 0.35f, 0.35f);
                }
                for (int i = 0; i < level.getDiver().getRemainingFlares(); i++) {
                    canvas.draw(flareHud, Color.WHITE, (float) flareHud.getRegionWidth(), (float) flareHud.getRegionHeight() / 2,
                            tempProjectedOxygen.x - 10 * i,
                            tempProjectedOxygen.y,
                            0.0f, 0.35f, 0.35f);
                }

                break;

            case PAUSE:
                setCameraPositionNormal();
                System.out.println("pause state");
                canvas.draw(pause_screen, Color.WHITE,
                        cameraController.getCameraPosition2D().x - canvas.getWidth() / 2f,
                        cameraController.getCameraPosition2D().y - canvas.getHeight() / 2f, canvas.getWidth(), canvas.getHeight());
                canvas.draw(black_spot, Color.WHITE,
                        cameraController.getCameraPosition2D().x - canvas.getWidth() / 2f,
                        cameraController.getCameraPosition2D().y - canvas.getHeight() / 2f - canvas.getHeight() / 4f,
                        black_spot.getRegionWidth(), black_spot.getRegionHeight());

                Color tint = (pointer1((int) cameraController.getCameraPosition2D().x,
                        (int) cameraController.getCameraPosition2D().y + main_menu.getRegionHeight() + main_menu.getRegionHeight() / 2,
                        resume.getRegionWidth() / 2, resume.getRegionHeight() / 2, 0.7f) ? Color.GRAY : Color.WHITE);
//                if(clicked) resume();
//                canvas.draw(resume, tint,
//                        resume.getRegionWidth() / 2,
//                        resume.getRegionHeight(),
//                        cameraController.getCameraPosition2D().x,
//                        cameraController.getCameraPosition2D().y + main_menu.getRegionHeight() + main_menu.getRegionHeight() / 2,
//                        0, 0.7f, 0.7f);

                tint = (pointer1((int) cameraController.getCameraPosition2D().x,
                        (int) cameraController.getCameraPosition2D().y
                                + main_menu.getRegionHeight() / 2,
                        restart.getRegionWidth() / 2,
                        restart.getRegionHeight() / 2, 0.7f) ? Color.GRAY : Color.WHITE);
                if (clicked) reset();
                canvas.draw(restart, tint,
                        restart.getRegionWidth() / 2,
                        resume.getRegionHeight(),
                        cameraController.getCameraPosition2D().x,
                        cameraController.getCameraPosition2D().y
                                + main_menu.getRegionHeight() / 2, 0, 0.7f, 0.7f);

                tint = (pointer1((int) cameraController.getCameraPosition2D().x,
                        (int) cameraController.getCameraPosition2D().y - main_menu.getRegionHeight() / 2
                        , main_menu.getRegionWidth() / 2,
                        main_menu.getRegionHeight() / 2, 0.7f) ? Color.GRAY : Color.WHITE);
                if (clicked) exit_home = true;
                canvas.draw(main_menu, tint,
                        main_menu.getRegionWidth() / 2,
                        main_menu.getRegionHeight(),
                        cameraController.getCameraPosition2D().x,
                        cameraController.getCameraPosition2D().y - main_menu.getRegionHeight() / 2,
                        0, 0.7f, 0.7f);

        }

        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for (GameObject obj : level.getAllObjects()) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

    }


    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // IGNORE FOR NOW
    }

    /**
     * Called when the Screen should render itself.
     * <p>
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
                postUpdate(delta);
            }
            draw(delta);
            //draw(delta);
            if (game_state == state.WIN_GAME) {
                listener.exitScreen(this, 0);
            } else if (game_state == state.LOSE_GAME) {
                listener.exitScreen(this, 1);
            } else if (exit_home == true && listener != null) {
                listener.exitScreen(this, 2);
            }
        }
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */

    public void pause() {
        System.out.println("pause() was called");
        pause = true;

        updateGameState();
    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
     * This is usually when it regains focus.
     */
    public void resume() {
        System.out.println("resume");
        pause = false;
        updateGameState();
        // TODO Auto-generated method stub
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


    // ================= CONTACT LISTENER METHODS =============================

    public void beginContact(Contact contact) {

        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        collisionController.startDiverToObstacle(fix1, fix2, level.getDiver(), monsterController);
        if (listener != null) {
            reach_target = collisionController.getWinState(body1, body2, level.getDiver());

        }

//        collisionController.addDiverSensorTouching(level.getDiver(), fix1, fix2);
        collisionController.startDiverItemCollision(body1, body2);
        collisionController.startDiverDoorCollision(body1, body2);
        collisionController.startMonsterWallCollision(body1, body2);
        collisionController.startDiverDeadBodyCollision(body1, body2);
        collisionController.startFlareTentacleCollision(fix1, fix2);
        collisionController.startFlareFlare(body1, body2);
        collisionController.startDiverFlare(body1, body2);
        float d = collisionController.startDiverHazardCollision(fix1, fix2, level.getDiver());
        if (d != 0)
            hostileOxygenDrain = d;

    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        // Call CollisionController to handle collisions

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        collisionController.endDiverToObstacle(fix1, fix2, level.getDiver());
        collisionController.endDiverDeadBodyCollision(body1, body2);
        collisionController.endMonsterWallCollision(body1, body2);
        collisionController.removeDiverSensorTouching(level.getDiver(), fix1, fix2);
        collisionController.endDiverItemCollision(body1, body2);
        collisionController.endDiverHazardCollision(fix1, fix2, level.getDiver());
        collisionController.endFlareFlare(body1, body2);
        collisionController.endDiverFlare(body1, body2);

    }

    /**
     * Handles any modifications necessary before collision resolution
     * <p>
     * This method is called just before Box2D resolves a collision.
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    public Rectangle getWorldBounds() {
        return bounds;
    }

}
