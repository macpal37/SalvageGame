package com.xstudios.salvage.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.Door;
import com.xstudios.salvage.game.models.ItemModel;
import com.xstudios.salvage.game.models.ItemType;
import com.xstudios.salvage.game.models.Wall;
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

    JsonValue constants;

    // Models to be updated
    protected TextureRegion wallTexture;
    protected TextureRegion wallBackTexture;

    /** The font for giving messages to the player */
    protected BitmapFont displayFont;


    // Models to be updated
    protected DiverModel diver;

    protected ItemModel key;

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
    protected static final float DEFAULT_GRAVITY = -4.9f;


    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** All the objects in the world. */
    protected PooledList<GameObject> objects  = new PooledList<GameObject>();
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


    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether or not debug mode is active */
    private boolean debug;


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
        debug  = false;
        active = false;

        // TODO: oxygen rate should be a parameter loaded from a json
        passiveOxygenRate = -.01f;
        activeOxygenRate = -.02f;

        System.out.println("BG: "+background);
        collisionController = new CollisionController();
        world.setContactListener(this);
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
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale  = null;
        world  = null;
        canvas = null;
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
        //wallBackTexture = new TextureRegion(directory.getEntry( "background:wooden_bg", Texture.class ));
        displayFont = directory.getEntry("fonts:lightpixel", BitmapFont.class);
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
        Vector2 gravity = new Vector2(world.getGravity() );
        for(GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }

        world = new World(gravity,false);
        world.setContactListener(this);
        resetLevel();
    }

    private void resetLevel() {

        diver = new DiverModel(constants.get("diver"),diverTexture.getRegionWidth(),
            diverTexture.getRegionHeight());

        diver.setTexture(diverTexture);
        diver.setPingTexture(pingTexture);
        diver.setDrawScale(scale);
        diver.setName("diver");

        addObject(diver);

        key = new ItemModel(constants.get("diver"),itemTexture.getRegionWidth(),
                itemTexture.getRegionHeight(), ItemType.KEY, 0);

        key.setTexture(itemTexture);
        key.setDrawScale(scale);
        key.setName("key");
        key.setGravityScale(.01f);

        addObject(key);
        //add a wall

        float[][] wallVerts={
            {1.0f, 3.0f, 6.0f, 3.0f, 6.0f, 2.5f, 1.0f, 2.5f},
            { 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
            {23.0f, 4.0f,31.0f, 4.0f,31.0f, 2.5f,23.0f, 2.5f},
            {26.0f, 5.5f,28.0f, 5.5f,28.0f, 5.0f,26.0f, 5.0f},
            {29.0f, 7.0f,31.0f, 7.0f,31.0f, 6.5f,29.0f, 6.5f},
            {24.0f, 8.5f,27.0f, 8.5f,27.0f, 8.0f,24.0f, 8.0f},
            {29.0f,10.0f,31.0f,10.0f,31.0f, 9.5f,29.0f, 9.5f},
            {23.0f,11.5f,27.0f,11.5f,27.0f,11.0f,23.0f,11.0f},
            {19.0f,12.5f,23.0f,12.5f,23.0f,12.0f,19.0f,12.0f},
            { 1.0f,12.5f, 7.0f,12.5f, 7.0f,12.0f, 1.0f,12.0f}
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

    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        float diverWidth = diverTexture.getRegionWidth();
        float diverHeight = diverTexture.getRegionHeight();

        // add the diver

        diver = new DiverModel(constants.get("diver"), diverWidth, diverHeight);
        diver.setTexture(diverTexture);
        diver.setName("Diver");
        addObject(diver);

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
        if (input.didReset()) {
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
        // apply movement
        InputController input = InputController.getInstance();
        diver.setHorizontalMovement(input.getHorizontal() *diver.getForce());
        diver.setVerticalMovement(input.getVertical() *diver.getForce());

        diver.applyForce();

        // do the ping
        diver.setPing(input.didPing());
        if (input.didPing()){
            diver.setPingDirection(new Vector2(3,3));
        }
        diver.setPickUpOrDrop(input.getOrDropObject());
        diver.setItem();

        // decrease oxygen from movement
        if (Math.abs(input.getHorizontal()) > 0 || Math.abs(input.getVertical()) > 0) {
//            System.out.println("moving");
            diver.changeOxygenLevel(activeOxygenRate);
        } else {
//            System.out.println("passive Oxygen Rate: " + passiveOxygenRate);
            diver.changeOxygenLevel(passiveOxygenRate);
        }

        if (diver.getBody()!=null){
            cameraController.setCameraPosition(diver.getX()*diver.getDrawScale().x,diver.getY()*diver.getDrawScale().y);
        }

//        System.out.println("WORLD GRAVITY: " + world.getGravity());
        cameraController.render();
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

        // draw UI relative to the camera position
        // TODO: the text is shaking!!!!
        canvas.drawText(
            "Oxygen Level: " + (int) diver.getOxygenLevel(),
            displayFont,
            cameraController.getCameraPosition2D().x - canvas.getWidth()/2 + 50,
            cameraController.getCameraPosition2D().y - canvas.getHeight()/2 + 50);

        canvas.end();

            canvas.beginDebug();
            for(GameObject obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();

    }

    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId) {
        return playSound( sound, soundId, 1.0f );
    }

    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     * @param volume	The sound volume
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
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
    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        Door door=null;

        // Call CollisionController to handle collisions
        if(body1.getUserData() instanceof DiverModel || body2.getUserData() instanceof DiverModel){
            if(body1.getUserData() instanceof Door){
                door=(Door)body1.getUserData();
            }
            else if (body2.getUserData() instanceof Door){
                door=(Door)body2.getUserData();
            }
            if(door !=null){
                if (diver.getItem().equals(door.getKey())){
                    door.setAwake(false);

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
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

//        System.out.println("END CONTACT");
        collisionController.endContact(body1, body2);
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
}
