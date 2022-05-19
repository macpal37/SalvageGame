package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.xstudios.salvage.game.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.util.FilmStrip;

public class TextModel extends GameObject {


    private FilmStrip spriteSheet;

    private Fixture geometry;
    private PolygonShape shape;
    private CircleShape circ;
    private float radius = 10f;

    private Color textColor;

    private BitmapFont font;

    public void setText(String text) {
        this.text = text;
    }

    private String text = "";


    public TextModel(float x, float y) {
        super(x, y);

        circ = new CircleShape();
        circ.setRadius(radius);
        textColor = new Color(1f, 1f, 1f, 0f);
        font = GameController.displayFont;
        font.setColor(textColor);

    }

    @Override
    public boolean activatePhysics(World world) {
        return false;
    }

    @Override
    public void deactivatePhysics(World world) {

    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();
        fixture.filter.maskBits = -1;
        fixture.shape = shape;

        geometry = body.createFixture(fixture);
        markDirty(false);
    }

    @Override
    protected void releaseFixtures() {

    }

    public Vector2 getScale() {
        return scale;
    }

    public Vector2 scale = new Vector2(1, 1);

    public void setScale(float x, float y) {
        scale.set(x, y);
    }

    public void setDisplay(boolean display) {
        isDisplay = display;
    }

    private boolean isDisplay = false;


    @Override
    public void draw(GameCanvas canvas) {
        if (tick % 5 == 0) {
            if (isDisplay)
                if (textColor.a < 1)
                    textColor.set(1f, 1f, 1f, textColor.a + 0.05f);
                else if (textColor.a > 0)
                    textColor.set(1f, 1f, 1f, textColor.a - 0.05f);
            font.setColor(textColor);
        }

        canvas.drawText(text, font,
                (getX()) * drawScale.x * worldDrawScale.x, (getY()) * drawScale.y * worldDrawScale.y);


    }

    @Override
    protected void resize(float width, float height) {

    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(circ, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }
}

