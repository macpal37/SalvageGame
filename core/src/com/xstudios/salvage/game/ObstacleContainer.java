package com.xstudios.salvage.game;


import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

/**
 * Model class representing an obstacle such as a wall that the
 * diver can't swim through
 */
public class ObstacleContainer {
    /** texture of the wall*/
    private Texture wallTexture;

    /** array containing all obstacles*/
    private Array<Rectangle> allObstacles;



    /**
     * Creates a container for all obstacles
     * @param wallTexture the texture image of the wall
     */
    public ObstacleContainer(Texture wallTexture){
        this.allObstacles=new Array<Rectangle>();
        this.wallTexture=wallTexture;
    }

    public int getSize(){return allObstacles.size;}

    /** return wallTexture*/
    public Texture getWallTexture(){return wallTexture;}

    /** return allObstacles*/
    public Array<Rectangle> getAllObstacles(){return allObstacles;}

    /** return an obstacle at a given id
     * @param id the id of the wall
     */
    public Rectangle getWall(int id){return allObstacles.get(id);}



    /**
     * Adds a new rectangle to the obstacle container
     * @param x x-coordinate of lower-left corner of the obstacle
     * @param y y-coordinate of lower-left corner of the obstacle
     * @param width width of rectangles
     * @param height height of rectangle
     * @return the newly created rectangle
     */
    public Rectangle addRectangle(float x, float y, float width, float height){
        Rectangle tempRectangle=new Rectangle((int)(x),(int)y,(int)width,(int)height);
        allObstacles.add(tempRectangle);
        return tempRectangle;
    }

    /**
     * Checks if the diver has collided with an obstacle
     * @param diver circle representing the diver
     * @return the obstacle collided with if true or null otherwise
     */
    public Rectangle getIntersectingObstacle(Rectangle diver){

        for(Rectangle obstacle: allObstacles){
//            System.out.println("OBS?: "+obstacle.toString());
//            System.out.println("Diver?: "+ diver.toString());
//            System.out.println("F: "+ diver.contains(obstacle));
            if(diver.intersects(obstacle)){
                tint = Color.RED;
               return obstacle;
            }else {
                tint = Color.GREEN;
            }
        }
        return null;
    }
    public Color tint = Color.GREEN;
    /** draw all walls on the game canvas
     *
     * @param allObstacles array containing all walls to be drawn
     * @param canvas gameCanvas
     */
    public void drawWalls(Array<Rectangle>allObstacles, GameCanvas canvas) {
        if (wallTexture == null) {
            return;
        }
        for (Rectangle wall : allObstacles) {
            float ox = 0;
            float oy = 0;
            float x = wall.x;
            float y = wall.y;
            float sx = (float)wall.width / wallTexture.getWidth();
            float sy = (float)wall.height / wallTexture.getHeight();

            canvas.draw(wallTexture, Color.WHITE, ox, oy, x, y, 0f, sx, sy);
        }
    }

}
