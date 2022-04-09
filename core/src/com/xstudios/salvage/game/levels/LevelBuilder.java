package com.xstudios.salvage.game.levels;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.models.Wall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.FileHandler;

public class LevelBuilder {
    private  JsonReader jsonReader;
    private AssetDirectory internal;
    public LevelBuilder(){

        jsonReader = new JsonReader();

    }
     enum TileType {
        Empty,Wall
    }
    TileType tileTypeFromString(String type){
        if(type.equals("Wall")){
            return TileType.Wall;
        }

        return  TileType.Empty;
    }

    class Tile {

        public float x,y,width,height;
        public float[] vertices;
       public TileType tileType;
        public Tile(){
            vertices = new float[0];
            x=0;
            y=0;
            width = 0;
            height = 0;
            tileType = TileType.Empty;
        }
        public Tile(float x,float y,float width,float height,float[] vertices,TileType t){
            this.vertices = vertices;
            this.x=x;
            this.y=y;
            this.width = width;
            this.height = height;
            tileType = t;
        }


    }

    float round(float num){
        float result = Math.abs(num);
        boolean isNegative = num<0;
        if (result>40 && result <60 ){
            result = 50;
        }else
        if (result <10 ){
            result = 0;
        }else
        if (result>90 && result <110 ){
            result = 100;
        }

            return (isNegative)?-result:result;
    }

    public  ArrayList<GObject> createLevel (String levelFileName, String tilesetFileName , TextureRegion tilesetImage){
        System.out.println("Creating Level");
        ArrayList<GObject> gameObjects = new ArrayList<GObject>();
        JsonReader json = new JsonReader();
        JsonValue map = json.parse(Gdx.files.internal("levels/"+levelFileName+".json"));
        JsonValue tileset = json.parse(Gdx.files.internal("levels/tilesets/"+tilesetFileName+".json"));

        int width = map.getInt("width");
        int height = map.getInt("height");
        int tileSize =  map.getInt("tileheight");
        int ii =0, jj = height-1;
        int start = 1;
        float div = 32f;
        Tile empty = new Tile();

       Tile[] tiles =  new Tile[tileset.getInt("tilecount")];

        ArrayList<Float> verticies = new ArrayList<>();
        int tt = 0;
        for (JsonValue tileJson : tileset.get("tiles") ){

           int id =tileJson.getInt("id");
           TileType tileType = TileType.Empty;

            if (tileJson.get("properties") != null) {
                for(JsonValue p: tileJson.get("properties")) {
                    if(p.getString("name").equals("model_type")) {
                        tileType = tileTypeFromString(p.getString("value"));
                    }
                }
            }



           if (tileJson.get("objectgroup") != null) {
               float x = 0;
               float y = 0;
               for(JsonValue o: tileJson.get("objectgroup").get("objects")){
                    x =  round(o.getFloat("x"))/div;
                   y =  round(o.getFloat("y"))/div;
                  verticies.clear();
                   for(JsonValue point: o.get("polygon")){
                       float vx = (round(point.getFloat("x"))/div)+x;
                       float vy = tileSize/div-((round(point.getFloat("y"))/div)+y);

                       verticies.add(vx);
                       verticies.add(vy);


                   }



               }

               float[] verts = new float[verticies.size()];
               int index = 0;
//               for(int i = verticies.size()-1; i>=0 ;i--)
//                   verts[index++] = verticies.get(i);
               for(int i = 0 ; i< verticies.size() ;i++)
                   verts[index++] = verticies.get(i);
               System.out.println(Arrays.toString(verts));
               tiles[tt] = (new Tile(x,y,tileSize/div,
                       tileSize/div, verts ,tileType
               ));
           }else{
               tiles[tt] = empty;
           }
            tt++;
           }

        int xx = 0;
        System.out.println("Num Tieles: "+ tiles.length);
        for (JsonValue layer : map.get("layers") ){
            for (JsonValue tileId: layer.get("data")){


                xx++;
               int id = Integer.parseInt(tileId.toString());
                id = (id==0)? 51 : id-1;
//                id =0;


                System.out.print(id+", ");

               Tile tile = tiles[id];
               switch(tile.tileType) {
                   case Wall:
                       float sx = (tileSize / div) * ii;
                       float sy = (tileSize / div) * jj;
                       float[] newVertices = new float[tile.vertices.length];
                       int index = 0;
                       for (Float f : tile.vertices)
                           newVertices[index++] = (index % 2 == 0) ? f + sy : f + sx;
                       gameObjects.add(new Wall(newVertices, 0, 0));
                       break;

                   case Empty:
               }

                ii++;
                if (ii==width) {
                    System.out.println();
                    ii=0;
                    jj--;
                }
//                jj--;
//                if (jj==-1) {
//                    System.out.println();
//                    ii++;
//                    jj= height-1;
//
//                }
            }

        }


        return gameObjects;
    }


}
