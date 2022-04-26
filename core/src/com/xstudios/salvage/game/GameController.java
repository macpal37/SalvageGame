package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
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

import java.util.ArrayList;
import java.util.Iterator;

public class GameController implements Screen, ContactListener {

    public int Level = 0;


    // Assets
    JsonValue constants;
    /**
     * Ocean Background Texture
     */
    protected TextureRegion background;
<<<<<<< HEAD
=======
    /**
     * The texture for flare
     */
    protected TextureRegion flareTexture;
    /**
     * The texture for ping
     */
    protected TextureRegion pingTexture;
    /**
     * The texture for dead body
     */
    protected TextureRegion deadBodyTexture;
    /**
     * The texture for dead body
     */
    protected TextureRegion doorTexture;
    /**
     * Texturs for the door
     */
    protected TextureRegion doorOpenTexture;
    protected TextureRegion doorCloseTexture;
    protected Texture swimmingAnimation;
    protected Texture flareAnimation;
    protected Texture dustAnimation;
    protected Texture plantAnimation;
    protected TextureRegion tileset;
    JsonValue constants;

    // Models to be updated
    protected TextureRegion wallTexture;
    protected TextureRegion hazardTexture;
    protected TextureRegion wallBackTexture;
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c

//    /**
//     * The texture for level.getDiver()
//     */
//    protected TextureRegion level.getDiver()Texture;
//    /**
//     * The texture for item
//     */
//    protected TextureRegion itemTexture;
//
//    /**
//     * The texture for ping
//     */
//    protected TextureRegion pingTexture;
//    /**
//     * The texture for dead body
//     */
//    protected TextureRegion deadBodyTexture;
//    /**
//     * The texture for dead body
//     */
//    protected TextureRegion doorTexture;
//    /**
//     * Texturs for the door
//     */
//    protected TextureRegion doorOpenTexture;
//    protected TextureRegion doorCloseTexture;
//    protected Texture swimmingAnimation;
//    protected Texture dustAnimation;
//    protected Texture plantAnimation;
//    protected TextureRegion tileset;
//
//    protected TextureRegion wallTexture;
//    protected TextureRegion hazardTexture;
//    protected TextureRegion wallBackTexture;

    protected Texture monsterTenctacle;
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
<<<<<<< HEAD
    protected static final float DEFAULT_GRAVITY = -0.01f;//-4.9f;
=======
    protected static final float DEFAULT_GRAVITY = -1f;

>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c

    /**
     * Reference to the game canvas
     */
    protected GameCanvas canvas;

    /**
     * Listener that will update the player mode when we are done
     */
    private ScreenListener listener;

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

    private String[] levels = {"tutorial1", "tutorial2", "tutorial3"};

//    private String[] levels = {"test_level", "level1", "level3"};

    private int curr_level;

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


    private LevelBuilder levelBuilder;
    private LevelModel level;

    private PointLight light;
    private RayHandler rayHandler;

    private PointLight wallShine;


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

<<<<<<< HEAD

    protected Monster monster;
=======
    private PointLight light;
    private PointLight wallShine;

    private RayHandler rayHandler;
//    private RayHandler rayHandlerFlare;

>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c

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
//        audioController.dispose();
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
        levelBuilder.setDirectory(directory);
<<<<<<< HEAD

        levelBuilder.gatherAssets(directory);
//
=======
        tileset = new TextureRegion(directory.getEntry("levels:tilesets:old_ship_tileset", Texture.class));
        diverTexture = new TextureRegion(directory.getEntry("models:diver", Texture.class));
        swimmingAnimation = directory.getEntry("models:diver_swimming", Texture.class);
        flareAnimation = directory.getEntry("models:flare_animation", Texture.class);
        dustAnimation = directory.getEntry("models:dust", Texture.class);

