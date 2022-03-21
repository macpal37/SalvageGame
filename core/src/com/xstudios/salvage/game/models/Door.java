package com.xstudios.salvage.game.models;

import com.badlogic.gdx.utils.Array;

public class Door extends Wall{
    private ItemModel key;

    public Door(float[] points, ItemModel key){
        this(points, 0,0, key);
    }
    public Door(float[] points, float x, float y, ItemModel key){
        super(points, x,y);
        this.key=key;
    }

    public ItemModel getKey(){return key;}

    public boolean isActive(){return body.isActive();}

}
