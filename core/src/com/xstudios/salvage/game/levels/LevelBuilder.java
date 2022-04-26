package com.xstudios.salvage.game.levels;


import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.models.*;

import com.xstudios.salvage.util.FilmStrip;
import jdk.internal.misc.OSEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.FileHandler;

public class LevelBuilder {
    private JsonReader jsonReader;
    private AssetDirectory directory;
//    private LevelModel level;

    // Assets
    JsonValue constants;
    protected Texture tilesheet;
    protected Texture woodenWall;
    protected Texture woodenChair1;
    protected Texture woodenChair2;
    protected Texture woodenTable;


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

    // Models to be updated
    protected TextureRegion wallTexture;
    protected TextureRegion hazardTexture;
    protected TextureRegion crateTexture;
    protected TextureRegion barrelTexture;
    protected TextureRegion wallBackTexture;

    // Models to be updated
    protected DiverModel diver;

    protected ItemModel key;
    //    protected ItemModel dead_body;
    protected DeadBodyModel dead_body;

    public LevelBuilder() {
        this.directory = directory;
        jsonReader = new JsonReader();
//        level = new LevelModel();

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


        background = new TextureRegion(directory.getEntry("background:ocean", Texture.class));
        keyTexture = new TextureRegion(directory.getEntry("models:key", Texture.class));
        pingTexture = new TextureRegion(directory.getEntry("models:ping", Texture.class));
        wallTexture = new TextureRegion(directory.getEntry("hazard", Texture.class));
        hazardTexture = new TextureRegion(directory.getEntry("models:hazard", Texture.class));
        doorOpenTexture = new TextureRegion(directory.getEntry("models:door_open", Texture.class));
        doorCloseTexture = new TextureRegion(directory.getEntry("models:door_closed", Texture.class));

        crateTexture = new TextureRegion(directory.getEntry("models:crate", Texture.class));
        barrelTexture = new TextureRegion(directory.getEntry("models:barrel", Texture.class));

        deadBodyTexture = new TextureRegion(directory.getEntry("models:dead_body", Texture.class));
    }

    enum TileType {
        Empty, Wall, Diver, Obstacle, Item, Door, DeadBody, Block, Goal, Hazard, Decor
    }

    class Tile {

        public float x, y, width, height;
        public float spawnX, spawnY;
        public float rotation;
        public float[] vertices;
        public TileType tileType;
        public String modelType;

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


    public float div = 25f;

