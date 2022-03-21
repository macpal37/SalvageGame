package com.xstudios.salvage.game.models;

import com.badlogic.gdx.utils.Array;

public class Door extends Wall{
    private ItemModel key=null;


    public Door(float[] points, float x, float y){
        this(points, x,y,null);
    }
    public Door(float[] points, float x, float y, ItemModel key){
        super(points, x,y);
        this.key=key;
    }

    public ItemModel getKey(){return key;}

    public void addKey(ItemModel key){
        this.key=key;
    }

    public boolean isActive(){return body.isActive();}

}
