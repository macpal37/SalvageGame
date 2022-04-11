package com.xstudios.salvage.game;

import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class PhysicsController {

    public static Vector2[][] CURRENTS;
    int width;
    int height;

    public PhysicsController(int w, int h) {
        width = w;
        height = h;
        CURRENTS = new Vector2[height][width];

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Random random = new Random();
                CURRENTS[r][c] = new Vector2(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f);
                CURRENTS[r][c].scl(0.0003f);
            }
        }

    }

    public Vector2 getCurrentVector(float xpos, float ypos) {
        Vector2 to_return;

        try {
            to_return = CURRENTS[(int) ypos / height + height / 2][(int) xpos / width + width / 2];
        } catch (Exception e) {
            to_return = CURRENTS[0][0];
        }
        return to_return;
    }

    public Vector2 getCurrentVector(Vector2 pos) {
        return getCurrentVector(pos.x, pos.y);
    }


}
