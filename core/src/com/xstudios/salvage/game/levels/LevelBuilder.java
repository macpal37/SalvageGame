package com.xstudios.salvage.game.levels;


import box2dLight.RayHandler;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameController;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.game.models.*;

import com.xstudios.salvage.game.models.TreasureModel.TreasureType;
import com.xstudios.salvage.util.FilmStrip;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class LevelBuilder {
    private JsonReader jsonReader;
    private AssetDirectory directory;


    // Assets
    JsonValue constants;
    protected Texture tilesheet;
    protected Texture woodenWall;
    protected Texture woodenChair1;
    protected Texture woodenChair2;
    protected Texture woodenTable;
    protected Texture monsterTenctacle;


//    /**
//     * The texture for Hazard
//     */
//    protected TextureRegion hazardTexture;

    protected Texture[] kitchenSet;

    /**
     * The texture for diver
     */
    protected TextureRegion diverTexture;
    /**
     * The texture for item
     */
    protected TextureRegion keyTexture;
    /**
     * Ocean Background Texture
     */
    protected TextureRegion background;
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
    protected Texture swimmingAnimationWBody;

    protected Texture dustAnimation;
    protected Texture plantAnimation;
    protected Texture plant2Animation;
    protected Texture keyAnimation;
    // Models to be updated
    protected TextureRegion sprite;
    protected TextureRegion hazardTexture;
    protected TextureRegion crateTexture;
    protected TextureRegion barrelTexture;
    protected TextureRegion wallBackTexture;
    protected TextureRegion treatureChestOverlay;
    // Models to be updated
    protected DiverModel diver;

    protected ItemModel key;
    //    protected ItemModel dead_body;
    protected DeadBodyModel dead_body;
    private FilmStrip treasureOpenAnimation;
    private FilmStrip treasureKeyAnimation;
    private FilmStrip treasureMonsterAnimation;

    private FilmStrip monsterAttackAnimation;

    private FilmStrip monsterAttack2Animation;
    private FilmStrip monsterAttack3Animation;
    private FilmStrip monsterWiggleAnimation;

    private FilmStrip fishAnimation;

    private FilmStrip monsterIdleAnimation;
    private FilmStrip doorAnimation;

    private FilmStrip exitDoorAnimation;

    private ArrayList<Wall> invisibleWalls = new ArrayList<>();

    public void turnOffInvisibleWalls() {
        for (Wall w : invisibleWalls)
            w.setActive(false);

    }

    /**
     * A hashmap used for assigning contents to treasure chests. may be destroyed once level is created
     * Maps door id to a list of treasure chests that may contain a key corresponding to the door id
     */
    HashMap<Integer, ArrayList<TreasureModel>> chests;


    public LevelBuilder() {
        this.directory = directory;
        jsonReader = new JsonReader();
//        level = new LevelModel();
        chests = new HashMap<Integer, ArrayList<TreasureModel>>();
    }

    public void setDirectory(AssetDirectory directory) {
        this.directory = directory;
    }


    public void gatherAssets(AssetDirectory directory) {
        kitchenSet = new Texture[5];
        for (int i = 1; i <= kitchenSet.length; i++) {
            kitchenSet[i - 1] = directory.getEntry("models:kitchen" + i, Texture.class);
        }

        tilesheet = directory.getEntry("levels:tilesets:old_ship_tileset", Texture.class);
        woodenWall = directory.getEntry("models:wooden_wall", Texture.class);

        woodenChair1 = directory.getEntry("models:wooden_chair1", Texture.class);
        woodenChair2 = directory.getEntry("models:wooden_chair2", Texture.class);
        woodenTable = directory.getEntry("models:wooden_table", Texture.class);


        constants = directory.getEntry("models:constants", JsonValue.class);

        diverTexture = new TextureRegion(directory.getEntry("models:diver", Texture.class));
        swimmingAnimation = directory.getEntry("models:diver_swimming", Texture.class);
        swimmingAnimationWBody = directory.getEntry("models:diver_swimming_w_body", Texture.class);
        dustAnimation = directory.getEntry("models:dust", Texture.class);
        plantAnimation = directory.getEntry("models:plant", Texture.class);
        plant2Animation = directory.getEntry("models:plant2", Texture.class);
        keyAnimation = directory.getEntry("models:key_animation", Texture.class);
        //Treasure Chest Animations
        treasureKeyAnimation = new FilmStrip(directory.getEntry("models:treasure_chest_w_key", Texture.class), 2, 21, 42);
        treasureOpenAnimation = new FilmStrip(directory.getEntry("models:treasure_chest", Texture.class), 1, 14, 14);
        treasureMonsterAnimation = new FilmStrip(directory.getEntry("models:treasure_chest_w_monster", Texture.class), 1, 36, 36);
        treatureChestOverlay = new TextureRegion(directory.getEntry("models:treasure_chest_overlay", Texture.class));
        exitDoorAnimation = new FilmStrip(directory.getEntry("models:exit_door", Texture.class), 1, 25, 25);

        monsterTenctacle = directory.getEntry("models:monster1", Texture.class);

        monsterAttackAnimation = new FilmStrip(directory.getEntry("models:monster_attack", Texture.class), 5, 6, 30);
        monsterIdleAnimation = new FilmStrip(directory.getEntry("models:monster_idle", Texture.class), 5, 6, 30);
        monsterAttack2Animation = new FilmStrip(directory.getEntry("models:monster_attack2", Texture.class), 6, 5, 30);
        monsterAttack3Animation = new FilmStrip(directory.getEntry("models:monster_attack3", Texture.class), 5, 6, 30);
        monsterWiggleAnimation = new FilmStrip(directory.getEntry("models:monster_wiggle", Texture.class), 6, 3, 18);

        doorAnimation = new FilmStrip(directory.getEntry("models:door_animation", Texture.class), 1, 12, 12);

        fishAnimation = new FilmStrip(directory.getEntry("models:fish_animation", Texture.class), 4, 4, 16);

        background = new TextureRegion(directory.getEntry("background:ocean", Texture.class));
        keyTexture = new TextureRegion(directory.getEntry("models:key", Texture.class));
        pingTexture = new TextureRegion(directory.getEntry("models:ping", Texture.class));
        sprite = new TextureRegion(directory.getEntry("hazard", Texture.class));
        hazardTexture = new TextureRegion(directory.getEntry("models:hazard", Texture.class));
        doorOpenTexture = new TextureRegion(directory.getEntry("models:door_open", Texture.class));
        doorCloseTexture = new TextureRegion(directory.getEntry("models:door_closed", Texture.class));

        crateTexture = new TextureRegion(directory.getEntry("models:crate", Texture.class));
        barrelTexture = new TextureRegion(directory.getEntry("models:barrel", Texture.class));

        deadBodyTexture = new TextureRegion(directory.getEntry("models:dead_body", Texture.class));


    }

    enum TileType {
        Empty, Wall, Diver, Obstacle, Item, Door, DeadBody, Block, Goal, Hazard, Decor, Monster, Treasure, Tentacle, Text
    }

    class Tile {

        public float x, y, width, height;
        public float spawnX, spawnY;
        public float rotation;
        public float[] vertices;
        public TileType tileType;
        public int id = 0;

        public Tile() {
            vertices = new float[0];
            tileType = TileType.Empty;
        }

        public Tile(float x, float y, float width, float height, TileType t) {
            this.vertices = new float[0];
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            tileType = t;

        }

        public Tile(float x, float y, float width, float height, float[] vertices, TileType t) {
            this.vertices = vertices;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            tileType = t;

        }


    }

    float round(float num) {
        float result = Math.abs(num);
        boolean isNegative = num < 0;
        if (result > 40 && result < 60) {
            result = 50;
        } else if (result < 10) {
            result = 0;
        } else if (result > 90 && result < 110) {
            result = 100;
        }

        return (isNegative) ? -result : result;
    }


    //    public float div = 25f;
    public float div = 25f;

    public Tentacle createTentacle(float agg_level, float tentacleScale, Wall w, Tentacle.TentacleType type, int lifespan) {
        return createTentacle(agg_level, tentacleScale, w, type, lifespan, w.getTentacleRotation() / 180 * (float) Math.PI);
    }

    public Tentacle createTentacle(float agg_level, float tentacleScale, Wall w, Tentacle.TentacleType type, int lifespan, float angle) {

        float tScale = 2f / 3;
        if (w.canSpawnTentacle()) {

            JsonValue tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_tile.json"));
            ;
            float width = 0, height = 0;
            Tentacle t = null;
            switch (type) {
                case OldAttack:
                    t = new Tentacle(w, agg_level);
                    t.setFilmStrip(new FilmStrip(monsterTenctacle, 1, 30, 30));
                    tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_tile.json"));
                    break;

                case NewAttack:
                    t = new Tentacle(w, agg_level);
                    switch (rand.nextInt(3)) {
                        case 2:
                            t.setFilmStrip(monsterAttack3Animation.copy());
                            tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_attack3.json"));
                            break;
                        case 1:
                            t.setFilmStrip(monsterAttack2Animation.copy());
                            t.setTentacleSprite2(monsterWiggleAnimation.copy());
                            tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_attack2.json"));
                            break;
                        case 0:
                            t.setFilmStrip(monsterAttackAnimation.copy());
                            tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_attack.json"));
                            break;
                    }

                    break;
                case KILL:
                case NewAttack2:
                    t = new Tentacle(w, agg_level);
                    t.setFilmStrip(monsterAttack3Animation.copy());
                    tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_attack3.json"));
                    break;
                case Idle:

                    t = new Tentacle(w, agg_level);
                    t.setTentacleType(type);
                    t.setFilmStrip(monsterIdleAnimation.copy());
                    tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_idle.json"));
                    width = tileset.getFloat("imagewidth");
                    height = tileset.getFloat("imageheight");
                    t.setPosition(t.getX() + 25 / div * (float) Math.cos(t.getAngle()),
                            t.getY() - height / div / 8 * (float) Math.cos(t.getAngle()) + 25 / div * (float) Math.sin(t.getAngle()));
                    break;
            }
            t.setAngle(angle);
            t.setScale(tentacleScale, tentacleScale);
            HazardModel[] boxes = new HazardModel[4];
            int tCount = 0;
            float tileHieght = tileset.getFloat("tileheight");
            float originAngle = 0;
            for (JsonValue tileJson : tileset.get("tiles")) {

                float x = 0;
                float y = 0;

                ArrayList<Float> verticies = new ArrayList<>();
                for (JsonValue o : tileJson.get("objectgroup").get("objects")) {
                    if (o.getString("name").equals("Origin")) {
                        x = round(o.getFloat("x"));
                        y = round(o.getFloat("y"));
                        originAngle = o.getFloat("rotation") / 180 * (float) Math.PI;
                        t.setAngle(t.getAngle() + originAngle);
                        t.setPivot((-x * t.getScale().x) * (float) Math.cos(t.getAngle())
                                        + (tileHieght - y) * t.getScale().y * (float) Math.sin(t.getAngle())
                                , (-(tileHieght - y) * t.getScale().y) * (float) Math.cos(t.getAngle()) +
                                        (-x * t.getScale().x) * (float) Math.sin(t.getAngle())
                        );
                    } else {
                        x = round(o.getFloat("x")) / div - width / div + width / div / 6;
                        y = round(o.getFloat("y")) / div + height / div;
                        verticies.clear();
                        if (o.get("polygon") != null) {


                            for (JsonValue point : o.get("polygon")) {
                                float vx = (round(point.getFloat("x")) / div) + x;

                                float vy = -((round(point.getFloat("y")) / div) + y

                                );
                                verticies.add(vx * (tScale * t.getScale().x));
                                verticies.add(vy * (tScale * t.getScale().y));
                            }
                        }
                        float[] verts = new float[verticies.size()];
                        int index = 0;
                        for (int i = 0; i < verticies.size(); i++)
                            verts[index++] = verticies.get(i);
                        float dx = -(50 / div * (float) Math.sin(t.getAngle()))
                                - (50 / div * (float) Math.cos(t.getAngle()));
                        float dy = (50 / div * (float) Math.cos(t.getAngle()))
                                - (50 / div * (float) Math.sin(t.getAngle()));
                        HazardModel hazard = new HazardModel(verts,
                                w.getTentacleSpawnPosition().x + (dx * (t.getScale().x))
                                , w.getTentacleSpawnPosition().y + (dy * (t.getScale().y))
                        );

                        hazard.setAngle(t.getAngle());
                        hazard.setOxygenDrain(-0.1f);
                        hazard.setStunDuration(60);
                        hazard.setBodyType(BodyDef.BodyType.StaticBody);
                        hazard.setDensity(0);
                        hazard.setFriction(0.4f);
                        hazard.setRestitution(0.1f);
                        hazard.setName("tentacle" + tCount);
                        hazard.setDrawScale(drawScale);
                        hazard.setActive(false);
                        boxes[tCount] = hazard;
                        tCount++;
                    }
                }
            }
            t.initShape(boxes);
            t.setBodyType(BodyDef.BodyType.StaticBody);
            t.setDensity(0);
            t.setFriction(0.4f);
            t.setRestitution(0.1f);
            t.setDrawScale(drawScale);
            t.setStartGrowing(true);
            t.setMaxLifeSpan(lifespan);
            t.setName("tentacle");
            return t;
        } else
            return null;
    }

    Random rand = new Random();

    private Tile[] createTiles(JsonValue tileset, float div, float tileSize) {
        Tile[] tiles = new Tile[tileset.getInt("tilecount")];

        ArrayList<Float> verticies = new ArrayList<>();
        int tt = 0;
        for (JsonValue tileJson : tileset.get("tiles")) {
            int id = tileJson.getInt("id");
            TileType tileType = TileType.Empty;
            String modelType = "Empty";
            int tileId = 0;
            if (tileJson.get("properties") != null) {
                for (JsonValue p : tileJson.get("properties")) {
                    if (p.getString("name").equals("model_type")) {
                        tileType = TileType.valueOf(p.getString("value"));
                    }
                    if (p.getString("name").equals("id")) {
                        tileId = p.getInt("value");
                    }
                }
            }
            if (tileJson.get("objectgroup") != null) {

                if (tileType == TileType.Treasure)
                    System.out.println("YEkjasfkjsadfhbaskdjfbh");
                float x = 0;
                float y = 0;
                float spawnX = -1, spawnY = -1;
                float rotation = 0;

                for (JsonValue o : tileJson.get("objectgroup").get("objects")) {
                    if (tileType == TileType.Treasure)
                        System.out.println("HMMM");
                    if (o.getString("name").equals("SpawnLocation")) {
                        rotation = o.getFloat("rotation");
                        spawnX = o.getFloat("x") / div;
                        spawnY = (100 - o.getFloat("y")) / div;
                    } else {
                        x = round(o.getFloat("x")) / div;
                        y = round(o.getFloat("y")) / div;
                        verticies.clear();
                        if (o.get("polygon") != null) {

                            for (JsonValue point : o.get("polygon")) {
                                float vx = (round(point.getFloat("x")) / div) + x;
                                float vy = tileSize / div - ((round(point.getFloat("y")) / div) + y);
                                verticies.add(vx);
                                verticies.add(vy);
                            }
                        }
                    }
                }

                float[] verts = new float[verticies.size()];
                int index = 0;
                for (int i = 0; i < verticies.size(); i++)
                    verts[index++] = verticies.get(i);
                tiles[tt] = (new Tile(x, y, tileSize / div,
                        tileSize / div, verts, tileType
                ));
                tiles[tt].id = tileId;
                tiles[tt].spawnX = spawnX;
                tiles[tt].spawnY = spawnY;
                tiles[tt].rotation = rotation;


            } else {
                tiles[tt] = tiles[tt] = (new Tile(0, 0, tileSize / div,
                        tileSize / div, tileType
                ));
                tiles[tt].id = tileId;
            }
            tt++;
        }
        return tiles;
    }


    private float[] createVerticies(Tile tile, float x, float y, float xs, float ys) {
        int index = 0;
        float[] newVerts = new float[tile.vertices.length];
        for (Float f : tile.vertices)
            newVerts[index++] = (index % 2 == 0) ? (f * ys + y) : (f * xs + x);
        return newVerts;
    }

    public Vector2 drawScale;

    /**
     * Create level from a json. Objects created will be drawn at scale drawScale, and lights will be created using rayHandler
     */
    public void createLevel(String levelFileName, LevelModel level, Vector2 drawScale, Vector2 drawScaleSymbol, RayHandler rayHandler) {
        this.drawScale = drawScale;
        System.out.println("LEVEL SCALE: " + drawScale.toString());
        ArrayList<GObject> gameObjects = new ArrayList<GObject>();

        JsonValue map = jsonReader.parse(Gdx.files.internal("levels/" + levelFileName + ".json"));

        String tileSetFileName = map.get("tilesets").get(0).getString("source");


        JsonValue tileset = jsonReader.parse(Gdx.files.internal("levels/" + tileSetFileName));
        JsonValue constants = directory.getEntry("models:constants", JsonValue.class);
        int width = map.getInt("width");
        int height = map.getInt("height");
        level.setMapBounds(new Rectangle(0, 0, (width * 100) / 32f, (height * 100) / 32f));

        int tileSize = map.getInt("tileheight");
        float div = 25f;
        int start = 1;


        Tile[] tiles = createTiles(tileset, div, tileSize);


        int ii = 0, jj = height - 1;
        for (JsonValue layer : map.get("layers")) {
            /*===============================================
             * ================* Tile Layers *================
             * ===============================================*/
            if (layer.get("data") != null) {
                for (int n = 0; n < width * height; n++) {
                    int id = Integer.parseInt(layer.get("data").get(n).toString());

                    id = (id == 0) ? 51 : id - start;

                    Tile tile = tiles[id];
                    float sy = (tileSize / div) * jj;
                    float sx = (tileSize / div) * ii;
                    switch (tile.tileType) {
                        case Wall:
                            Wall wall = new Wall(createVerticies(tile, 0, 0, 1, 1), sx, sy);
                            if (tile.id != 10) {
                                wall.setTentacleSpawnPosition(tile.spawnX, tile.spawnY);
                                wall.setTentacleRotation(tile.rotation);
                            }
                            wall.setCanAlertMonster(true);
                            wall.setID(tile.id);

                            gameObjects.add(wall);
                            break;
                        case Empty:


                            break;
                        default:
                            break;
                    }
                    DecorModel dust = new DecorModel(sx, sy);
                    dust.setFilmStrip(new FilmStrip(dustAnimation, 1, 8, 8));
                    dust.setName("dust");
                    dust.setBodyType(BodyDef.BodyType.StaticBody);
                    dust.setSensor(true);
                    dust.setDrawScale(drawScale);
                    gameObjects.add(dust);
                    ii++;
                    if (ii == width) {
                        ii = 0;
                        jj--;
                    }
                }
                ii = 0;
                jj = height - 1;
            }

            /*===============================================
             * ================* Object Layers *================
             * ===============================================*/
            else {
                for (JsonValue obj : layer.get("objects")) {
                    Tile tile = tiles[obj.getInt("gid") - 1];

                    float sx = obj.getFloat("x") / div;
                    float sy = (height * tileSize - obj.getFloat("y")) / div;

                    float objectWidth = obj.getFloat("width");
                    float objectHeight = obj.getFloat("height");
                    float rotation = obj.getFloat("rotation");
                    if (rotation != 0) {
                        rotation = ((360 - rotation) / 180f) * (float) Math.PI;
                    }

                    float widthScale = (objectWidth) / tileSize;
                    float heightScale = (objectHeight) / tileSize;
                    switch (tile.tileType) {
                        case Wall:
                            Wall wall = new Wall(createVerticies(tile, 0, 0, 1, 1), sx, sy);
                            break;
                        case Diver:
                            DiverModel d = new DiverModel(sx, sy, constants.get("diver"));
                            if (obj.get("properties") != null)
                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("starting_oxygen"))
                                        d.setMaxOxygen(prop.getInt("value"));
                                }
                            d.setAngle(rotation);
                            gameObjects.add(d);
                            break;
                        case DeadBody:
                            DeadBodyModel db = new DeadBodyModel(sx + tileSize / (2 * div), sy + tileSize / (2 * div), constants.get("dead_body"));
                            if (obj.get("properties") != null)
                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("oxygen_rewarded"))
                                        db.setOxygenRewarded(prop.getFloat("value"));
                                }
                            gameObjects.add(db);


                            break;
                        case Item:
                            ItemModel item = new ItemModel(sx + tileSize / (2 * div), sy + tileSize / (2 * div), constants.get("key"), ItemModel.ItemType.KEY);
                            if (obj.get("properties") != null)

                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("id"))
                                        item.setID(prop.getInt("value"));
                                }
                            item.setFilmStrip(new FilmStrip(keyAnimation, 1, 6, 6));

                            gameObjects.add(item);
                            item.setAngle(rotation);
                            break;
                        case Door:
                            Door door = new Door(createVerticies(tile, sx, sy, widthScale, heightScale), 0, 0);
                            door.setDoorScale((40f / div) * (widthScale / 2), (40f / div) * (heightScale / 4f));
                            if (obj.get("properties") != null)
                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("id"))
                                        door.setID(prop.getInt("value"));
                                }
                            else
                                door.setID(0);
                            door.setAngle(rotation);
                            door.setFilmStrip(doorAnimation.copy());
                            gameObjects.add(door);
                            break;
                        case Obstacle:
                            ObstacleModel obstacle = new ObstacleModel(createVerticies(tile, 0, 0,
                                    widthScale / 2, heightScale / 2), sx + objectWidth / div / 2, sy + objectHeight / div / 2);
                            switch (tile.id) {
                                case 0:
                                    obstacle.setTexture(barrelTexture);
                                    obstacle.setScale((40f / div) * (widthScale / 4), (40f / div) * (heightScale / 4f));
                                    break;
                                case 1:
                                    obstacle.setTexture(crateTexture);
                                    obstacle.setScale((40f / div) * (widthScale / 4), (40f / div) * (heightScale / 4f));
                                    break;
                                default:
                                    System.out.println("Unknown Object?");
                            }

                            obstacle.setAngle(rand.nextFloat() * (float) Math.PI * 2f);
                            obstacle.setCanAlertMonster(true);
                            gameObjects.add(obstacle);
                            break;

                        case Goal:
                            GoalDoor gd = new GoalDoor(sx, sy + objectHeight / (div * 2) - 5 / div, objectWidth / div, objectHeight / div);


                            gd.setDoorScale((40f / div) * (widthScale / 2), (40f / div) * (heightScale / 4f));
                            gd.setFilmStrip(doorAnimation.copy());
                            gameObjects.add(gd);
                            break;
                        case Block:
                            Wall block = new Wall(createVerticies(tile, 0, 0, widthScale, heightScale), sx, sy);
                            block.setInvisible(true);
                            block.setAngle(rotation);
                            invisibleWalls.add(block);
                            gameObjects.add(block);
                            break;
                        case Hazard:
                            HazardModel hazard = new HazardModel(createVerticies(tile, 0, 0, widthScale, heightScale), sx, sy);
                            gameObjects.add(hazard);
                            hazard.setCanAlertMonster(true);
                            hazard.setAngle(rotation);
                            break;
                        case Decor:
                            DecorModel decor = new DecorModel(sx, sy);
                            switch (tile.id) {
                                case 0:

                                    if (rand.nextInt(2) == 1)
                                        decor.setFilmStrip(new FilmStrip(plantAnimation, 1, 6, 6));
                                    else
                                        decor.setFilmStrip(new FilmStrip(plant2Animation, 1, 6, 6));
                                    break;
                                case 1:
                                    decor.setFilmStrip(new FilmStrip(woodenChair1, 1, 1, 1));
                                    decor.setScale(1 / 1.5f, 1 / 1.5f);
                                    break;
                                case 2:
                                    decor.setFilmStrip(new FilmStrip(woodenChair2, 1, 1, 1));
                                    decor.setScale(1 / 1.5f, 1 / 1.5f);
                                    break;
                                case 3:
                                    decor.setFilmStrip(new FilmStrip(woodenTable, 1, 1, 1));
                                    decor.setScale(1 / 3f, 1 / 3f);
                                    break;
                                case 4:

                                    decor.setFilmStrip(fishAnimation.copy());
                                    decor.setAnimSleep(rand.nextFloat() * 50 + 50);
                                    break;
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                    decor.setFilmStrip(new FilmStrip(kitchenSet[tile.id - 11], 1, 1, 1));
                                    decor.setScale(1 / 3f, 1 / 3f);
                                    break;
                                default:


                            }
                            decor.setScale(decor.getScale().x * objectWidth / tileSize, decor.getScale().y * objectHeight / tileSize);

                            decor.setAngle(rotation);
                            decor.setBodyType(BodyDef.BodyType.StaticBody);
                            decor.setSensor(true);
                            decor.setDrawScale(drawScale);
                            gameObjects.add(decor);
                            break;
                        case Monster:
                            Monster monster = new Monster(sx, sy, true);
                            if (obj.get("properties") != null)
                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("aggro_rate")) {
                                        float aggro = prop.getFloat("value") * 10f;
                                        System.out.println("");
                                        monster.setAggravationRate(aggro);
                                    } else if (prop.getString("name").equals("aggro_threshold"))
                                        monster.setAggroLevel(prop.getInt("value"));
                                    else if (prop.getString("name").equals("kill_strikes"))
                                        monster.setAggroStrikes(prop.getInt("value"));
                                    else if (prop.getString("name").equals("vision_radius"))
                                        monster.setVisionRadius(prop.getInt("value"));
                                    else if (prop.getString("name").equals("random_attack_chance"))
                                        monster.setRandomAttackChance(prop.getFloat("value"));


                                }
                            gameObjects.add(monster);
                            break;
                        case Treasure:
                            float dif = tileSize / 2f;

                            TreasureModel treasureModel = new TreasureModel(createVerticies(tile, -tileSize / div / 4, -tileSize / div / 4,
                                    widthScale / 2, heightScale), sx, sy, tileSize / 2f, tileSize / 2f, div);

                            if (obj.get("properties") != null)
                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("door_id"))
                                        treasureModel.setID(prop.getInt("value"));

                                    treasureModel.mayContainFlare(false);
                                }
                            else {
                                treasureModel.setID(0);

                                treasureModel.mayContainFlare(false);
                            }
                            treasureModel.setAngle(rotation);
                            treasureModel.setIdeSuspenseSprite(treasureOpenAnimation.copy(), treasureMonsterAnimation.copy());

                            treasureModel.setScale(1 / 2f, 1 / 2f);
                            treasureModel.initLight(rayHandler);
                            treasureModel.setTentacleRotation(180);
                            treasureModel.setTentacleSpawnPosition(0, -10f / div);
                            gameObjects.add(treasureModel);
                            // add treasuremodel to a hashmap mapping ids to lists of chests
                            chests.computeIfAbsent(treasureModel.getID(),
                                    k -> new ArrayList<TreasureModel>());
                            chests.get(treasureModel.getID()).add(treasureModel);
                            break;
                        case Text:
                            TextModel textModel = new TextModel(sx, sy);
                            if (obj.get("properties") != null)
                                for (JsonValue prop : obj.get("properties")) {
                                    if (prop.getString("name").equals("text"))
                                        textModel.setText(prop.getString("value"));
                                }
                            gameObjects.add(textModel);
                            break;

                    }


                }

            }
        }

        // we have a hashmap mapping ids to lists of chests that may contain a key matching that id
        // Iterate over the chest groups
        for (Integer id : chests.keySet()) {
            // Get the list of chests in one chest group
            ArrayList<TreasureModel> chest_lst = chests.get(id);
            // Shuffle the array to randomize which one gets the key
            Collections.shuffle(chest_lst);
            // Within a group, there can be only one key, so arbitrarily put a key in the first one
            TreasureModel key_chest = chest_lst.get(0);
            key_chest.setTreasureType(TreasureType.Key, treasureKeyAnimation.copy());
            ItemModel key = new ItemModel(key_chest.getX(), key_chest.getY() + 10f,
                    constants.get("key"), ItemModel.ItemType.KEY);

            key.setFilmStrip(new FilmStrip(keyAnimation, 1, 6, 6));
//            key.setAngle(rotation);
            key.setID(key_chest.getID());
            // this key will not be active/visible?
            key.setKeyActive(false);
            key.setInChest(true);
            key_chest.setKeyReward(key);
            gameObjects.add(key);

            for (int i = 1; i < chest_lst.size(); i++) {
                TreasureModel chest = chest_lst.get(i);
                // if the chest can contain a flare, choose to put a flare in it with probability 10%
                // otherwise, put a monster in it
                if (chest.isMayContainFlare()) {
                    int roll = (int) (Math.random() * 0);



                        chest.setTreasureType(TreasureType.Monster, treasureMonsterAnimation.copy());
                } else {
                    chest.setTreasureType(TreasureType.Monster, treasureMonsterAnimation.copy());
                }
            }
        }

        // now we parse gameObjects into the level model

        int wallCounter = 0;
        int keyCounter = 0;
        int doorCounter = 0;
        int goalDoorCounter = 0;
        int hazardCounter = 0;
        for (GObject go : gameObjects) {
            go.setWorldDrawScale(drawScale.x / 40f, drawScale.y / 40f);
            if (go instanceof HazardModel) {
                HazardModel hazard = (HazardModel) go;
                hazard.setOxygenDrain(-0.1f);
                hazard.setStunDuration(60);
                hazard.setBodyType(BodyDef.BodyType.StaticBody);
                hazard.setDensity(0);
                hazard.setFriction(0.4f);
                hazard.setRestitution(0.1f);
                hazard.setTexture(hazardTexture);
                hazard.setDrawScale(drawScale);
                hazard.setName("hazard" + hazardCounter++);
                hazard.setActive(true);
                level.addObject(hazard);

            } else if (go instanceof TreasureModel) {
                TreasureModel treasureModel = (TreasureModel) go;
                treasureModel.setBodyType(BodyDef.BodyType.DynamicBody);
                treasureModel.setSensor(false);
                treasureModel.setFixedRotation(false);
                treasureModel.setDrawScale(drawScale);
                treasureModel.setDensity(1);
                treasureModel.setMass(10f);
                treasureModel.setFriction(0.4f);
                treasureModel.setRestitution(0.1f);
                treasureModel.setTexture(treatureChestOverlay);
                treasureModel.setName("treasure");
                level.addObject(treasureModel);
            } else if (go instanceof Door) {
                Door door = (Door) go;
                door.setBodyType(BodyDef.BodyType.StaticBody);
                door.setTexture(doorTexture);
                door.addTextures(doorCloseTexture, doorOpenTexture);
                door.setDrawScale(drawScale);
                door.setName("door" + doorCounter++);
                door.setActive(true);
                System.out.println("HELLO?");
                level.addObject(door);
            } else if (go instanceof ObstacleModel) {
                ObstacleModel obstacle = (ObstacleModel) go;
                // TODO: One day we will be able to kick off of obstacles
//                obstacle.setWall(true);
                obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
                obstacle.setSensor(false);
                obstacle.setFixedRotation(false);
                obstacle.setDrawScale(drawScale);
                obstacle.setDensity(1);
                obstacle.setMass(10f);
                obstacle.setFriction(0.4f);
                obstacle.setRestitution(0.1f);
                obstacle.setName("obstacle");
                level.addObject(obstacle);
            } else if (go instanceof Wall) {
                Wall obj = (Wall) go;
                obj.setWall(true);
                obj.setBodyType(BodyDef.BodyType.StaticBody);
                obj.setDensity(0);
                obj.setFriction(0.4f);
                obj.setRestitution(0.1f);
                obj.setDrawScale(drawScale);
                obj.setFilmStrip(new FilmStrip(woodenWall, 5, 3, 15));
                obj.setName("wall " + wallCounter++);
                level.addObject(obj);

            } else if (go instanceof DiverModel) {
                diver = (DiverModel) go;
                diver.setStunned(false);
                diver.setTexture(diverTexture);
                diver.addFilmStrip(new FilmStrip(swimmingAnimation, 13, 6, 78));
                diver.addFilmStrip(new FilmStrip(swimmingAnimationWBody, 13, 6, 78));
                diver.setPingTexture(pingTexture);
                diver.setDrawScale(drawScale);
                diver.setName("diver");
                level.addObject(diver);
            } else if (go instanceof DeadBodyModel) {
                DeadBodyModel dead_body = (DeadBodyModel) go;
                dead_body.setTexture(deadBodyTexture);
                dead_body.setDrawScale(drawScale);
                dead_body.setDrawSymbolScale(drawScaleSymbol);
                dead_body.setName("dead_body");
                dead_body.setGravityScale(0f);
                dead_body.setSensor(true);
                this.dead_body = dead_body;
                level.addObject(dead_body);
            } else if (go instanceof ItemModel) {
                ItemModel key = (ItemModel) go;
                key.setTexture(keyTexture);
                key.setBodyType(BodyDef.BodyType.StaticBody);
                key.setDrawScale(drawScale);
                key.setDrawSymbolScale(drawScaleSymbol);
                key.setName("key" + keyCounter++);
                key.setGravityScale(0f);
                key.setSensor(true);
                key.initLight(rayHandler);
                level.addObject(key);
            } else if (go instanceof GoalDoor) {
                JsonValue goal = constants.get("goal");
                GoalDoor goal_door = (GoalDoor) go;
                goal_door.setBodyType(BodyDef.BodyType.StaticBody);
                goal_door.setDensity(goal.getFloat("density", 0));
                goal_door.setFriction(goal.getFloat("friction", 0));
                goal_door.setRestitution(goal.getFloat("restitution", 0));
                goal_door.setID(3);
                goal_door.setTexture(doorOpenTexture);
                goal_door.setSensor(true);
                goal_door.setDrawScale(drawScale);
                goal_door.setFont(GameController.displayFont2);
                goal_door.setFilmStrip(exitDoorAnimation.copy());
                goal_door.setName("goal" + goalDoorCounter++);
                level.addObject(goal_door);

            } else if (go instanceof DecorModel) {
                DecorModel dm = (DecorModel) go;
                level.getAboveObjects().add(dm);
                level.addObject(dm);
            } else if (go instanceof Monster) {
                Monster monster = (Monster) go;
                monster.setAttackTentacleSprite(new FilmStrip(monsterTenctacle, 1, 30, 30));
                monster.setDrawScale(drawScale);
                monster.setName("Monster");
                level.addObject(monster);
            } else if (go instanceof TextModel) {
                TextModel text = (TextModel) go;
                text.setDrawScale(drawScale);
                text.setFont(GameController.displayFont);
                text.setSensor(true);
                level.getAboveObjects().add(text);
                level.addObject(text);
            }
        }

        diver.setDeadBody(dead_body);

        // reset the hashmap for next time
        chests.clear();

    }


}
