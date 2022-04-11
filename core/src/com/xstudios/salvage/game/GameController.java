package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.audio.AudioController;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Wall;
import com.xstudios.salvage.game.models.*;
import com.xstudios.salvage.util.PooledList;
import com.xstudios.salvage.util.ScreenListener;

import java.util.Iterator;

public class GameController implements Screen, ContactListener {
    // Assets
    /** The texture for diver */
    protected TextureRegion diverTexture;
    /** The texture for item */
    protected TextureRegion itemTexture;
   /** Ocean Background Texture */
    protected TextureRegion background;
    /** The texture for ping */
    protected TextureRegion pingTexture;
    /** The texture for dead body */
    protected TextureRegion deadBodyTexture;
    /** The texture for dead body */
    protected TextureRegion doorTexture;
    /** Texturs for the door */
    protected TextureRegion doorOpenTexture;
    protected TextureRegion doorCloseTexture;

    JsonValue constants;

    // Models to be updated
    protected TextureRegion wallTexture;
    protected TextureRegion wallBackTexture;

    /** The font for giving messages to the player */
    public static BitmapFont displayFont;


    // Models to be updated
    protected DiverModel diver;

    protected ItemModel key;
//    protected ItemModel dead_body;
    protected DeadBodyModel dead_body;
    protected GoalDoor goal_door;

    private Array<Door> doors=new Array<Door>();

    /** Camera centered on the player */
    protected CameraController cameraController;

    /** manages collisions */
    protected CollisionController collisionController;
    /** The rate at which oxygen should decrease passively */
    protected float passiveOxygenRate;
    protected float activeOxygenRate;

    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 120;
    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** Aspect ratio of the world*/
    protected static final float ASPECT_RATIO = DEFAULT_WIDTH/DEFAULT_HEIGHT;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = 0;//-4.9f;


    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** All the objects in the world. */
    protected PooledList<GameObject> objects  = new PooledList<GameObject>();

    protected PooledList<GameObject> aboveObjects  = new PooledList<GameObject>();
    /** Queue for adding objects */
    protected PooledList<GameObject> addQueue = new PooledList<GameObject>();
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The symbol scale */
    protected Vector2 symbol_scale;


    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether or not debug mode is active */
    private boolean debug;

    private Vector2 forceCache;
    private AudioController audioController;
    private PhysicsController physicsController;

    private boolean reach_target = false;

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

//    private LightController lightController;
    private PointLight light ;
    private RayHandler rayHandler;


