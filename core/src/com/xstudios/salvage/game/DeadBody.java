package com.xstudios.salvage.game;

public class DeadBody extends GameObject{


    public DeadBody(float x, float y,float scale){
        position.set(x,y);
        radius = scale;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.BODY;
    }
}
