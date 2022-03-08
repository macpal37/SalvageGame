package com.xstudios.salvage.game;

import com.badlogic.gdx.graphics.Color;

public class DeadBody extends GameObject{

    protected float scale;

    public DeadBody(float x, float y,float scale){
        position.set(x,y);
        this.scale = scale;
        start_position.set(x,y);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.BODY;
    }
    @Override
    public void draw(GameCanvas canvas) {
        canvas.draw(animator, Color.WHITE, origin.x, origin.y,
                position.x, position.y, 0.0f, scale, scale);
    }
}
