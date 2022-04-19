package com.xstudios.salvage.game.models;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Monster {
    private ArrayList<Tentacle> tentacles;
    private float aggrivation = 0.75f;

    public Monster(){
        tentacles = new ArrayList<>();
    }

    public void addTentacle (Tentacle tentacle) {
        tentacles.add(tentacle);
    }

    public void removeTentacle (Tentacle tentacle) {
        tentacles.remove(tentacle);
    }

    public ArrayList<Tentacle> getTentacles () {
        return tentacles;
    }

    public void setAggrivation (float temp_aggrivation) {
        aggrivation = temp_aggrivation;
    }

    public float getAggrivation () {
        return aggrivation;
    }
}

