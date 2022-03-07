package com.xstudios.salvage.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Array;

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
     * @param width width of rectangle
     * @param height height of rectangle
     * @return the newly created rectangle
     */
    public Rectangle addRectangle(float x, float y, float width, float height){
        Rectangle tempRectangle=new Rectangle();
        tempRectangle.setX(x);
        tempRectangle.setY(y);
        tempRectangle.setWidth(width);
        tempRectangle.setHeight(height);
        allObstacles.add(tempRectangle);
        return tempRectangle;
    }

    /**
     * Checks if the diver has collided with an obstacle
     * @param diver circle representing the diver
     * @return the obstacle collided with if true or null otherwise
     */
    public Rectangle getIntersectingObstacle(Circle diver){
        Rectangle intersectingObstacle=null;
        if (allObstacles.size<1){
            return null;
        }
        for(Rectangle obstacle: allObstacles){
            if(Intersector.overlaps(diver, obstacle)){
                intersectingObstacle=obstacle;
                break;
            }
        }
        return intersectingObstacle;
    }

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
            float ox = wallTexture.getWidth() / 2;
            float oy = wallTexture.getHeight() / 2;
            float x = wall.getX();
            float y = wall.getY();
            float sx = wall.getWidth() / wallTexture.getWidth();
            float sy = wall.getHeight() / wallTexture.getHeight();

            canvas.draw(wallTexture, Color.GREEN, ox, oy, x, y, 0f, sx, sy);
        }
    }








}
