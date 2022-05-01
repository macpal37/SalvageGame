package com.xstudios.salvage.game.levels;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.xstudios.salvage.game.GObject;
import com.xstudios.salvage.game.GameObject;
import com.xstudios.salvage.game.models.*;
;
import com.xstudios.salvage.util.FilmStrip;
import com.xstudios.salvage.util.PooledList;

import java.util.ArrayList;

public class LevelModel {
    /**
     * Level Attributes
     */
    protected DiverModel diver;

    protected ItemModel key;
    //    protected ItemModel dead_body;
    protected DeadBodyModel dead_body;
    protected ArrayList<GoalDoor> goalArea = new ArrayList<>();
    protected ArrayList<Tentacle> tentacles = new ArrayList<>();

    private Array<Door> doors = new Array<Door>();

    /**
     * All the objects in the world.
     */
    protected PooledList<GameObject> objects = new PooledList<GameObject>();

    protected PooledList<GameObject> aboveObjects = new PooledList<GameObject>();


    public LevelModel() {

    }

    public DiverModel getDiver() {
        return diver;
    }

    public DeadBodyModel getDeadBody() {
        return dead_body;
    }

    public Array<Door> getDoors() {
        return doors;
    }

    public ArrayList<Tentacle> getTentacles() {
        return tentacles;
    }

    public PooledList<GameObject> getAllObjects() {
        return objects;
    }

    public PooledList<GameObject> getAboveObjects() {
        return aboveObjects;
    }

    /**
     * Add new objects to the list of all objects and the category lists they correspond to
     */
    public void addObject(GameObject obj) {
        objects.add(obj);
        if (obj instanceof Door) {
            doors.add((Door) obj);
        } else if (obj instanceof DiverModel) {
            diver = (DiverModel) obj;
        } else if (obj instanceof DeadBodyModel) {
            dead_body = (DeadBodyModel) obj;
        } else if (obj instanceof ItemModel) {
        } else if (obj instanceof GoalDoor) {
            goalArea.add((GoalDoor) obj);
        } else if (obj instanceof Tentacle) {
            tentacles.add((Tentacle) obj);
        }
    }
    /**
     * remove objects to the list of all objects and the category lists they correspond to
     */
    public void removeObject(GameObject obj) {
        objects.remove(obj);
        if(obj instanceof Tentacle){
            tentacles.remove((Tentacle) obj);
        }
    }
    public void dispose() {
        // TODO: do we need to clear all of the arrays?
        getAboveObjects().clear();
        getAllObjects().clear();
        objects = null;
        aboveObjects = null;
    }
}
