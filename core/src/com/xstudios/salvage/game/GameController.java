package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

public class GameController extends ScreenController implements ContactListener {

    // Assets
    JsonValue constants;
    /**
     * Ocean Background Texture
     */
    protected TextureRegion background;

    public static Vector2 worldScale = null;

    protected Texture monsterAttackTenctacle;
    protected Texture monsterIdleTenctacle;

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
    private Vector3 pauseScreen;

    //pause
    protected Texture pause_screen;
    protected Texture black_spot;
    protected Texture resume;
    protected Texture restart;
    protected Texture main_menu;

    public static BitmapFont displayFont;

    /**
     * how much the object that's stunning the level.getDiver() is draining their oxygen by
     */
    float hostileOxygenDrain = 0.0f;

    /**
     * Queue for adding objects
     */
    protected PooledList<GameObject> addQueue = new PooledList<GameObject>();

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

    protected static final float DEFAULT_GRAVITY = -1f;

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
    protected Vector2 world_scale;

    /**
     * The symbol scale
     */
    protected Vector2 symbol_scale;

    /**
     * Whether or not debug mode is active
     */
    private boolean debug;

    private Vector2 forceCache;
    //    private AudioController audioController;
    private PhysicsController physicsController;

    private boolean reach_target = false;

    private int game_over_animation_time = 60;
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
    private String[] levels = {"Golden0", "Golden1", "Golden2", "Golden3", "Golden4", "Golden5", "Golden6", "Golden7", "Golden8"};


    private int curr_level;

    private TextureRegion test;


    private enum state {
        PLAYING,
        EXIT_WIN,
        EXIT_LOSE,
        RESTART,
        PAUSE,
        QUIT,
        DYING,
        WIN_ANIMATION
    }

    // TODO: when we add other screens we can actually implement code to support pausing and quitting
    private state game_state;
    private boolean pause;
    boolean resume_game;
    boolean restart_game;
    boolean exit_home;
    boolean clicked;
    boolean press_resume;
    boolean press_restart;

    private Player player;

    private LevelBuilder levelBuilder;
    private LevelModel level;