    //sample wall to get rid of later
    public float[][] wall_indices={{16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
            1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 18.0f},
            {32.0f, 18.0f, 32.0f,  0.0f, 31.0f,  0.0f,
            31.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f}};
    // ======================= CONSTRUCTORS =================================
    /**
     * Creates a new game world with the default values.
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected GameController() {
        this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),
            new Vector2(0,DEFAULT_GRAVITY));

    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width  	The width in Box2d coordinates
     * @param height	The height in Box2d coordinates
     * @param gravity	The downward gravity
     */
    protected GameController(float width, float height, float gravity) {
        this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds	The game bounds in Box2d coordinates
     * @param gravity	The gravitational force on this Box2d world
     */
    protected GameController(Rectangle bounds, Vector2 gravity) {
        world = new World(gravity.scl(1),false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        this.symbol_scale = new Vector2(.4f, .4f);
        debug  = false;
        active = false;
        // TODO: oxygen rate should be a parameter loaded from a json
        passiveOxygenRate = -.01f;
        activeOxygenRate = -.02f;

        forceCache = new Vector2(0, 0);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(.01f);


        light = new PointLight(rayHandler,100, Color.BLACK,10,0,0);
//        light.setContactFilter((short)1,(short)1,(short)1);

        Filter f = new Filter();
        f.categoryBits = 0x0002;
        f.maskBits =0x0004;
        f.groupIndex = 1;


        light.setContactFilter(f);
        System.out.println("BG: "+background);
        audioController = new AudioController(100.0f);
        audioController.intialize();
        collisionController = new CollisionController();
        physicsController = new PhysicsController(10, 5);
        world.setContactListener(this);
        game_state = state.PLAYING;
    }

    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive( ) {
        return active;
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    public void setCameraController(  CameraController cameraController){
        this.cameraController = cameraController;
        cameraController.setBounds(0,0,5400*2/5,3035*2/5);
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for(GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }
        for(GameObject obj : aboveObjects) {
            obj.deactivatePhysics(world);


        }
        aboveObjects.clear();
        objects.clear();
        addQueue.clear();
        world.dispose();
        rayHandler.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale  = null;
        world  = null;
        canvas = null;
        audioController.dispose();
    }

    /**
     * Gather the assets for this controller.
     *
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory	Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Allocate the tiles
        diverTexture = new TextureRegion(directory.getEntry( "models:diver", Texture.class ));
        background = new TextureRegion(directory.getEntry( "background:ocean", Texture.class ));
        itemTexture = new TextureRegion(directory.getEntry("models:key", Texture.class));
        constants =  directory.getEntry( "models:constants", JsonValue.class );
        pingTexture = new TextureRegion(directory.getEntry( "models:ping", Texture.class ));
        wallTexture = new TextureRegion(directory.getEntry( "wall", Texture.class ));
        doorTexture= new TextureRegion(directory.getEntry( "door", Texture.class ));
        //wallBackTexture = new TextureRegion(directory.getEntry( "background:wooden_bg", Texture.class ));
        doorOpenTexture = new TextureRegion(directory.getEntry( "models:door_open", Texture.class ));
        doorCloseTexture = new TextureRegion(directory.getEntry( "models:door_closed", Texture.class ));
        displayFont = directory.getEntry("fonts:lightpixel", BitmapFont.class);
        deadBodyTexture = new TextureRegion(directory.getEntry( "models:dead_body", Texture.class ));
    }

    /**
     *
     * Adds a physics object in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     *
     * param obj The object to add
     */
    public void addQueuedObject(GameObject obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(GameObject obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);

    }


    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(GameObject obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    public void reset() {
        game_state = state.PLAYING;
        Vector2 gravity = new Vector2(world.getGravity() );
        for(GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }

        objects.clear();
        addQueue.clear();
//        world.dispose();

//        world = new World(gravity,false);
//        world.setContactListener(this);
        populateLevel();
    }
    /**
     * Lays out the game geography.
     */
    private void populateLevel() {

        diver = new DiverModel(constants.get("diver"),diverTexture.getRegionWidth(),
            diverTexture.getRegionHeight());

        diver.setTexture(diverTexture);
        diver.setPingTexture(pingTexture);
        diver.setDrawScale(scale);
        diver.setName("diver");

        addObject(diver);

        light.setPosition((diver.getX()*diver.getDrawScale().x)/32f,(diver.getY()*diver.getDrawScale().y)/32f);

        key = new ItemModel(constants.get("key"),itemTexture.getRegionWidth(),
                itemTexture.getRegionHeight(), ItemType.KEY, 0);
        key.setTexture(itemTexture);
        key.setBodyType(BodyDef.BodyType.StaticBody);
        key.setDrawScale(scale);
        key.setDrawSymbolScale(symbol_scale);
        key.setName("key");
        key.setGravityScale(0f);
        key.setSensor(true);

        addObject(key);

//        dead_body = new ItemModel(constants.get("dead_body"),deadBodyTexture.getRegionWidth(),
//                deadBodyTexture.getRegionHeight(), ItemType.DEAD_BODY, 0);

        dead_body = new DeadBodyModel(constants.get("dead_body"),deadBodyTexture.getRegionWidth(),
                deadBodyTexture.getRegionHeight());

        dead_body.setTexture(deadBodyTexture);
        dead_body.setDrawScale(scale);
        dead_body.setDrawSymbolScale(symbol_scale);
        dead_body.setName("dead_body");
        dead_body.setGravityScale(0f);
        dead_body.setSensor(true);
        diver.setDeadBody(dead_body);

        addObject(dead_body);

        JsonValue goal = constants.get("goal");
        goal_door = new GoalDoor(diver.getX(),diver.getY(),
                goal.getFloat("width"),goal.getFloat("height"));
        goal_door.setBodyType(BodyDef.BodyType.StaticBody);
        goal_door.setDensity(goal.getFloat("density", 0));
        goal_door.setFriction(goal.getFloat("friction", 0));
        goal_door.setRestitution(goal.getFloat("restitution", 0));
        goal_door.setSensor(true);
        goal_door.setDrawScale(scale);
        goal_door.setTexture(doorOpenTexture);
        goal_door.setName("goal");
        addObject(goal_door);


        float[][] wallVerts={

                //walls
            {-50.0f, 18.0f, -40.0f, 0.0f, -39.5f,  0.0f, -49.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f,},
            { 46.0f, 18.0f,  32.0f, -9.0f,  31.0f,  -10.0f,  45.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f},
                //first floor
            { -35.0f, -9.0f, -35.0f, -10.0f ,  32.0f, -10.0f,  32.0f, -9.0f},

            { -40.5f, 0.0f, -40.0f, -1.0f,  -13.0f, -1.0f,  -13.0f, 0.0f},
            { -3.0f, 0.0f, -3.0f, -9.0f, -2.0f, -9.0f, -2.0f, -1.0f,  6.0f, -1.0f, 6.0f, 0.0f},
            { 14.0f, 0.0f, 14.0f, -4.0f, 15.0f, -4.0f, 15.0f, -1.0f, 28.0f, -1.0f, 28.0f, 0.0f},

                //second floor
            { -33.0f, 9.0f, -33.0f, 8.0f , 32.0f, 8.0f, 32.0f, 9.0f},
            { 22.0f, 8.0f, 22.0f, 0.0f , 23.0f, 0.0f, 23.0f, 8.0f},
                //third floor
            {-10.0f, 17.0f, -10.0f, 9.0f , -9.0f, 9.0f, -9.0f, 17.0f},
            {20.0f, 17.0f, 20.0f, 13.0f , 21f, 13.0f, 21f, 17.0f}
        };

        for (int ii = 0; ii < wallVerts.length; ii++) {
            Wall obj;
            obj = new Wall(wallVerts[ii], 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(0);
            obj.setFriction(0.4f);
            obj.setRestitution(0.1f);
            obj.setDrawScale(scale);
            obj.setTexture(wallTexture);
            obj.setDrawScale(scale);
            obj.setName("wall "+ii);
            addObject(obj);
        }


        float[] doorverts= {14f, -4.0f, 14f, -9.0f, 14.5f, -4.0f, 14.5f, -9.0f};
        Door door=new Door(doorverts, 0,0, key);
        door.setBodyType(BodyDef.BodyType.StaticBody);
        door.setTexture(doorTexture);
        door.addTextures(doorCloseTexture,doorOpenTexture);
        door.setDrawScale(scale);
        door.setName("door");
        addObject(door);
        door.setUserData(door);
        door.setActive(true);
        doors.add(door);

        float[] doorverts1= { 20.0f, 13.0f, 20.0f, 9.0f , 20.5f, 9.0f, 20.5f, 13.0f};
        Door door1=new Door(doorverts1, 0,0, key);
        door1.setBodyType(BodyDef.BodyType.StaticBody);
        door1.setTexture(doorTexture);
        door.addTextures(doorCloseTexture,doorOpenTexture);
        door1.setDrawScale(scale);
        door1.setName("door1");
        addObject(door1);
        door1.setUserData(door1);

        door1.setActive(true);
        doors.add(door1);

    }

    private void updateGameState() {
        if(diver.getOxygenLevel() <= 0) {
            game_state = state.LOSE_GAME;
        }
    }

    private void updatePlayingState() {
        // apply movement
        InputController input = InputController.getInstance();
        diver.setHorizontalMovement(input.getHorizontal() * diver.getForce());
        diver.setVerticalMovement(input.getVertical() * diver.getForce());

        // stop boosting when player has slowed down enough
        if (diver.getLinearVelocity().len() < 15 && diver.isBoosting()) {
            diver.setBoosting(false);
        }
        // set latching and boosting attributes
        // latch onto obstacle when key pressed and close to an obstacle
        // stop latching and boost when key is let go
        // TODO: or when it is pressed again? Have had some issues with key presses being missed
        // otherwise, stop latching
        if (input.didKickOff() && diver.isTouchingObstacle()) {
            diver.setLatching(true);
        } else if (!input.didKickOff() && diver.isLatching()) {
            diver.setLatching(false);
            diver.setBoosting(true);
            diver.boost(); // boost according to the current user input
        } else {
            diver.setLatching(false);
        }
        System.out.println("isTouchingObstacle?: " + diver.isTouchingObstacle());
        System.out.println("Latching: " + diver.isLatching());
        System.out.println("Boosting? " + diver.isBoosting());

        // set forces from ocean currents
        diver.setDriftMovement(physicsController.getCurrentVector(diver.getPosition()).x,
                physicsController.getCurrentVector(diver.getPosition()).y);
        // apply forces for movement
        diver.applyForce();

        // do the ping
        diver.setPing(input.didPing());
        diver.setPingDirection(dead_body.getPosition());

        // manage items/dead body
        diver.setPickUpOrDrop(input.getOrDropObject());
        diver.setItem();
        dead_body.setCarried(diver.hasBody());

        // decrease oxygen from movement
        if (Math.abs(input.getHorizontal()) > 0 || Math.abs(input.getVertical()) > 0) {
            diver.changeOxygenLevel(activeOxygenRate);
            // TODO: faster oxygen drain while carrying the body
        } else {
            diver.changeOxygenLevel(passiveOxygenRate);
        }

        // update audio according to oxygen level
        audioController.update(diver.getOxygenLevel());

        if (diver.getBody() != null) {
            cameraController.setCameraPosition(
                    diver.getX() * diver.getDrawScale().x, diver.getY() * diver.getDrawScale().y);
            //
            light.setPosition(
                    (diver.getX() * diver.getDrawScale().x) / 40f,
                    (diver.getY() * diver.getDrawScale().y) / 40f);
        }

        // TODO: why wasnt this in marco's code?

        cameraController.render();
    }
    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt	Number of seconds since last animation frame
     *
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

        // Handle resets
        if (input.didReset() || game_state == state.RESTART) {
            reset();
        }
        return true;


    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        for (Door door: doors){
            door.setActive(!door.getUnlock());
        }

        rayHandler.setCombinedMatrix(cameraController.getCamera().combined.cpy().scl(40f));

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

        updateGameState();
    }

    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<GameObject>.Entry> iterator = objects.entryIterator();
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
        iterator = aboveObjects.entryIterator();
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
     *
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void draw(float dt) {
        canvas.clear();

        canvas.begin();
        // draw game objects
        canvas.draw(background, com.badlogic.gdx.graphics.Color.WHITE,background.getRegionWidth()/2f,background.getRegionHeight()/2f,0,0,0,4,4);
        for(GameObject obj : objects) {
            obj.draw(canvas);
        }

        for(GameObject obj : aboveObjects) {
            obj.draw(canvas);
        }

        if(!debug)
            rayHandler.updateAndRender();
        canvas.end();
        canvas.begin();
        switch (game_state) {
            case PLAYING:
                canvas.drawText(

                        "Oxygen Level: " + (int) diver.getOxygenLevel(),
                        displayFont,
                        cameraController.getCameraPosition2D().x - canvas.getWidth()/2f + 50,
                        cameraController.getCameraPosition2D().y - canvas.getHeight()/2f + 50);
            break;
            case WIN_GAME:
                System.out.println( "TEXT POS" +
                        cameraController.getCameraPosition2D().x + " " +
                        cameraController.getCameraPosition2D().y );
//                canvas.drawText("you win! Press R to restart",
//                        displayFont,
//                        cameraController.getCameraPosition2D().x - 100,
//                        cameraController.getCameraPosition2D().y );
            break;
//            case LOSE_GAME:
//                canvas.drawText("you lose :( Press R to restart",
//                        displayFont,
//                        cameraController.getCameraPosition2D().x - 100,
//                        cameraController.getCameraPosition2D().y );
//            break;

        }

        for(GameObject o: objects) {
            if(o instanceof DiverObjectModel && ((DiverObjectModel)o).isCarried()) {
                DiverObjectModel d_obj = (DiverObjectModel)o;

                canvas.draw(d_obj.getTexture(), d_obj.getColor(), d_obj.origin.x, d_obj.origin.y,
                        cameraController.getCameraPosition2D().x - canvas.getWidth()/2f + d_obj.getDrawSymbolPos().x,
                        cameraController.getCameraPosition2D().y - canvas.getHeight()/2f + d_obj.getDrawSymbolPos().y,
                        d_obj.getAngle(), d_obj.getDrawSymbolScale().x, d_obj.getDrawSymbolScale().y);
            }
        }
        canvas.end();

        if(debug) {
            canvas.beginDebug();
            for (GameObject obj : objects) {
                if (!(obj instanceof Wall)) {
                    obj.drawDebug(canvas);
                }
            }
            canvas.endDebug();
        }
    }


    /**
     * Called when the Screen is resized.
     *
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
     *
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
            if(game_state == state.WIN_GAME) {
                listener.exitScreen(this, 0);
            } else if (game_state == state.LOSE_GAME) {
                listener.exitScreen(this, 1);
            }
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
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
     *
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

        try {
            GObject bd1 = (GObject)body1.getUserData();
            GObject bd2 = (GObject)body2.getUserData();

            if ((diver.getSensorNameLeft().equals(fd2) && diver != bd1) ||
                    (diver.getSensorNameLeft().equals(fd1) && diver != bd2)) {

               if(diver != bd1)
                diver.addTouching(diver.getSensorNameLeft(),bd1);
               else
                   diver.addTouching(diver.getSensorNameLeft(),bd2);

            }
            if ((diver.getSensorNameRight().equals(fd2) && diver != bd1) ||
                    (diver.getSensorNameRight().equals(fd1) && diver != bd2)) {

                if(diver != bd1)
                    diver.addTouching(diver.getSensorNameRight(),bd1);
                else
                    diver.addTouching(diver.getSensorNameRight(),bd2);

            }

            if(bd1 instanceof DiverModel && !diver.getSensorNameRight().equals(fd1) && !diver.getSensorNameLeft().equals(fd1)  && bd2 instanceof Wall){
                audioController.wall_collision(diver.getForce());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Call CollisionController to handle collisions
//        System.out.println("BEGIN CONTACT");
        collisionController.startContact(body1, body2);

        if(body1.getUserData() instanceof DiverModel){
            if(body2.getUserData() instanceof ItemModel){
                CollisionController.pickUp(diver, (ItemModel) body2.getUserData());
               ((ItemModel) body2.getUserData()).setTouched(true);
            } else if(body2.getUserData() instanceof Door){
                System.out.println("Attempt Unlock");
//                toUnlock=CollisionController.attemptUnlock(diver, (Door)body2.getUserData());
                ((Door)body2.getUserData()).setUnlock(CollisionController.attemptUnlock(diver, (Door)body2.getUserData()));
            }  else if(body2.getUserData() instanceof DeadBodyModel){
                ((DiverModel) body1.getUserData()).setBodyContact(true);
            }


        }
        else if (body2.getUserData() instanceof DiverModel){
            if (body1.getUserData() instanceof ItemModel){
                CollisionController.pickUp(diver, (ItemModel)body1.getUserData());
                ((ItemModel) body1.getUserData()).setTouched(true);
            } else if(body1.getUserData() instanceof Door){
                System.out.println("Attempt Unlock");
//                toUnlock=CollisionController.attemptUnlock(diver, (Door)body1.getUserData());
                ((Door)body1.getUserData()).setUnlock(CollisionController.attemptUnlock(diver, (Door)body1.getUserData()));

            } else if(body1.getUserData() instanceof DeadBodyModel){
                ((DiverModel) body2.getUserData()).setBodyContact(true);
            }

        }

        if(body1.getUserData() instanceof DiverModel){
            if(body2.getUserData() instanceof GoalDoor){
                if(CollisionController.winGame(diver, (GoalDoor) body2.getUserData())
                        && listener!=null) {
                   // reach_target = true;//listener.exitScreen(this, 0);
                    game_state = state.WIN_GAME;
                }
            }
        } else if(body2.getUserData() instanceof DiverModel){
            if(body1.getUserData() instanceof GoalDoor){
                if(CollisionController.winGame(diver, (GoalDoor) body1.getUserData())
                        && listener!=null) {
                    //reach_target = true;//listener.exitScreen(this, 0);
                    game_state = state.WIN_GAME;
                }
            }
        }
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        // Call CollisionController to handle collisions
//        System.out.println("END CONTACT");
        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();
        collisionController.endContact(body1, body2);

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

       try{
           GObject bd1 = (GObject)body1.getUserData();
           GObject bd2 = (GObject)body2.getUserData();

           if ((diver.getSensorNameLeft().equals(fd2) && diver != bd1) ||
                   (diver.getSensorNameLeft().equals(fd1) && diver != bd2)) {

               if(diver != bd1)
                   diver.removeTouching(diver.getSensorNameLeft(),bd1);
               else
                   diver.removeTouching(diver.getSensorNameLeft(),bd2);

           }
           if ((diver.getSensorNameRight().equals(fd2) && diver != bd1) ||
                   (diver.getSensorNameRight().equals(fd1) && diver != bd2)) {

               if(diver != bd1)
                   diver.removeTouching(diver.getSensorNameRight(),bd1);
               else
                   diver.removeTouching(diver.getSensorNameRight(),bd2);

           }
       } catch (Exception e) {
           e.printStackTrace();
       }

        if (body1.getUserData() instanceof DiverModel) {

            if ( body2.getUserData() instanceof ItemModel) {
                CollisionController.putDown(diver,
                    (ItemModel) body2.getUserData());
                ((ItemModel) body2.getUserData()).setTouched(false);
            }
//            else if(body2.getUserData() instanceof DeadBodyModel){
//                System.out.println("end contact with body");
//                ((DiverModel) body1.getUserData()).setBodyContact(false);
//            }
        } else if (body2.getUserData() instanceof DiverModel) {
            if (body1.getUserData() instanceof ItemModel) {
                CollisionController.putDown(diver,
                    (ItemModel) body1.getUserData());
                ((ItemModel) body1.getUserData()).setTouched(false);
            }
//            else if(body2.getUserData() instanceof DeadBodyModel){
//                System.out.println("end contact with body");
//                ((DiverModel) body2.getUserData()).setBodyContact(true);
//            }
        }
    }
    /**
     * Handles any modifications necessary before collision resolution
     *
     * This method is called just before Box2D resolves a collision.
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}

    public Rectangle getWorldBounds() {
        return bounds;
    }
}
