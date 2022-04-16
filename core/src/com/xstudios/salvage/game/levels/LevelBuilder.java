package com.xstudios.salvage.game.levels;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.FileHandler;

public class LevelBuilder {
    private JsonReader jsonReader;
    private AssetDirectory directory;

    public LevelBuilder() {
        this.directory = directory;
        jsonReader = new JsonReader();

    }

    public void setDirectory(AssetDirectory directory) {
        this.directory = directory;
    }

    enum TileType {
        Empty, Wall, Diver, Obstacle, Item, Door, DeadBody, Block, Goal, Hazard, Plant
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
        } else if (type.equals("Plant")) {
            return TileType.Plant;
        }

        return TileType.Empty;
    }


    class Tile {

        public float x, y, width, height;
        public float[] vertices;
        public TileType tileType;

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

    class TObject extends Tile {
        ArrayList<Integer> propInt = new ArrayList<>();
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

    public int getNighbor(int x, int y, int dx, int dy, int width, int height) {


        return 0;
    }


    private Tile[] createTiles(JsonValue tileset, float div, float tileSize) {
        Tile[] tiles = new Tile[tileset.getInt("tilecount")];

        ArrayList<Float> verticies = new ArrayList<>();
        int tt = 0;
        for (JsonValue tileJson : tileset.get("tiles")) {
            int id = tileJson.getInt("id");
            TileType tileType = TileType.Empty;

            if (tileJson.get("properties") != null) {
                for (JsonValue p : tileJson.get("properties")) {
                    if (p.getString("name").equals("model_type")) {
                        tileType = tileTypeFromString(p.getString("value"));
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
                System.out.println(Arrays.toString(verts));
                tiles[tt] = (new Tile(x, y, tileSize / div,
                        tileSize / div, verts, tileType
                ));
            } else {
                tiles[tt] = tiles[tt] = (new Tile(0, 0, tileSize / div,
                        tileSize / div, tileType
                ));
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

    public ArrayList<GObject> createLevel(String levelFileName) {
        System.out.println("Creating Level");
        ArrayList<GObject> gameObjects = new ArrayList<GObject>();

        JsonValue map = jsonReader.parse(Gdx.files.internal("levels/" + levelFileName + ".json"));

        String tileSetFileName = map.get("tilesets").get(0).getString("source");
        System.out.println("FILENAME: " + tileSetFileName);

        JsonValue tileset = jsonReader.parse(Gdx.files.internal("levels/" + tileSetFileName));
        JsonValue constants = directory.getEntry("models:constants", JsonValue.class);
        int width = map.getInt("width");
        int height = map.getInt("height");


        int tileSize = map.getInt("tileheight");
        float div = 25f;
        int start = 1;


        Tile[] tiles = createTiles(tileset, div, tileSize);


        int ii = 0, jj = height - 1;
        ArrayList<TObject> tileObjects = new ArrayList<>();
        for (JsonValue layer : map.get("layers")) {
            if (layer.get("data") != null) {
                for (int n = 0; n < width * height; n++) {
                    int id = Integer.parseInt(layer.get("data").get(n).toString());

                    id = (id == 0) ? 51 : id - start;

                    Tile tile = tiles[id];
                    float sy = (tileSize / div) * jj;
                    float sx = (tileSize / div) * ii;
                    switch (tile.tileType) {
                        case Wall:
                            float[] newVertices = new float[tile.vertices.length];
                            int index = 0;
                            for (Float f : tile.vertices)
                                newVertices[index++] = (index % 2 == 0) ? f + sy : f + sx;
                            gameObjects.add(new Wall(newVertices, 0, 0));
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
//                                idItems.add(item);
                            break;
                        case Door:
                            float[] doorVerticies = new float[tile.vertices.length];
                            index = 0;
                            for (Float f : tile.vertices)
                                doorVerticies[index++] = (index % 2 == 0) ? f + sy : f + sx;
                            Door door = new Door(doorVerticies, 0, 0);
                            gameObjects.add(door);
//                                idItems.add(door);
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
                            if (layer.getString("name").equals("walls"))
                                gameObjects.add(new Dust(sx, sy));
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
                    float objectWidth = obj.getFloat("width") / div;
                    float objectHeight = obj.getFloat("height") / div;
                    float widthScale = (objectWidth * div) / tileSize;
                    float heightScale = (objectHeight * div) / tileSize;

                    int index = 0;
                    switch (tile.tileType) {
                        case Wall:

                            gameObjects.add(new Wall(createVerticies(tile, sx, sy, widthScale, heightScale), 0, 0));
                            break;
                        case Diver:
                            System.out.println("Diver Made!");
                            System.out.println("X: " + sx + " Y: " + sy);
                            gameObjects.add(new DiverModel(sx, sy, constants.get("diver")));
                            break;
                        case DeadBody:
                            System.out.println("Pass: Body");
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

                            door.setDoorScale(obj.getFloat("width"), obj.getFloat("height"));
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

                            gameObjects.add(new GoalDoor(sx, sy + objectHeight / 2, objectWidth, objectHeight));
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
                        case Plant:
                            gameObjects.add(new Plant(sx, sy));
                    }


                }

            }
        }

        System.out.println("ALL PASS!");
        return gameObjects;
    }


}