        plantAnimation = directory.getEntry("models:plant", Texture.class);
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
        background = new TextureRegion(directory.getEntry("background:ocean", Texture.class));
//        itemTexture = new TextureRegion(directory.getEntry("models:key", Texture.class));
        constants = directory.getEntry("models:constants", JsonValue.class);
<<<<<<< HEAD
//
//
=======
        pingTexture = new TextureRegion(directory.getEntry("models:ping", Texture.class));
        flareTexture = new TextureRegion(directory.getEntry("models:flare", Texture.class));
        wallTexture = new TextureRegion(directory.getEntry("wall", Texture.class));
        hazardTexture = new TextureRegion(directory.getEntry("hazard", Texture.class));
        doorTexture = new TextureRegion(directory.getEntry("door", Texture.class));
        //wallBackTexture = new TextureRegion(directory.getEntry( "background:wooden_bg", Texture.class ));
        doorOpenTexture = new TextureRegion(directory.getEntry("models:door_open", Texture.class));
        doorCloseTexture = new TextureRegion(directory.getEntry("models:door_closed", Texture.class));
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
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
<<<<<<< HEAD
=======
        int wallCounter = 0;
        int keyCounter = 0;
        int doorCounter = 0;
        int goalDoorCounter = 0;
        int hazardCounter = 0;
        for (GObject go : objects) {
            if (go instanceof HazardModel) {
                HazardModel hazard = (HazardModel) go;
                hazard.setOxygenDrain(-0.1f);
                hazard.setStunDuration(60);
                hazard.setBodyType(BodyDef.BodyType.StaticBody);
                hazard.setDensity(0);
                hazard.setFriction(0.4f);
                hazard.setRestitution(0.1f);
                hazard.setTexture(hazardTexture);
                hazard.setDrawScale(scale);
                hazard.setName("hazard" + hazardCounter++);

                addObject(hazard);
//                hazard.setUserData(hazard);
                hazard.setActive(true);
            } else if (go instanceof Door) {
                Door door = (Door) go;
                door.setBodyType(BodyDef.BodyType.StaticBody);
                door.setTexture(doorTexture);
                door.addTextures(doorCloseTexture, doorOpenTexture);
                door.setDrawScale(scale);
                door.setName("door" + doorCounter++);
                door.setActive(true);
                doors.add(door);
                addObject(door);
            } else if (go instanceof Wall) {

                Wall obj = (Wall) go;
                obj.setBodyType(BodyDef.BodyType.StaticBody);
                obj.setDensity(0);
                obj.setFriction(0.4f);
                obj.setRestitution(0.1f);
                obj.setDrawScale(scale);
                obj.setTexture(wallTexture);
                obj.setDrawScale(scale);
                obj.setName("wall " + wallCounter++);
                addObject(obj);
                System.out.println("filter data " + obj.getFilterData().categoryBits + " " + obj.getFilterData().maskBits+ " " + obj.getFilterData().groupIndex);
            } else if (go instanceof DiverModel) {
                diver = (DiverModel) go;
                diver.setStunned(false);

                diver.setTexture(diverTexture);
                diver.setFilmStrip(new FilmStrip(swimmingAnimation, 2, 12, 24));
                diver.setPingTexture(pingTexture);
                diver.setFlareFilmStrip(new FilmStrip(flareAnimation, 1, 4, 4));
                diver.setFlareTexture(flareTexture);
                diver.setDrawScale(scale);
                diver.setName("diver");
                diver.initFlares(rayHandler);
                addObject(diver);

                System.out.println("diver filter data " + diver.getFilterData().categoryBits + " " + diver.getFilterData().maskBits+ " " + diver.getFilterData().groupIndex);

            } else if (go instanceof DeadBodyModel) {
                dead_body = (DeadBodyModel) go;
                dead_body.setTexture(deadBodyTexture);
                dead_body.setDrawScale(scale);
                dead_body.setDrawSymbolScale(symbol_scale);
                dead_body.setName("dead_body");
                dead_body.setGravityScale(0f);
                dead_body.setSensor(true);
                addObject(dead_body);
            } else if (go instanceof ItemModel) {
                key = (ItemModel) go;
                key.setTexture(itemTexture);
                key.setBodyType(BodyDef.BodyType.StaticBody);
                key.setDrawScale(scale);
                key.setDrawSymbolScale(symbol_scale);
                key.setName("key" + keyCounter++);
                key.setGravityScale(0f);
                key.setSensor(true);
                key.initLight(rayHandler);
                addObject(key);

            } else if (go instanceof GoalDoor) {
                JsonValue goal = constants.get("goal");

                GoalDoor goal_door = (GoalDoor) go;
                goal_door.setBodyType(BodyDef.BodyType.StaticBody);
                goal_door.setDensity(goal.getFloat("density", 0));
                goal_door.setFriction(goal.getFloat("friction", 0));
                goal_door.setRestitution(goal.getFloat("restitution", 0));
                goal_door.setSensor(true);
                goal_door.setDrawScale(scale);
                goal_door.setTexture(doorOpenTexture);
                goal_door.setName("goal" + goalDoorCounter++);
                goalArea.add(goal_door);
                addObject(goal_door);
                System.out.println("goal door filter data " + goal_door.getFilterData().categoryBits + " " + goal_door.getFilterData().maskBits+ " " + goal_door.getFilterData().groupIndex);


            } else if (go instanceof Dust) {
                Dust dust = (Dust) go;
                dust.setFilmStrip(new FilmStrip(dustAnimation, 1, 8, 8));
                dust.setName("dust");
                dust.setBodyType(BodyDef.BodyType.StaticBody);
                dust.setSensor(true);
                dust.setDrawScale(scale);
                addObject(dust);
            } else if (go instanceof Plant) {
                Plant dust = (Plant) go;
                dust.setFilmStrip(new FilmStrip(plantAnimation, 1, 6, 6));
                dust.setName("plant");
                dust.setBodyType(BodyDef.BodyType.StaticBody);
                dust.setSensor(true);
                dust.setDrawScale(scale);
                addObject(dust);
            }

>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c

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
        addObject(monster);

    }

