package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
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
import com.xstudios.salvage.util.ScreenListener;

import java.util.Iterator;
import java.util.Queue;

public class GameController implements Screen, ContactListener, InputProcessor {

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
    // Beta Release Setup
//    private String[] levels = {"beta_0", "beta_1", "beta_3"};

    private String[] levels = {"test_level", "level1", "level3"};

    private int curr_level;

    private TextureRegion test;

    float scale1;


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
    boolean exit_home;
    boolean press_resume;
    boolean press_restart;

    private Player player;

    private LevelBuilder levelBuilder;
    private LevelModel level;

    private PointLight light;
    private RayHandler rayHandler;

    private PointLight wallShine;

    AudioController audio;


    // ======================= CONSTRUCTORS =================================

    /**
     * Creates a new game world with the default values.
     * <p>
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected GameController(Player player) {
        this(new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT),
                new Vector2(0, DEFAULT_GRAVITY), player);
        pause = false;
        exit_home = false;
        press_restart = false;
        press_resume = false;
    }

    public int getTotalLevels(){
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
    protected GameController(float width, float height, float gravity, Player player) {
        this(new Rectangle(0, 0, width, height), new Vector2(0, gravity), player);
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

    protected GameController(Rectangle bounds, Vector2 gravity, Player player) {
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

        audio = AudioController.getInstance((float)player.getMusic(), (float)player.getSoundEffects());
        audio.initialize();
        collisionController = new CollisionController();
        collisionController.setAudio(audio);
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

    public AudioController getAudio(){
        return audio;
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

    public void setDefaultPosition(){
        cameraController.setCameraPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
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
        level.dispose();
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        canvas = null;
        pause = false;
        exit_home = false;
        press_restart = false;
        press_resume = false;
        audio.dispose();
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
        monsterTenctacle = directory.getEntry("models:monster1", Texture.class);
        plantAnimation = directory.getEntry("models:plant", Texture.class);

        //pause
        pause_screen = directory.getEntry( "pause", Texture.class);
        black_spot = directory.getEntry( "black_spot", Texture.class);
        resume = directory.getEntry( "resume", Texture.class);
        restart = directory.getEntry( "restart", Texture.class);
        main_menu = directory.getEntry( "main_menu_pause", Texture.class);
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
        exit_home = false;
        Vector2 gravity = new Vector2(world.getGravity());
        for (GameObject obj : level.getAllObjects()) {
            obj.deactivatePhysics(world);
        }

        level.getAllObjects().clear();
        level.getAboveObjects().clear();
        addQueue.clear();
        audio.reset();
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
        } else if(pause) {
            game_state = state.PAUSE;
        } else {
            game_state = state.PLAYING;
        }
    }



    private void updatePlayingState() {
        // apply movement
        InputController input = InputController.getInstance();

        if(input.isPause()){
            pause();
        }

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
            if (Math.abs(input.getHorizontal()) > 0 || Math.abs(input.getVertical()) > 0) {
                level.getDiver().changeOxygenLevel(activeOxygenRate);
                // TODO: faster oxygen drain while carrying the body
            } else {
                level.getDiver().changeOxygenLevel(passiveOxygenRate);
            }
        }


        // update audio according to oxygen level

        audio.update(level.getDiver().getOxygenLevel(), level.getDiver().getMaxOxygen());


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

        monsterController.update(hostileOxygenDrain, level.getDiver());
        Queue<Wall> tentacles = monsterController.getMonster().getTentacles();

        while (tentacles.size() > 0) {
            Wall add_wall = tentacles.poll();
            if (add_wall.canSpawnTentacle() && add_wall != null) {
                Tentacle t = levelBuilder.createTentcle(add_wall, new FilmStrip(monsterTenctacle, 1, 30, 30), scale);
                addQueuedObject(t);
                audio.roar();
            }
        }

        switch (game_state) {
            case PLAYING:
                updatePlayingState();
                break;
            case PAUSE:
                InputController input = InputController.getInstance();
                if(input.isPause()){
                    resume();
                }
                updatePlayingState();
                break;
        }
        updateGameState();

        if (level.getDiver().getStunCooldown() > 0) {
            System.out.println("PAIN: " + hostileOxygenDrain);
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
            level.getDiver().setStunCooldown(level.getDiver().getStunCooldown() - 1);

        } else {

            level.getDiver().setStunned(false);
            hostileOxygenDrain = 0.0f;
            level.getDiver().changeOxygenLevel(hostileOxygenDrain);
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

    /**
     * Draw the physics objects to the canvas
     * <p>
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     * <p>
     * The method draws all objects in the order that they were added.
     *
     */

    public boolean help_draw (Texture t, float x, float y, boolean tint) {
        int ox = t.getWidth()/2;
        int oy = t.getHeight()/2;
        Color c = Color.WHITE;
        boolean clicked = false;

        if(tint) {
            int pX = Gdx.input.getX();
            int pY = Gdx.graphics.getHeight() - Gdx.input.getY();
            float y1 =  y + (int)(Gdx.graphics.getHeight()/2 - cameraController.getCameraPosition2D().y);
            float x1 =  x + (int) (Gdx.graphics.getWidth()/2 - cameraController.getCameraPosition2D().x);
            float w = scale1 * ox;
            float h = scale1 * oy;

            if ((x1 + w > pX && x1 - w < pX) && (y1 + h > pY && y1 - h < pY)) {
                c = Color.GRAY;
                if (Gdx.input.isTouched()) clicked = true;
            }
        }
        canvas.draw(t, c, ox, oy, x, y, 0, scale1, scale1);
        return clicked;
    }

    int tick = 0;

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
                float cX = cameraController.getCameraPosition2D().x;
                float cY = cameraController.getCameraPosition2D().y;
                if (Gdx.input.justTouched()) {
                    System.out.println("x: " + cX + " y: " + cY);
                    System.out.println(Gdx.input.getX() + " " + Gdx.input.getY());
                }

                canvas.draw(pause_screen, Color.WHITE, cX - canvas.getWidth() / 2f,
                        cY - canvas.getHeight() / 2f, canvas.getWidth(), canvas.getHeight());

                press_resume = help_draw(resume, cX, cY + canvas.getHeight() / 10, true);
                press_restart = help_draw(restart, cX, cY, true);
                exit_home = help_draw(main_menu, cX, cY - canvas.getHeight() / 10, true);

            case WIN_GAME:
                break;
            }

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
        float sx = ((float)width)/1280;
        float sy = ((float)height)/720;
        scale1 = 0.75f * (sx < sy ? sx : sy);
        cameraController.getCamera().setToOrtho(false, width, height);
        cameraController.getCamera().update();
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
        Gdx.input.setInputProcessor(this);
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        Gdx.input.setInputProcessor(null);
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
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
         if (exit_home){
            listener.exitScreen(this, 2);
         }
         if(press_restart){
             press_restart = false;
             reset();
         }
         if(press_resume){
             press_resume = false;
             resume();
         }

        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
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
     *
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
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }


}
