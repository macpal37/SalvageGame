package com.xstudios.salvage.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Array;

/**
 * Model class representing an obstacle such as a wall that the
 * diver can't swim through
 */
public class ObstacleContainer {
    /** color of obstacles*/
    private static final Color WALL_COLOR=Color.RED;

    private Array<Rectangle> allObstacles;

    private Rectangle tempRectangle;


    /**
     * Creates a container for all obstacles
     *
     */
    public ObstacleContainer(){
        this.allObstacles=new Array<Rectangle>();
        tempRectangle=new Rectangle();
    }

    /**
     * Adds a new rectangle to the obstacle container
     * @param x x-coordinate of lower-left corner of the obstacle
     * @param y y-coordinate of lower-left corner of the obstacle
     * @param width width of rectangle
     * @param height height of rectangle
     * @return the newly created rectangle
     */
    public Rectangle addRectangle(float x, float y, float width, float height){
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
    







}
