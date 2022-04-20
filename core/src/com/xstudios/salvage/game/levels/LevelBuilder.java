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
    protected Texture dustAnimation;
    protected Texture plantAnimation;

    // Models to be updated
    protected TextureRegion wallTexture;
    protected TextureRegion hazardTexture;
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
        tilesheet = directory.getEntry("levels:tilesets:old_ship_tileset", Texture.class);
        woodenWall = directory.getEntry("models:wooden_wall", Texture.class);

        woodenChair1 = directory.getEntry("models:wooden_chair1", Texture.class);
        woodenChair2 = directory.getEntry("models:wooden_chair2", Texture.class);
        woodenTable = directory.getEntry("models:wooden_table", Texture.class);


        constants = directory.getEntry("models:constants", JsonValue.class);

        diverTexture = new TextureRegion(directory.getEntry("models:diver", Texture.class));
        swimmingAnimation = directory.getEntry("models:diver_swimming", Texture.class);
        dustAnimation = directory.getEntry("models:dust", Texture.class);
        plantAnimation = directory.getEntry("models:plant", Texture.class);
        dustAnimation = directory.getEntry("models:dust", Texture.class);
        plantAnimation = directory.getEntry("models:plant", Texture.class);


        background = new TextureRegion(directory.getEntry("background:ocean", Texture.class));
        keyTexture = new TextureRegion(directory.getEntry("models:key", Texture.class));
        pingTexture = new TextureRegion(directory.getEntry("models:ping", Texture.class));
        wallTexture = new TextureRegion(directory.getEntry("hazard", Texture.class));
        hazardTexture = new TextureRegion(directory.getEntry("hazard", Texture.class));
        doorTexture = new TextureRegion(directory.getEntry("door", Texture.class));
        //wallBackTexture = new TextureRegion(directory.getEntry( "background:wooden_bg", Texture.class ));
        doorOpenTexture = new TextureRegion(directory.getEntry("models:door_open", Texture.class));
        doorCloseTexture = new TextureRegion(directory.getEntry("models:door_closed", Texture.class));

        deadBodyTexture = new TextureRegion(directory.getEntry("models:dead_body", Texture.class));
    }

    enum TileType {
        Empty, Wall, Diver, Obstacle, Item, Door, DeadBody, Block, Goal, Hazard, Decor
    }

    TileType tileTypeFromString(String type) {
        if (type.equals("Wall")) {
            return TileType.Wall;
        } else if (type.equals("Diver")) {
            return TileType.Diver;
        } else if (type.equals("DeadBody")) {
            return TileType.DeadBody;
        } else if (type.equals("Item")) {
            return TileType.Item;
        } else if (type.equals("Door")) {
            return TileType.Door;
        } else if (type.equals("Obstacle")) {
            return TileType.Obstacle;
        } else if (type.equals("Block")) {
            return TileType.Block;
        } else if (type.equals("Goal")) {
            return TileType.Goal;
        } else if (type.equals("Hazard")) {
            return TileType.Hazard;
        } else if (type.equals("Decor")) {
            return TileType.Decor;
        }

        return TileType.Empty;
    }


    class Tile {

        public float x, y, width, height;
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


    private Tile[] createTiles(JsonValue tileset, float div, float tileSize) {
        Tile[] tiles = new Tile[tileset.getInt("tilecount")];

        ArrayList<Float> verticies = new ArrayList<>();
        int tt = 0;
        for (JsonValue tileJson : tileset.get("tiles")) {
            int id = tileJson.getInt("id");
            TileType tileType = TileType.Empty;
            int tileId = 0;
            if (tileJson.get("properties") != null) {
                for (JsonValue p : tileJson.get("properties")) {
                    if (p.getString("name").equals("model_type")) {
                        tileType = tileTypeFromString(p.getString("value"));
                    }
                    if (p.getString("name").equals("id")) {
                        tileId = p.getInt("value");
                    }
                }
            }
            if (tileJson.get("objectgroup") != null) {
                float x = 0;
                float y = 0;
                for (JsonValue o : tileJson.get("objectgroup").get("objects")) {
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

                float[] verts = new float[verticies.size()];
                int index = 0;
                for (int i = 0; i < verticies.size(); i++)
                    verts[index++] = verticies.get(i);
                tiles[tt] = (new Tile(x, y, tileSize / div,
                        tileSize / div, verts, tileType
                ));
                tiles[tt].id = tileId;

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

    /**
     * Create level from a json. Objects created will be drawn at scale drawScale, and lights will be created using rayHandler
     */
    public void createLevel(String levelFileName, LevelModel level, Vector2 drawScale, Vector2 drawScaleSymbol, RayHandler rayHandler) {

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
            if (layer.get("data") != null) {
                for (int n = 0; n < width * height; n++) {
                    int id = Integer.parseInt(layer.get("data").get(n).toString());

                    id = (id == 0) ? 51 : id - start;

                    Tile tile = tiles[id];
                    float sy = (tileSize / div) * jj;
                    float sx = (tileSize / div) * ii;
                    int index = 0;
                    switch (tile.tileType) {
                        case Wall:
                            Wall wall = new Wall(createVerticies(tile, sx, sy, 1, 1), 0, 0);

                            wall.setID(tile.id);

                            gameObjects.add(wall);
                            break;
                        case Diver:

                            gameObjects.add(new DiverModel(sx, sy, constants.get("diver")));
                            break;
                        case DeadBody:

                            gameObjects.add(new DeadBodyModel(sx, sy, constants.get("dead_body")));
                            break;
                        case Item:
                            ItemModel item = new ItemModel(sx, sy, constants.get("key"), ItemType.KEY);
                            gameObjects.add(item);
                            break;
                        case Door:
                            float[] doorVerticies = new float[tile.vertices.length];
                            index = 0;
                            for (Float f : tile.vertices)
                                doorVerticies[index++] = (index % 2 == 0) ? f + sy : f + sx;
                            Door door = new Door(doorVerticies, 0, 0);
                            gameObjects.add(door);
                            break;

                        case Obstacle:
                            break;

                        case Goal:

                            gameObjects.add(new GoalDoor(sx, sy, tileSize / div, tileSize / div));
                            break;
                        case Block:
                            float[] blockVertices = new float[tile.vertices.length];
                            index = 0;
                            for (Float f : tile.vertices)
                                blockVertices[index++] = (index % 2 == 0) ? f + sy : f + sx;
                            Wall block = new Wall(blockVertices, 0, 0);
                            block.setInvisible(true);
                            gameObjects.add(block);
                            break;
                        case Hazard:

                            HazardModel hazard = new HazardModel(createVerticies(tile, sx, sy, 1, 1), 0, 0);
                            gameObjects.add(hazard);

                        case Empty:
                            if (layer.getString("name").equals("walls")) {
                                DecorModel dust = new DecorModel(sx, sy);
                                dust.setFilmStrip(new FilmStrip(dustAnimation, 1, 8, 8));
                                dust.setName("dust");
                                dust.setBodyType(BodyDef.BodyType.StaticBody);
                                dust.setSensor(true);
                                dust.setDrawScale(drawScale);

                            }

                            break;
                    }
                    ii++;
                    if (ii == width) {
                        ii = 0;
                        jj--;

                    }
                }
                ii = 0;
                jj = height - 1;


            } else {
                for (JsonValue obj : layer.get("objects")) {
                    Tile tile = tiles[obj.getInt("gid") - 1];

                    float sx = obj.getFloat("x") / div;
                    float sy = (height * tileSize - obj.getFloat("y")) / div;

                    float objectWidth = obj.getFloat("width");
                    float objectHeight = obj.getFloat("height");

                    float widthScale = (objectWidth) / tileSize;
                    float heightScale = (objectHeight) / tileSize;
                    switch (tile.tileType) {
                        case Wall:

                            gameObjects.add(new Wall(createVerticies(tile, sx, sy, widthScale, heightScale), 0, 0));
                            break;
                        case Diver:

                            gameObjects.add(new DiverModel(sx, sy, constants.get("diver")));
                            break;
                        case DeadBody:

                            gameObjects.add(new DeadBodyModel(sx + tileSize / (2 * div), sy + tileSize / (2 * div), constants.get("dead_body")));
                            break;
                        case Item:
                            ItemModel item = new ItemModel(sx + tileSize / (2 * div), sy + tileSize / (2 * div), constants.get("key"), ItemType.KEY);
                            for (JsonValue prop : obj.get("properties")) {
                                if (prop.getString("name").equals("id"))
                                    item.setID(prop.getInt("value"));
                            }
                            gameObjects.add(item);
//
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
                            gameObjects.add(door);
//
                            break;

                        case Obstacle:
                            break;

                        case Goal:

                            gameObjects.add(new GoalDoor(sx, sy + objectHeight / (div * 2), objectWidth / div, objectHeight / div));
                            break;
                        case Block:
                            Wall block = new Wall(createVerticies(tile, sx, sy, widthScale, heightScale), 0, 0);
                            block.setInvisible(true);
                            gameObjects.add(block);
                            break;
                        case Hazard:
                            HazardModel hazard = new HazardModel(createVerticies(tile, sx, sy, widthScale, heightScale), 0, 0);
                            gameObjects.add(hazard);
                            break;
                        case Decor:
                            DecorModel dust = new DecorModel(sx, sy);
                            switch (tile.id) {
                                case 0:
                                    dust.setFilmStrip(new FilmStrip(plantAnimation, 1, 6, 6));
                                    break;
                                case 1:
                                    dust.setFilmStrip(new FilmStrip(woodenChair1, 1, 1, 1));
                                    dust.setScale(1 / 2f, 1 / 2f);

                                    break;
                                case 2:
                                    dust.setFilmStrip(new FilmStrip(woodenChair2, 1, 1, 1));
                                    dust.setScale(1 / 2f, 1 / 2f);
                                    break;
                                case 3:
                                    dust.setFilmStrip(new FilmStrip(woodenTable, 1, 1, 1));
                                    dust.setScale(1 / 5f, 1 / 5f);
                                    break;
                                default:
                                    System.out.println("Unknown Object?");

                            }

                            dust.setBodyType(BodyDef.BodyType.StaticBody);
                            dust.setSensor(true);
                            dust.setDrawScale(drawScale);
                            gameObjects.add(dust);
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
                level.addObject(hazard);
//                hazard.setUserData(hazard);
                hazard.setActive(true);
            } else if (go instanceof Door) {
                Door door = (Door) go;
                door.setBodyType(BodyDef.BodyType.StaticBody);
                door.setTexture(doorTexture);
                door.addTextures(doorCloseTexture, doorOpenTexture);
                door.setDrawScale(drawScale);
                door.setName("door" + doorCounter++);
                door.setActive(true);
                level.addObject(door);
            } else if (go instanceof Wall) {

                Wall obj = (Wall) go;
                obj.setBodyType(BodyDef.BodyType.StaticBody);
                obj.setDensity(0);
                obj.setFriction(0.4f);
                obj.setRestitution(0.1f);
                obj.setDrawScale(drawScale);
                obj.setFilmStrip(new FilmStrip(woodenWall, 5, 3, 15));
//                obj.setTexture(wallTexture);
                obj.setName("wall " + wallCounter++);
                level.addObject(obj);

            } else if (go instanceof DiverModel) {
                diver = (DiverModel) go;
                diver.setStunned(false);
                diver.setTexture(diverTexture);
                diver.setFilmStrip(new FilmStrip(swimmingAnimation, 2, 12, 24));
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

            } else if (go instanceof Dust) {
                Dust dust = (Dust) go;
                dust.setFilmStrip(new FilmStrip(dustAnimation, 1, 8, 8));
                dust.setName("dust");
                dust.setBodyType(BodyDef.BodyType.StaticBody);
                dust.setSensor(true);
                dust.setDrawScale(drawScale);
                level.addObject(dust);
            } else if (go instanceof Plant) {
                Plant dust = (Plant) go;
                dust.setFilmStrip(new FilmStrip(plantAnimation, 1, 6, 6));
                dust.setName("plant");
                dust.setBodyType(BodyDef.BodyType.StaticBody);
                dust.setSensor(true);
                dust.setDrawScale(drawScale);
                level.addObject(dust);
            } else if (go instanceof DecorModel) {
                DecorModel dm = (DecorModel) go;

                level.addObject(dm);
            }
        }

        diver.setDeadBody(dead_body);

    }


}
