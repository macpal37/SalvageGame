package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.xstudios.salvage.assets.AssetDirectory;


public class Player {
    private int level;
    private int music;
    private int sound_effects;

    public Player(AssetDirectory directory){
        System.out.println("inside the player directory");

        if (isMac()) {
            // check if a save file exists in home directory
            // if exists, load from it
            // if dne, create a new save file in the home directory
            FileHandle file = Gdx.files.external("salvage_save_files/player.json");
            if (file.exists()) {
                JsonReader reader = new JsonReader();
                JsonValue json = reader.parse(file);
                sound_effects = json.getInt("sound_effects", 2);
                music = json.getInt("music", 2);
                level = json.getInt("level", 1);
            } else {
                sound_effects = 2;
                music = 2;
                level = 1;
            }
        } else {
            JsonValue json = directory.getEntry("player", JsonValue.class);
            sound_effects = json.getInt("sound_effects", 2);
            music = json.getInt("music", 2);
            level = json.getInt("level", 1);
        }
    }

    public boolean isMac() {
        return ((String)System.getProperties().get("os.name")).contains("Mac");
    }

    public int getLevel(){
        return level;
    }

    public void nextLevel(){
        level++;
        save();
    }

    public void setLevel(int l){
        level = l;
    }

    public void setMusic(int m){
        music = m;
    }

    public int getMusic(){
        return music;
    }

    public void setSoundEffects(int s){sound_effects = s;}

    public int getSoundEffects(){ return sound_effects;}

    public void save() {

        if (isMac()) {
            FileHandle file = Gdx.files.external("salvage_save_files/player.json");
            Json json = new Json();
            json.setOutputType(OutputType.json);
            file.writeString(json.toJson(this),false);
        } else {
            System.out.println("Saving file");

            FileHandle file = Gdx.files.local("player.json");
            Json json = new Json();
            json.setOutputType(OutputType.json);
            System.out.println(json.toJson(this, Player.class));
            file.writeString(json.toJson(this, Player.class),false);
        }

    }

}


