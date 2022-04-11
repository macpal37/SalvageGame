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
        Empty, Wall, Diver, Obstacle, Item, Door, DeadBody, Block, Goal, Hazard
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


    public ArrayList<GObject> createLevel(String levelFileName, String tilesetFileName, TextureRegion tilesetImage) {
        System.out.println("Creating Level");
        ArrayList<GObject> gameObjects = new ArrayList<GObject>();

        JsonValue map = jsonReader.parse(Gdx.files.internal("levels/" + levelFileName + ".json"));
        JsonValue tileset = jsonReader.parse(Gdx.files.internal("levels/tilesets/" + tilesetFileName + ".json"));
        JsonValue constants = directory.getEntry("models:constants", JsonValue.class);
        int width = map.getInt("width");
        int height = map.getInt("height");


        int tileSize = map.getInt("tileheight");

        int start = 1;
        float div = 25f;
        Tile empty = new Tile();

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
                    for (JsonValue point : o.get("polygon")) {
                        float vx = (round(point.getFloat("x")) / div) + x;
                        float vy = tileSize / div - ((round(point.getFloat("y")) / div) + y);

                        verticies.add(vx);
                        verticies.add(vy);
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
        int ii = 0, jj = height - 1;
        System.out.println("Num Tieles: " + tiles.length);
//       ArrayList<Integer> banList = new ArrayList<>();
        ArrayList<GObject> idItems = new ArrayList<>();

        int idCount = 0;
        for (JsonValue layer : map.get("layers")) {

            for (int n = 0; n < width * height; n++) {
                int id = Integer.parseInt(layer.get("data").get(n).toString());
                if (!layer.getString("name").equals("ids")) {
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
                            System.out.println("Diver Made!");
                            System.out.println("X: " + sx + " Y: " + sy);
                            gameObjects.add(new DiverModel(sx, sy, constants.get("diver")));
                            break;
                        case DeadBody:
                            System.out.println("Pass: Body");
                            gameObjects.add(new DeadBodyModel(sx, sy, constants.get("dead_body")));
                            break;
                        case Item:
                            ItemModel item = new ItemModel(sx, sy, constants.get("key"), ItemType.KEY);
                            gameObjects.add(item);
                            idItems.add(item);
                            break;
                        case Door:
                            float[] doorVerticies = new float[tile.vertices.length];
                            index = 0;
                            for (Float f : tile.vertices)
                                doorVerticies[index++] = (index % 2 == 0) ? f + sy : f + sx;
                            Door door = new Door(doorVerticies, 0, 0);
                            gameObjects.add(door);
                            idItems.add(door);
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

                            float[] hazardVerticies = new float[tile.vertices.length];
                            index = 0;
                            for (Float f : tile.vertices)
                                hazardVerticies[index++] = (index % 2 == 0) ? f + sy : f + sx;
                            HazardModel hazard = new HazardModel(hazardVerticies, 0, 0);
                            gameObjects.add(hazard);

                        case Empty:
                            if (layer.getString("name").equals("walls"))
                                gameObjects.add(new Dust(sx, sy));
                            break;
                    }
                } else {
                    if (id != 0) {

                        id = id - 101;
                        System.out.println("ID: " + id);
                        System.out.println("OBJECT: " + idItems.get(idCount).toString());
                        idItems.get(idCount).setID(id);
                        idCount++;
                    }

                }


                ii++;
                if (ii == width) {
                    ii = 0;
                    jj--;

                }
            }
            ii = 0;
            jj = height - 1;


        }

        System.out.println("ALL PASS!");
        return gameObjects;
    }


}
