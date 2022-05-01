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


public class Player {
    private boolean mute;
    private int volume;
    private int level;
    JsonValue json;

    public Player(AssetDirectory directory){
        json = directory.getEntry("player", JsonValue.class);
        mute = json.getBoolean("mute", false);
        volume = json.getInt("volume", 5);
        level = json.getInt("level", 1);
    }

    public int getLevel(){
        return level;
    }

    public void nextLevel(){
        level++;
    }

    public void setVolume(int v){
        volume = v;
    }

    public int getVolume(){
        return volume;
    }

    public void setMute(boolean m){
        mute = m;
    }

    public boolean getMute(){
        return mute;
    }

    public void save() {
        JsonValue updateLevel = new JsonValue(level);
        JsonValue updateVolume = new JsonValue(volume);
        JsonValue updateMute = new JsonValue(mute);
        FileHandle file = Gdx.files.local("core/assets/player.json");

        json.remove("level");
        json.remove("volume");
        json.remove("mute");

        json.addChild("level", updateLevel);
        json.addChild("volume", updateVolume);
        json.addChild("mute", updateMute);

        file.writeString(json.toJson(JsonWriter.OutputType.json),false);
    }

}