    private void updateGameState() {
        if (level.getDiver().getOxygenLevel() <= 0) {
            game_state = state.LOSE_GAME;
        }
        if (reach_target) {
            game_state = state.WIN_GAME;
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

        // set latching and boosting attributesf
        // latch onto obstacle when key pressed and close to an obstacle
        // stop latching and boost when key is let go
        // TODO: or when it is pressed again? Have had some issues with key presses being missed
        // otherwise, stop latching

        if (input.didKickOff() && !level.getDiver().isLatching() && level.getDiver().isTouchingObstacle()) {
            System.out.println("Player Coords: " + level.getDiver().getPosition());
            System.out.println("Wall Coords: " + level.getDiver().getTouchedWall().getPosition());
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

<<<<<<< HEAD

        }

        if (input.didKickOff() && level.getDiver().isTouchingObstacle()) {
            level.getDiver().setLatching(true);
        } else if (!input.didKickOff() && level.getDiver().isLatching()) {
            level.getDiver().setLatching(false);
            level.getDiver().setBoosting(true);
            level.getDiver().boost(); // boost according to the current user input
        }


=======
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
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
            diver.dropFlare(input.dropFlare());
        }
        diver.updateFlare();
        // manage items/dead body
<<<<<<< HEAD
        level.getDiver().setPickUpOrDrop(input.getOrDropObject());
        level.getDiver().setItem();
=======
        diver.setPickUpOrDrop(input.getOrDropObject());
        diver.setItem();
//        diver.printPotentialItems();
        dead_body.setCarried(diver.hasBody());
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c


        if (!level.getDiver().getStunned()) {
            // decrease oxygen from movement
            if (Math.abs(input.getHorizontal()) > 0 || Math.abs(input.getVertical()) > 0) {
                level.getDiver().changeOxygenLevel(activeOxygenRate);
                // TODO: faster oxygen drain while carrying the body
            } else {
                level.getDiver().changeOxygenLevel(passiveOxygenRate);
            }
        }

        // update audio according to oxygen level
        System.out.println("DIVER: " + level.getDiver().getOxygenLevel());
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

    public boolean pause;

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

        monsterController.update(level.getDiver());


        //** ADDING TENTACLES TO WalL!
//        if (level.getDiver().getTouchedWall() != null && level.getDiver().getTouchedWall().canSpawnTentacle()) {
//            Wall w = level.getDiver().getTouchedWall();
//            Tentacle t = levelBuilder.createTentcle(w, new FilmStrip(monsterTenctacle, 1, 30, 30), scale);
//            addQueuedObject(t);
//        }

        switch (game_state) {
            case PLAYING:
                updatePlayingState();
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
        InputController input = InputController.getInstance();


        updateGameState();

        //deal with hazard stun
<<<<<<< HEAD

//        System.out.println("STUN: " + level.getDiver().getStunCooldown());
        if (level.getDiver().getStunCooldown() > 0) {
            System.out.println("PAIN: " + hostileOxygenDrain);
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
            level.getDiver().setStunCooldown(level.getDiver().getStunCooldown() - 1);
=======
        if (diver.getStunCooldown() > 0) {
            diver.setStunCooldown(diver.getStunCooldown() - 1);
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
        } else {

            level.getDiver().setStunned(false);
            hostileOxygenDrain = 0.0f;
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
        }
<<<<<<< HEAD


=======
        diver.changeOxygenLevel(hostileOxygenDrain);
//        diver.printPotentialItems();
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
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
        if (!debug){
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

                    canvas.draw(keyHud, diver.getItem().getColor(), (float) keyHud.getRegionWidth(), (float) keyHud.getRegionHeight() / 2,
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
                for(int i = 0; i < diver.getRemainingFlares(); i++) {
                    canvas.draw(flareHud, Color.WHITE, (float) flareHud.getRegionWidth(), (float) flareHud.getRegionHeight() / 2,
                            tempProjectedOxygen.x - 10*i,
                            tempProjectedOxygen.y,
                            0.0f, 0.35f, 0.35f);
                }

                break;
        }

<<<<<<< HEAD
        for (GameObject o : level.getAllObjects()) {
            if (o instanceof DiverObjectModel && ((DiverObjectModel) o).isCarried()) {
                DiverObjectModel d_obj = (DiverObjectModel) o;

                canvas.draw(d_obj.getTexture(), d_obj.getColor(), d_obj.origin.x, d_obj.origin.y,
                        cameraController.getCameraPosition2D().x - canvas.getWidth() / 2f + d_obj.getDrawSymbolPos().x,
                        cameraController.getCameraPosition2D().y - canvas.getHeight() / 2f + d_obj.getDrawSymbolPos().y,
                        d_obj.getAngle(), d_obj.getDrawSymbolScale().x, d_obj.getDrawSymbolScale().y);
            }
        }
=======
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for (GameObject obj : level.getAllObjects()) {
//                if (!(obj instanceof Wall)) {
                obj.drawDebug(canvas);
//                }
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
            if (game_state == state.WIN_GAME) {
                listener.exitScreen(this, 0);
            } else if (game_state == state.LOSE_GAME) {
                listener.exitScreen(this, 1);
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
        // TODO Auto-generated method stub
    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
     * This is usually when it regains focus.
     */
    public void resume() {
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

<<<<<<< HEAD
        collisionController.startDiverToObstacle(fix1, fix2, level.getDiver());
        if (listener != null) {
            reach_target = collisionController.getWinState(body1, body2, level.getDiver());
=======
        collisionController.startDiverToObstacle(body1, body2);

        try {
            GObject bd1 = (GObject) body1.getUserData();
            GObject bd2 = (GObject) body2.getUserData();

            if ((diver.getSensorNameLeft().equals(fd2) && diver != bd1 && !(bd1 instanceof ItemModel)) ||
                    (diver.getSensorNameLeft().equals(fd1) && diver != bd2 && !(bd2 instanceof ItemModel))) {

                if (diver != bd1)
                    diver.addTouching(diver.getSensorNameLeft(), bd1);
                else
                    diver.addTouching(diver.getSensorNameLeft(), bd2);

            }
            if ((diver.getSensorNameRight().equals(fd2) && diver != bd1 && !(bd1 instanceof ItemModel)) ||
                    (diver.getSensorNameRight().equals(fd1) && diver != bd2 && !(bd2 instanceof ItemModel))) {

                if (diver != bd1)
                    diver.addTouching(diver.getSensorNameRight(), bd1);
                else
                    diver.addTouching(diver.getSensorNameRight(), bd2);

            }

            if (bd1 instanceof DiverModel && !diver.getSensorNameRight().equals(fd1) && !diver.getSensorNameLeft().equals(fd1) && bd2 instanceof Wall) {
                audioController.wall_collision(diver.getForce());
            }
            if (body1.getUserData() instanceof HazardModel) {

            }
            if (body2.getUserData() instanceof HazardModel) {

            }

//            boolean sensorTouching1 =  diver.getSensorNameLeft().equals(fd2) ||

            if (body1.getUserData() instanceof DiverModel) {
                if (body2.getUserData() instanceof GoalDoor) {
                    if (CollisionController.winGame(diver, (GoalDoor) body2.getUserData())
                            && listener != null) {
                        reach_target = true;//listener.exitScreen(this, 0);
                    }
                }
//                else if (body2.getUserData() instanceof ItemModel) {
//
//                    CollisionController.pickUp(diver, (ItemModel) body2.getUserData());
//                    ((ItemModel) body2.getUserData()).setTouched(true);
//                }
                else if (body2.getUserData() instanceof Door) {


//                toUnlock=CollisionController.attemptUnlock(diver, (Door)body2.getUserData());
                    ((Door) body2.getUserData()).setUnlock(CollisionController.attemptUnlock(diver, (Door) body2.getUserData()));
                } else if (body2.getUserData() instanceof DeadBodyModel) {

                    ((DiverModel) body1.getUserData()).setBodyContact(true);
                } else if (!diver.getSensorNameRight().equals(fd1) && !diver.getSensorNameLeft().equals(fd1) &&
                        body2.getUserData() instanceof HazardModel) {

                    hostileOxygenDrain = CollisionController.staticHazardCollision(diver, (HazardModel) body2.getUserData());
                }


            } else if (body2.getUserData() instanceof DiverModel) {
                if (body1.getUserData() instanceof GoalDoor) {
                    if (CollisionController.winGame(diver, (GoalDoor) body1.getUserData())
                            && listener != null) {
                        reach_target = true;//listener.exitScreen(this, 0);
                    }
                } else if (body1.getUserData() instanceof Door) {
//                toUnlock=CollisionController.attemptUnlock(diver, (Door)body1.getUserData());
                    ((Door) body1.getUserData()).setUnlock(CollisionController.attemptUnlock(diver, (Door) body1.getUserData()));

                }
//                else if (body1.getUserData() instanceof ItemModel) {
//
//                    CollisionController.pickUp(diver, (ItemModel) body1.getUserData());
//                    ((ItemModel) body1.getUserData()).setTouched(true);
//                }
                else if (body1.getUserData() instanceof DeadBodyModel) {
                    ((DiverModel) body2.getUserData()).setBodyContact(true);
                } else if (!diver.getSensorNameRight().equals(fd2) && !diver.getSensorNameLeft().equals(fd2) &&

                        body1.getUserData() instanceof HazardModel) {

                    hostileOxygenDrain = CollisionController.staticHazardCollision(diver, (HazardModel) body1.getUserData());
                }
            }
            if (body1.getUserData() instanceof DiverModel) {
                if (body2.getUserData() instanceof GoalDoor) {
                    if (CollisionController.winGame(diver, (GoalDoor) body2.getUserData())
                            && listener != null) {
                        // reach_target = true;//listener.exitScreen(this, 0);
                        game_state = state.WIN_GAME;
                    }
                }
            } else if (body2.getUserData() instanceof DiverModel) {
                if (body1.getUserData() instanceof GoalDoor) {
                    if (CollisionController.winGame(diver, (GoalDoor) body1.getUserData())
                            && listener != null) {
                        //reach_target = true;//listener.exitScreen(this, 0);
                        game_state = state.WIN_GAME;
                    }
                }
            }
            if (diver.getSensorNameHitBox().equals(fd1)) {
                if (body2.getUserData() instanceof ItemModel) {
                    CollisionController.pickUp(diver, (ItemModel) body2.getUserData());
                    ((ItemModel) body2.getUserData()).setTouched(true);
                }
            } else if (diver.getSensorNameHitBox().equals(fd2)) {
                if (body1.getUserData() instanceof ItemModel) {
                    CollisionController.pickUp(diver, (ItemModel) body1.getUserData());
                    ((ItemModel) body1.getUserData()).setTouched(true);
                }
            }
            // ================= CONTACT LISTENER METHODS =============================

        } catch (Exception e) {
            e.printStackTrace();
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
        }

//        collisionController.addDiverSensorTouching(level.getDiver(), fix1, fix2);
        collisionController.startDiverItemCollision(body1, body2);
        collisionController.startDiverDoorCollision(body1, body2);
        collisionController.startDiverDeadBodyCollision(body1, body2);
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
<<<<<<< HEAD

=======
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

<<<<<<< HEAD
        collisionController.endDiverToObstacle(fix1, fix2, level.getDiver());
        collisionController.endDiverDeadBodyCollision(body1, body2);
        collisionController.removeDiverSensorTouching(level.getDiver(), fix1, fix2);
        collisionController.endDiverItemCollision(body1, body2);

=======
        collisionController.endContact(body1, body2);

        try {
            GObject bd1 = (GObject) body1.getUserData();
            GObject bd2 = (GObject) body2.getUserData();

            if ((diver.getSensorNameLeft().equals(fd2) && diver != bd1 && !(bd1 instanceof ItemModel)) ||
                    (diver.getSensorNameLeft().equals(fd1) && diver != bd2 && !(bd2 instanceof ItemModel))) {

                if (diver != bd1)
                    diver.removeTouching(diver.getSensorNameLeft(), bd1);
                else
                    diver.removeTouching(diver.getSensorNameLeft(), bd2);

            }
            if ((diver.getSensorNameRight().equals(fd2) && diver != bd1 && !(bd1 instanceof ItemModel)) ||
                    (diver.getSensorNameRight().equals(fd1) && diver != bd2 && !(bd2 instanceof ItemModel))) {

                if (diver != bd1)
                    diver.removeTouching(diver.getSensorNameRight(), bd1);
                else
                    diver.removeTouching(diver.getSensorNameRight(), bd2);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (diver.getSensorNameHitBox().equals(fd1)) {
            if (body2.getUserData() instanceof ItemModel) {
                CollisionController.putDown(diver,
                        (ItemModel) body2.getUserData());
                ((ItemModel) body2.getUserData()).setTouched(false);
            }

        } else if (diver.getSensorNameHitBox().equals(fd2)) {
            if (body1.getUserData() instanceof ItemModel) {
                CollisionController.putDown(diver,
                        (ItemModel) body1.getUserData());
                ((ItemModel) body1.getUserData()).setTouched(false);
            }
        }
>>>>>>> 1e6c6b6dfeae2a7403f79cee3c8a396b4343570c
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
