package com.xstudios.salvage.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.xstudios.salvage.assets.AssetDirectory;


public class Player {
    private int sound_effects;
    private int music;
    private int level;
    JsonValue json;

    public Player(AssetDirectory directory){
        System.out.println("inside the player directory");

        if (isMac()) {
            // check if a save file exists in home directory
            // if exists, load from it
            // if dne, create a new save file in the home directory
            FileHandle file = Gdx.files.external("salvage_save_files/player.json");
            if (file.exists()) {
                JsonReader reader = new JsonReader();
                json = reader.parse(file);
                sound_effects = json.getInt("sound_effects", 2);
                music = json.getInt("music", 2);
                level = json.getInt("level", 1);
            } else {
                sound_effects = 2;
                music = 2;
                level = 1;
            }
        } else {
            json = directory.getEntry("player", JsonValue.class);
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

        JsonValue updateLevel = new JsonValue(level);
        JsonValue updateMusic = new JsonValue(music);
        JsonValue updateSoundEffects = new JsonValue(sound_effects);

        json.remove("level");
        json.remove("music");
        json.remove("sound_effects");

        json.addChild("level", updateLevel);
        json.addChild("music", updateMusic);
        json.addChild("sound_effects", updateSoundEffects);

        if (isMac()) {
            FileHandle file = Gdx.files.external("salvage_save_files/player.json");
            file.writeString(json.toJson(JsonWriter.OutputType.json),false);
        } else {
            FileHandle file = Gdx.files.local("core/assets/player.json");
            file.writeString(json.toJson(JsonWriter.OutputType.json),false);
        }

    }

}