    public Tentacle createTentcle(Wall w, FilmStrip sprite, Vector2 scale) {

        float tScale = 3f / 2;
        if (w.canSpawnTentacle()) {


            Tentacle t = new Tentacle(w);
            t.setScale(1, 1);
            JsonValue tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/tentacle_tile.json"));
            HazardModel[] boxes = new HazardModel[4];
            int tCount = 0;

            Vector2 dif = new Vector2(0, 0);

            float tileHieght = tileset.getFloat("tileheight");

            for (JsonValue tileJson : tileset.get("tiles")) {
                float x = 0;
                float y = 0;
                ArrayList<Float> verticies = new ArrayList<>();
                for (JsonValue o : tileJson.get("objectgroup").get("objects")) {


                    if (o.getString("name").equals("Origin")) {
                        x = round(o.getFloat("x"));
                        y = round(o.getFloat("y"));
                        t.setPivot((-x * t.getScale().x) * (float) Math.cos(t.getAngle())
                                        + (tileHieght - y) * t.getScale().y * (float) Math.sin(t.getAngle())


                                , (-(tileHieght - y) * t.getScale().y) * (float) Math.cos(t.getAngle()) +

                                        (-x * t.getScale().x) * (float) Math.sin(t.getAngle())
                        );
                        dif.set(x / div, tileHieght / div - (2 * y / div)


                        );

                    } else {
                        x = round(o.getFloat("x")) / div;
                        y = round(o.getFloat("y")) / div;
                        verticies.clear();
                        if (o.get("polygon") != null) {


                            for (JsonValue point : o.get("polygon")) {
                                float vx = (round(point.getFloat("x")) / div) + x


//                                        - dif.y * (float) Math.cos(t.getAngle())
                                        - ((dif.x) * 2 * ((Math.sin(t.getAngle()) >= 0) ? 0 : 1));

                                float vy = -((round(point.getFloat("y")) / div) + y
                                        - ((dif.x) * 2 * ((Math.sin(t.getAngle()) >= 0) ? 0 : 1))
                                        - ((dif.x) * 2 * (float) Math.cos(t.getAngle()))
//                                        - ((dif.x) * 2 * ((Math.cos(t.getAngle()) >= 0) ? 1 : 0))
//                                        + ((dif.x) * 2 * ((Math.cos(t.getAngle()) < 0) ? 1 : 0))
//

//                                        +(float) (tileHieght / div * Math.cos(t.getAngle()))

                                );
                                verticies.add(vx / tScale);
                                verticies.add(vy / tScale);
                            }
                        }
                        float[] verts = new float[verticies.size()];
                        int index = 0;
                        for (int i = 0; i < verticies.size(); i++)
                            verts[index++] = verticies.get(i);
                        HazardModel hazard = new HazardModel(verts, w.getX(), w.getY());
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
            t.setDrawScale(scale);
            t.setFilmStrip(sprite);
            t.setStartGrowing(true);
            t.setMaxLifeSpan(300);
            t.setName("tentacle");
            return t;
        } else
            return null;
    }


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
                float x = 0;
                float y = 0;
                float spawnX = -1, spawnY = -1;
                float rotation = 0;
                for (JsonValue o : tileJson.get("objectgroup").get("objects")) {

                    if (o.getString("name").equals("SpawnLocation")) {
                        rotation = o.getFloat("rotation");
                        spawnX = round(o.getFloat("x")) / div;
                        spawnY = (100 - round(o.getFloat("y"))) / div;
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
        ArrayList<GObject> gameObjects = new ArrayList<GObject>();

        JsonValue map = jsonReader.parse(Gdx.files.internal("levels/" + levelFileName + ".json"));

        String tileSetFileName = map.get("tilesets").get(0).getString("source");


        JsonValue tileset = jsonReader.parse(Gdx.files.internal("levels/" + tileSetFileName));
        JsonValue constants = directory.getEntry("models:constants", JsonValue.class);
        int width = map.getInt("width");
        int height = map.getInt("height");


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
                            wall.setTentacleSpawnPosition(tile.spawnX, tile.spawnY);
                            wall.setTentacleRotation(tile.rotation);
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
//                    gameObjects.add(dust);
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
                            gameObjects.add(d);
                            break;
                        case DeadBody:

                            gameObjects.add(new DeadBodyModel(sx + tileSize / (2 * div), sy + tileSize / (2 * div), constants.get("dead_body")));

                            break;
                        case Item:
                            ItemModel item = new ItemModel(sx + tileSize / (2 * div), sy + tileSize / (2 * div), constants.get("key"), ItemModel.ItemType.KEY);
                            for (JsonValue prop : obj.get("properties")) {
                                if (prop.getString("name").equals("id"))
                                    item.setID(prop.getInt("value"));
                            }
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
                            gameObjects.add(door);
                            break;
                        case Obstacle:
                            ObstacleModel obstacle = new ObstacleModel(createVerticies(tile, 0, 0, widthScale / 2, heightScale / 2), sx, sy);
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
                            obstacle.setAngle(rotation);
                            gameObjects.add(obstacle);
                            break;

                        case Goal:

                            gameObjects.add(new GoalDoor(sx, sy + objectHeight / (div * 2), objectWidth / div, objectHeight / div));
                            break;
                        case Block:
                            Wall block = new Wall(createVerticies(tile, 0, 0, widthScale, heightScale), sx, sy);
                            block.setInvisible(true);
                            block.setAngle(rotation);
                            gameObjects.add(block);
                            break;
                        case Hazard:
                            HazardModel hazard = new HazardModel(createVerticies(tile, 0, 0, widthScale, heightScale), sx, sy);
                            gameObjects.add(hazard);
                            hazard.setAngle(rotation);
                            break;
                        case Decor:
                            DecorModel decor = new DecorModel(sx, sy);
                            switch (tile.id) {
                                case 0:
                                    decor.setFilmStrip(new FilmStrip(plantAnimation, 1, 6, 6));
                                    break;
                                case 1:
                                    decor.setFilmStrip(new FilmStrip(woodenChair1, 1, 1, 1));
                                    decor.setScale(1 / 2f, 1 / 2f);
                                    break;
                                case 2:
                                    decor.setFilmStrip(new FilmStrip(woodenChair2, 1, 1, 1));
                                    decor.setScale(1 / 2f, 1 / 2f);
                                    break;
                                case 3:
                                    decor.setFilmStrip(new FilmStrip(woodenTable, 1, 1, 1));
                                    decor.setScale(1 / 5f, 1 / 5f);
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
                                    System.out.println("Unknown Object?");

                            }

//                            decor.setAngle((float) Math.PI * 1 / 2f);
                            decor.setAngle(rotation);
                            decor.setBodyType(BodyDef.BodyType.StaticBody);
                            decor.setSensor(true);
                            decor.setDrawScale(drawScale);
                            gameObjects.add(decor);
                    }


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
                diver.addFilmStrip(new FilmStrip(swimmingAnimation, 4, 12, 48));
                diver.addFilmStrip(new FilmStrip(swimmingAnimationWBody, 4, 12, 48));
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
                goal_door.setSensor(true);
                goal_door.setDrawScale(drawScale);
                goal_door.setTexture(doorOpenTexture);
                goal_door.setName("goal" + goalDoorCounter++);
                level.addObject(goal_door);

            } else if (go instanceof DecorModel) {
                DecorModel dm = (DecorModel) go;
                level.getAboveObjects().add(dm);
                level.addObject(dm);
            }
        }

        diver.setDeadBody(dead_body);

    }


}