    private PointLight light;
    private Color stun_color;
    private Color low_oxygen_color;
    private Color monster_color;
    private float stun_light_radius = 5f;
    private float normal_light_radius = 15f;

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
        pause = false;
        exit_home = false;
        press_restart = false;
        press_resume = false;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(1280, 720);
        this.world_scale.x = 1280 / bounds.getWidth();
        this.world_scale.y = 720 / bounds.getHeight();
    }

    public int getTotalLevels() {
        return levels.length;
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

//    protected Monster monster;

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

        this.pauseScreen = new Vector3(0, 0, 0);
        this.tempProjectedHud = new Vector3(0, 0, 0);
        this.tempProjectedOxygen = new Vector3(0, 0, 0);
        world = new World(gravity.scl(1), false);
        this.bounds = new Rectangle(bounds);
        worldScale = new Vector2(1, 1);
        this.world_scale = new Vector2(1, 1);


        this.symbol_scale = new Vector2(.4f, .4f);
        debug = false;
        active = false;
        // TODO: oxygen rate should be a parameter loaded from a json
        passiveOxygenRate = -.01f;
        activeOxygenRate = -.02f;

        forceCache = new Vector2(0, 0);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(.015f);

        stun_color = new Color(1f, 0.15f, 0.15f, .3f);//Color.BLACK;
        low_oxygen_color = new Color(0f, .2f, .7f, .3f);
        monster_color = new Color(1f, 0f, 0f, .4f);

        light = new PointLight(rayHandler, 100, Color.BLACK, normal_light_radius, 0, 0);
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

        AudioController.getInstance().initialize();
        collisionController = new CollisionController();
        physicsController = new PhysicsController(10, 5);
        world.setContactListener(this);

        levelBuilder = new LevelBuilder();
        level = new LevelModel();


        game_state = state.PLAYING;

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

    }
    public void setCameraPositionNormal() {
        camera.setCameraPosition(640, 360);
        camera.render();
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
        this.camera = cameraController;
        cameraController.setBounds(0, 0, 5400 * 2 / 5, 3035 * 2 / 5);
        camera.render();
    }

    public void setDefaultPosition() {
        this.camera.setCameraPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    /**
     * Sets the canvas associated with this controller
     * Sets the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.world_scale.x = 1280 / bounds.getWidth();
        this.world_scale.y = 720 / bounds.getHeight();
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for (GameObject obj : level.getAllObjects()) {
            obj.deactivatePhysics(world);
        }
        for (GameObject obj : level.getAboveObjects()) {
            obj.deactivatePhysics(world);
        }

        addQueue.clear();
        world.dispose();
        rayHandler.dispose();
        level.dispose();
        addQueue = null;
        bounds = null;
        world_scale = null;
        world = null;
        canvas = null;
        pause = false;

        exit_home = false;
        press_restart = false;
        press_resume = false;
        AudioController.getInstance().dispose();
        player = null;
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
        monsterAttackTenctacle = directory.getEntry("models:monster1", Texture.class);
        monsterIdleTenctacle = directory.getEntry("models:monster1", Texture.class);
        //TODO: CHANGE BACK TO ACTUAL WALL TENTACLE
        plantAnimation = directory.getEntry("models:plant", Texture.class);

        //pause

        pause_screen = directory.getEntry("pause", Texture.class);
        black_spot = directory.getEntry("black_spot", Texture.class);
        resume = directory.getEntry("resume", Texture.class);
        restart = directory.getEntry("restart", Texture.class);
        main_menu = directory.getEntry("main_menu_pause", Texture.class);
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

    public ArrayList<Tentacle> tentacles = new ArrayList<>();

    /**
     * Immediately adds the object to the physics world
     * <p>
     * param obj The object to add
     */
    protected void addObject(GameObject obj) {
//        System.out.println("obj_x: " + obj.getX() + " obj_y: " + obj.getY());
//        System.out.println("bounds_x: " + bounds.x + " bounds_y: " + bounds.y);
        assert inBounds(obj) : "Object is not in bounds";
        obj.activatePhysics(world);
        if (obj instanceof Tentacle)
            tentacles.add((Tentacle) obj);
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
        clicked = false;
        exit_home = false;
        reach_target = false;
        Vector2 gravity = new Vector2(world.getGravity());
        for (GameObject obj : level.getAllObjects()) {
            obj.deactivatePhysics(world);
        }
        game_over_animation_time = 60;
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

//        camera.setZoom(1.0f);
//        System.out.println("SCALE:: " + world_scale.toString());
        levelBuilder.createLevel(levels[curr_level], level, world_scale, symbol_scale, rayHandler);
        pause = false;

        // TODO: will this have the same effect as going through each type, casting, then adding?
        for (GameObject obj : level.getAllObjects()) {
            addObject(obj);
//            System.out.println();
        }
//        System.out.println("added ");
        monsterController = new MonsterController(level.getMonster(), getWorldBounds());
        monsterController.setAudio(AudioController.getInstance());

        level.getDiver().initFlares(rayHandler);
        level.getDiver().setFlareFilmStrip(new FilmStrip(flareAnimation, 1, 4, 4));
        System.out.println("level populated");
    }

    private void updateGameState() {
        if(game_over_animation_time<=0) {
            if(game_state == state.DYING) {
                game_state = state.EXIT_LOSE;
            } else if (game_state == state.WIN_ANIMATION) {
                game_state = state.EXIT_WIN;
            }
        } else if (level.getDiver().getOxygenLevel() <= 0) {
            game_state = state.DYING;
        } else if (reach_target) {
//            System.out.println("REACH TARGET");
            game_state = state.WIN_ANIMATION;
        } else if (pause) {
            game_state = state.PAUSE;
        } else {
            game_state = state.PLAYING;
        }
    }

    private void updateDyingState() {
        changeLightColor(new Color(0,0,0,0));
    }

    private void updatePlayingState() {
        // apply movement
        InputController input = InputController.getInstance();

        if (input.isPause()) {
//            System.out.println("pAUSE??????????????");
            if (pause)
                resume();
            else
                pause();
        }

        level.getDiver().setHorizontalMovement(input.getHorizontal() * level.getDiver().getForce());
        level.getDiver().setVerticalMovement(input.getVertical() * level.getDiver().getForce());

//        System.out.println("Touching Obstacle: " + level.getDiver().isTouchingObstacle());
//        System.out.println("Latching: " + level.getDiver().isLatching());
//        System.out.println("KickOff: " + input.didKickOff());
//        System.out.println("Boosting: " + level.getDiver().isBoosting());
//        System.out.println("Diver Velocity: " + level.getDiver().getLinearVelocity().len());
//        System.out.println("Diver Mass: " + level.getDiver().getMass());

        // stop boosting when player has slowed down enough
        if (level.getDiver().getLinearVelocity().len() < 10 && level.getDiver().isBoosting()) {
            level.getDiver().setBoosting(false);
        }

        // store the facing direction, which cannot be 0, 0
        if (input.getHorizontal() != 0 || input.getVertical() != 0) {
            level.getDiver().setFacingDir(input.getHorizontal(), input.getVertical());
        }

//        level.getDiver().reduceInvincibleTime();
//        System.out.println("invincible time " + level.getDiver().getInvincibleTime());

        // set latching and boosting attributesf
        // latch onto obstacle when key pressed and close to an obstacle
        // stop latching and boost when key is let go
        // TODO: or when it is pressed again? Have had some issues with key presses being missed
        // otherwise, stop latching

        if (input.didKickOff() && !level.getDiver().isLatching() && level.getDiver().isTouchingObstacle() && level.getDiver().getTouchedWall() != null) {
            //System.out.println("Player Coords: " + level.getDiver().getPosition());
            //System.out.println("Wall Coords: " + level.getDiver().getTouchedWall().getPosition());
            int playerX = (int) level.getDiver().getPosition().x - 1;
            int playerY = (int) level.getDiver().getPosition().y - 1;
            int wallX = (int) level.getDiver().getTouchedWall().getPosition().x;
            int wallY = (int) level.getDiver().getTouchedWall().getPosition().y;
            if (Math.abs(level.getDiver().targetAngleY) >= 44) {
//                if (level.getDiver().getVerticalMovement() != 0)
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
            level.getDiver().setTouchedWall(null);
            level.getDiver().boost(); // boost according to the current user input
        }

        // set forces from ocean currents
        level.getDiver().setDriftMovement(physicsController.getCurrentVector(level.getDiver().getPosition()).x,
                physicsController.getCurrentVector(level.getDiver().getPosition()).y);
        // apply forces for movement
        if (!level.getDiver().getStunned()) {
            level.getDiver().applyForce();
//            System.out.println("APPLY FORCE????????????");
        }

        // do the ping
//        level.getDiver().setPing(input.didOpenChest());
//        level.getDiver().setPingDirection(level.getDeadBody().getPosition());

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

        if (input.didOpenChest()) {
            if (level.getDiver().getTreasureChests().size() > 0) {
                TreasureModel tm = level.getDiver().getTreasureChests().pop();
                tm.openChest();
                if (tm.getContents() == TreasureModel.TreasureType.Monster) {
                    Tentacle t = levelBuilder.createTentacle(0, 0.5f, tm, LevelBuilder.TentacleType.OldAttack, 30);
                    tm.setTrap(t);
                    addQueuedObject(t);
                } else if (tm.getContents() == TreasureModel.TreasureType.Key) {

                }
            }

        }


        if (level.getDiver().getBody() != null && !pause) {
//            cameraController.setCameraPosition(
//                    (level.getDiver().getX() + 1.5f) * level.getDiver().getDrawScale().x, (level.getDiver().getY() + .5f) * level.getDiver().getDrawScale().y);
            camera.setCameraPosition(
                    (level.getDiver().getX()) * level.getDiver().getDrawScale().x, (level.getDiver().getY()) * level.getDiver().getDrawScale().y );

            light.setPosition(
                    camera.getCameraPosition2D().x / (world_scale.x)
                    ,
                    camera.getCameraPosition2D().y / (world_scale.y));
            wallShine.setPosition(
                    camera.getCameraPosition2D().x / (world_scale.x)
                    ,
                    camera.getCameraPosition2D().y / (world_scale.y));
//            System.out.println("Light!: " + light.getPosition().toString());
//            light.setPosition(
//                    (level.getDiver().getX() * level.getDiver().getDrawScale().x) / 40f,
//                    (level.getDiver().getY() * level.getDiver().getDrawScale().y) / 40f);
//            wallShine.setPosition(
//                    (level.getDiver().getX() * level.getDiver().getDrawScale().x) / 40f,
//                    (level.getDiver().getY() * level.getDiver().getDrawScale().y) / 40f);
        }

        // TODO: why wasnt this in marco's code?

        updateDiverLighting();

        camera.render();
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
        input.readInput(bounds, world_scale);
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;

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
//        worldScale.set(world_scale.x, world_scale.y);
        for (Door door : level.getDoors()) {
            door.setActive(!door.getUnlock(level.getDiver().getItem()));
        }
//        System.out.println("ScALE: " + world_scale.toString());
        rayHandler.setCombinedMatrix(camera.getCamera().combined.cpy().scl(world_scale.x, world_scale.y, 1f));

//        System.out.println("isMonsterActive");
        if (monsterController.isMonsterActive()) {
            monsterController.update(hostileOxygenDrain, level.getDiver());
            Queue<Wall> tentacles = monsterController.getMonster().getTentacles();
            Queue<Wall> idle_tentacles = monsterController.getMonster().getIdleTentacles();
            Queue<Wall> attack_tentacles = monsterController.getMonster().getKillTentacles();


            while (tentacles.size() > 0) {
                Wall add_wall = tentacles.poll();
                if (add_wall != null && add_wall.canSpawnTentacle()) {
//                    System.out.println("CREATE TENTACLE");
                    Tentacle t = levelBuilder.createTentacle(level.getMonster().getAggravation(), 0.4f, add_wall, LevelBuilder.TentacleType.NewAttack, 40);
                    addQueuedObject(t);
                    t.setGrowRate(5);
                }
            }
            while (idle_tentacles.size() > 0) {
                Wall add_wall = idle_tentacles.poll();
                if (add_wall != null) {
//                    System.out.println("...............................................");
                    Tentacle t = levelBuilder.createTentacle(level.getMonster().getAggravation(), .4f, add_wall, LevelBuilder.TentacleType.Idle, 100);
                    t.setGrowRate(10);
                    t.setType(0);
                    addQueuedObject(t);
//                AudioController.getInstance().roar();
                }
            }
            while (attack_tentacles.size() > 0) {
                Wall add_wall = attack_tentacles.poll();
                if (add_wall != null && add_wall.canSpawnTentacle()) {
                    Tentacle t = levelBuilder.createTentacle(level.getMonster().getAggravation(), .6f, add_wall, LevelBuilder.TentacleType.KILL, 1000000);
                    t.setType(1);
                    t.setGrowRate(4);
//                    System.out.println("type" + t.getType());
                    addQueuedObject(t);
                }
            }
        }

        //** ADDING TENTACLES TO WalL!
//        if (level.getDiver().getTouchedWall() != null && level.getDiver().getTouchedWall().canSpawnTentacle()) {
//            Wall w = level.getDiver().getTouchedWall();
//            Tentacle t = levelBuilder.createTentacle(level.getMonster().getAggravation(), .4f, w, LevelBuilder.TentacleType.Idle, 100);
//            addQueuedObject(t);
//        }

//        System.out.println("STATE "+ game_state);
        switch (game_state) {
            case DYING:
                game_over_animation_time--;
                updateDyingState();
                break;
            case WIN_ANIMATION:
                game_over_animation_time--;
                // do other things here?
                break;
            case PLAYING:
                updatePlayingState();
                break;
            case PAUSE:
                InputController input = InputController.getInstance();
                if (input.isPause())
                    resume();
                // oxygen still drains when paused
                level.getDiver().changeOxygenLevel(passiveOxygenRate);
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

//        if (level.getDiver().isInvincible()) {
//            level.getDiver().setHazardInvincibilityFilter();
////            light.setContactFilter(no_hazard_collision_category, no_hazard_collision_group, no_hazard_collision_mask);
//        } else {
            level.getDiver().setHazardCollisionFilter();
//            light.setContactFilter(hazard_collision_category, hazard_collision_group, hazard_collision_mask);
//        }

        if (level.getDiver().getStunCooldown() > 0) {
//            System.out.println("PAIN: " + hostileOxygenDrain);
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
            level.getDiver().setStunCooldown(level.getDiver().getStunCooldown() - 1);
        } else {
            level.getDiver().setStunned(false);
            hostileOxygenDrain = 0.0f;
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
        }

    }

    public void updateDiverLighting() {
        if (monsterController.isMonsterActive() && level.getMonster().getAggravation() > level.getMonster().getAggroLevel()) {
            changeLightColor(monster_color);
        } else if (level.getDiver().getOxygenLevel() < level.getDiver().getMaxOxygen() * .25f) {
            changeLightColor(low_oxygen_color);
        } else {
            changeLightColor(Color.BLACK);
        }

        if (level.getDiver().getStunned()) {
            if (light.getDistance() > stun_light_radius) {
                light.setDistance(light.getDistance() - 1);
            }
        } else {
            if (light.getDistance() < normal_light_radius) {
                light.setDistance(light.getDistance() + 1);
            }
        }

    }

    public void changeLightColor(Color color) {
        float curr_a = light.getColor().a;
        float curr_r = light.getColor().r;
        float curr_g = light.getColor().g;
        float curr_b = light.getColor().b;
        if (curr_a != color.a) {
            curr_a += .01f * Math.signum(color.a - curr_a);
        }
        if (curr_r != color.r) {
            curr_r += .01f * Math.signum(color.r - curr_r);
        }
        if (curr_g != color.g) {
            curr_g += .01f * Math.signum(color.g - curr_g);
        }
        if (curr_b != color.b) {
            curr_b += .01f * Math.signum(color.b - curr_b);
        }
        light.setColor(new Color(curr_r, curr_g, curr_b, curr_a));
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
        for (int i = 0; i < tentacles.size(); i++) {
            Tentacle t = tentacles.get(i);

            if (t.isDead()) {
                tentacles.remove(t);
                i--;
                t.deactivatePhysics(world);
                level.getAllObjects().remove(t);
            }

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

    /**
     * Draw the physics objects to the canvas
     * <p>
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     * <p>
     * The method draws all objects in the order that they were added.
     */

    public boolean help_draw(Texture t, float x, float y, boolean tint) {

        int pW = canvas.getWidth() / t.getWidth();
        float w = canvas.getWidth() / (2 * pW);

        int pH = canvas.getHeight() / t.getHeight();
        float h = canvas.getHeight() / (2 * pH);

        int ox = t.getWidth() / 2;
        int oy = t.getHeight() / 2;
        Color c = Color.WHITE;
        boolean clicked = false;

        if (tint) {
            Vector3 pointer = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            pointer = camera.getCamera().unproject(pointer);
            float pX = pointer.x;
            float pY = pointer.y;

            if ((x + w > pX && x - w < pX) && (y + h > pY && y - h < pY)) {
                c = Color.GRAY;
                if (Gdx.input.isTouched()) clicked = true;
            }
        }
        canvas.draw(t, c, ox, oy, x, y, 0, 1 * worldScale.x, worldScale.x);
        return clicked;
    }

    int tick = 0;

    public void draw(float dt) {
        tick++;
        canvas.clear();
        canvas.begin();

        // draw game objects
        canvas.draw(background, com.badlogic.gdx.graphics.Color.WHITE, 0, 0, -500, -250, 0,
                4 * worldScale.x, 4 * worldScale.y);

        for (GameObject obj : level.getAllObjects()) {
//        for (int i = level.getAllObjects().size() - 1; i >= 0; i--) {
//            GameObject obj = level.getAllObjects().get(i);
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
        }
        canvas.begin();

        switch (game_state) {
            case PLAYING:

                tempProjectedHud.x = (float) canvas.getWidth() / 2;
                tempProjectedHud.y = 0f;
                tempProjectedHud = camera.getCamera().unproject(tempProjectedHud);

                //draw hud background

                canvas.draw(hud, Color.WHITE, hud.getRegionWidth() / 2, hud.getRegionHeight(),
                        tempProjectedHud.x, tempProjectedHud.y,
                        0.0f, 1, 0.5f);

                //draw remaining oxygen
                tempProjectedOxygen.x = (float) canvas.getWidth() / 5.5f;
                tempProjectedOxygen.y = (45 * canvas.getHeight()) / 1080;
                tempProjectedOxygen = camera.getCamera().unproject(tempProjectedOxygen);

                if (level.getDiver().getStunCooldown() % 20 > 10) {

                    canvas.draw(oxygen, Color.BLUE, 0, (float) oxygen.getRegionHeight() / 2,
                            (float) tempProjectedOxygen.x,
                            tempProjectedOxygen.y,
                            0.0f,
                            1f * (level.getDiver().getOxygenLevel() / (float) level.getDiver().getMaxOxygen()) * worldScale.x,
                            0.5f * worldScale.y
                    );

                } else if (level.getDiver().getOxygenLevel() < level.getDiver().getMaxOxygen() / 4 && tick % 25 > (level.getDiver().getOxygenLevel() / 2)) {
                    canvas.draw(oxygen, Color.BLUE, 0, (float) oxygen.getRegionHeight() / 2,
                            (float) tempProjectedOxygen.x,
                            tempProjectedOxygen.y,
                            0.0f,
                            1f * (level.getDiver().getOxygenLevel() / (float) level.getDiver().getMaxOxygen()) * worldScale.x,
                            0.5f * worldScale.y
                    );
                } else {
                    canvas.draw(oxygen, Color.WHITE, 0, (float) oxygen.getRegionHeight() / 2,
                            (float) tempProjectedOxygen.x,
                            tempProjectedOxygen.y,
                            0.0f,
                            1f * (level.getDiver().getOxygenLevel() / (float) level.getDiver().getMaxOxygen()) * worldScale.x,
                            0.5f * worldScale.y
                    );
                }


                canvas.draw(oxygenText, Color.WHITE, (float) oxygenText.getRegionWidth() / 2, (float) oxygenText.getRegionHeight() / 2,
                        tempProjectedHud.x, tempProjectedOxygen.y,
                        0.0f, 0.5f * worldScale.x, 0.5f * worldScale.y);

                //draw inventory indicator
                if (level.getDiver().carryingItem()) {
                    canvas.draw(keyHud, level.getDiver().getItem().getColor(), (float) keyHud.getRegionWidth(), (float) keyHud.getRegionHeight() / 2,
                            tempProjectedOxygen.x - 50,
                            tempProjectedOxygen.y,
                            0.0f, 0.35f * worldScale.x, 0.35f * worldScale.y);

                }

                //draw body indicator
                if (level.getDiver().hasBody()) {
                    canvas.draw(bodyHud, Color.WHITE, 0, (float) bodyHud.getRegionHeight() / 2,
                            tempProjectedHud.x + 50 + (camera.getCameraPosition2D().x - tempProjectedOxygen.x),
                            tempProjectedOxygen.y,
                            0.0f, 0.35f * worldScale.x, 0.35f * worldScale.y);
                }
                for (int i = 0; i < level.getDiver().getRemainingFlares(); i++) {
                    canvas.draw(flareHud, Color.WHITE, (float) flareHud.getRegionWidth(), (float) flareHud.getRegionHeight() / 2,
                            tempProjectedOxygen.x - 10 * i,
                            tempProjectedOxygen.y,
                            0.0f, 0.35f * worldScale.x, 0.35f * worldScale.y);
                }

                break;

            case PAUSE:

                // camera position tracks the position in game
                camera.render();

                pauseScreen.x = (float) canvas.getWidth() / 2;
                pauseScreen.y = (float) canvas.getHeight() / 2;
                pauseScreen = camera.getCamera().unproject(pauseScreen);

                //the scale is eyeballed, and resizing during the pause moves the camera weird
                //Solution: not a see through pause screen
                canvas.draw(pause_screen, Color.WHITE, pause_screen.getWidth()/2, pause_screen.getHeight()/2, pauseScreen.x,
                        pauseScreen.y, 0.0f, 0.75f, 0.75f);


                Vector3 resume_button = new Vector3(0, 0, 0);
                resume_button.x = (float) canvas.getWidth() / 2;
                resume_button.y = (float) canvas.getHeight() / 2 - canvas.getHeight() / 8;
                resume_button = camera.getCamera().unproject(resume_button);

                press_resume = help_draw(resume, resume_button.x, resume_button.y, true);

                Vector3 restart_button = new Vector3(0, 0, 0);
                restart_button.x = (float) canvas.getWidth() / 2;
                restart_button.y = (float) canvas.getHeight() / 2;
                restart_button = camera.getCamera().unproject(restart_button);

                press_restart = help_draw(restart, restart_button.x, restart_button.y, true);

                Vector3 exit_button = new Vector3(0, 0, 0);
                exit_button.x = (float) canvas.getWidth() / 2;
                exit_button.y = (float) canvas.getHeight() / 2 + canvas.getHeight() / 8;
                exit_button = camera.getCamera().unproject(exit_button);

                exit_home = help_draw(main_menu, exit_button.x, exit_button.y, true);

            case EXIT_WIN:
                break;
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
            if (game_state == state.EXIT_WIN) {
                listener.exitScreen(this, 0);
            } else if (game_state == state.EXIT_LOSE) {
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
//        System.out.println("pause() was called");
        pause = true;
        updateGameState();
    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
     * This is usually when it regains focus.
     */
    public void resume() {
//        System.out.println("resume");
        pause = false;
        updateGameState();
        // TODO Auto-generated method stub
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
        if (listener != null && !reach_target) {
            reach_target = collisionController.getWinState(body1, body2, level.getDiver());

        }

//        collisionController.addDiverSensorTouching(level.getDiver(), fix1, fix2);
        collisionController.startDiverItemCollision(body1, body2);
        collisionController.startDiverDoorCollision(body1, body2);
        collisionController.startMonsterWallCollision(body1, body2);
        collisionController.startDiverDeadBodyCollision(body1, body2);
        collisionController.startDiverMonsterCollision(body1, body2);
        collisionController.startFlareTentacleCollision(fix1, fix2);

        collisionController.startDiverTreasureCollision(fix1, fix2);

        collisionController.startFlareFlare(body1, body2);
        collisionController.startDiverFlare(body1, body2);

        float d = collisionController.startDiverHazardCollision(fix1, fix2, level.getDiver(), monsterController);
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

        collisionController.endDiverTreasureCollision(fix1, fix2);

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

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     * <p>
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (exit_home) {
            listener.exitScreen(this, 2);
        }
        if (press_restart) {
            press_restart = false;
            reset();
        }
        if (press_resume) {
            press_resume = false;
            resume();
        }

        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     * <p>
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown(Controller controller, int buttonCode) {
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     * <p>
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp(Controller controller, int buttonCode) {
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected(Controller controller) {
    }

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected(Controller controller) {
    }

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     * <p>
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode   The axis moved
     * @param value      The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return true;
    }

}
