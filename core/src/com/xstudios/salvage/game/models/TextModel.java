package com.xstudios.salvage.game.models;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.xstudios.salvage.game.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GameCanvas;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.game.levels.LevelBuilder;
import com.xstudios.salvage.util.FilmStrip;

public class TextModel extends GameObject {


    private FilmStrip spriteSheet;

    private Fixture geometry;
    private PolygonShape shape;
    private float radius = 2.5f;

    public void setTextPosition(float x, float y) {
        this.textPosition.set(x, y);
    }

    private Vector2 textPosition;

    private Color textColor;

    public void setFont(BitmapFont font) {
        this.font = font;
        font.setColor(textColor);
    }

    private BitmapFont font;

    public void setText(String text) {
        this.text = text;
    }

    private String text = "";


    public TextModel(float x, float y) {
        super(x, y);
        textPosition = new Vector2(x, y);

        setTextActive(false);


        textColor = new Color(1f, 1f, 1f, 1f);


    }


    @Override
    public void deactivatePhysics(World world) {

    }

    public CircleShape textRadius;

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }
        releaseFixtures();
        FixtureDef textDef = new FixtureDef();
        textDef.isSensor = true;
        textDef.filter.maskBits = -1;
        textRadius = new CircleShape();
        textRadius.setRadius(radius);
        textDef.shape = textRadius;
        Fixture hitboxFixture = body.createFixture(textDef);
        hitboxFixture.setUserData("Text");
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

//        if (textActive) {
//
//
//            if (tick % 5 == 0) {
//                if (isDisplay) {
//                    if (textColor.a < 1)
//                        textColor.set(1f, 1f, 1f, textColor.a + 0.05f);
//                } else {
//                    if (textColor.a > 0)
//                        textColor.set(1f, 1f, 1f, textColor.a - 0.05f);
//                }
//                font.setColor(textColor);
//            }

        canvas.drawText(text, font,
                (textPosition.x) * drawScale.x * worldDrawScale.x, (textPosition.y) * drawScale.y * worldDrawScale.y);
//        }

    }

    @Override
    protected void resize(float width, float height) {

    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        canvas.drawPhysics(textRadius, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
    }


    private boolean textActive;

    public void setTextActive(boolean b) {
        textActive = b;
    }
}

