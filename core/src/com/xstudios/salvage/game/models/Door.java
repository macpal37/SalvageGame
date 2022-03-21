package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.xstudios.salvage.game.GameCanvas;

public class Door extends Wall {
    private ItemModel key;

    public Door(float[] points, ItemModel key) {
        this(points, 0, 0, key);
    }

    public Door(float[] points, float x, float y, ItemModel key) {
        super(points, x, y);
        this.key = key;
    }

    public ItemModel getKey() {
        return key;
    }

    public boolean isActive() {
        return body.isActive();
    }

    public void draw(GameCanvas canvas) {

        if (region != null) {
            if (isActive()) {
                canvas.draw(region, Color.WHITE, 0, 0, (getX() - anchor.x) * drawScale.x, (getY() - anchor.y) * drawScale.y, getAngle(), 1, 1);
            } else {

                canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);
                canvas.draw(region, Color.WHITE, 0, 0, (getX() - anchor.x) * drawScale.x, (getY() - anchor.y) * drawScale.y, getAngle(), 1, 1);
                canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);
            }

        }

    }
}
