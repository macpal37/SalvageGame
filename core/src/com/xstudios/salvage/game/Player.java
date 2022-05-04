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
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.xstudios.salvage.assets.AssetDirectory;
import com.xstudios.salvage.game.models.DiverModel;
import com.xstudios.salvage.game.models.GoalDoor;

import java.io.BufferedWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static jdk.internal.org.jline.utils.Colors.s;

public class Player {
    private int sound_effects;
    private int music;
    private int level;
    JsonValue json;

    public Player(AssetDirectory directory){
        json = directory.getEntry("player", JsonValue.class);
        sound_effects = json.getInt("sound_effects", 2);
        music = json.getInt("music", 2);
        level = json.getInt("level", 1);
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
        FileHandle file = Gdx.files.local("core/assets/player.json");

        json.remove("level");
        json.remove("music");
        json.remove("sound_effects");

        json.addChild("level", updateLevel);
        json.addChild("music", updateMusic);
        json.addChild("sound_effects", updateSoundEffects);

        file.writeString(json.toJson(JsonWriter.OutputType.json),false);
    }

}


