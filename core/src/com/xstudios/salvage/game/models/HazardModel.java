package com.xstudios.salvage.game.models;

public class HazardModel extends Wall{
    /** the amount of oxygen that this hazard drains per frame*/
    private float oxygenDrain;

    /** number of frames the stun lasts for*/
    private float stunDuration;

    /** get oxygen drain rate*/
    public float getOxygenDrain(){return oxygenDrain;}

    /** get stun duration*/
    public float getStunDuration(){return stunDuration;}

    public HazardModel(float[] points, float oxygenDrain, float stunDuration){
        this(points, 0, 0, oxygenDrain, stunDuration);
    }

    public HazardModel(float points[], float x, float y, float oxygenDrain, float stunDuration){
        super(points, x,y);
        this.oxygenDrain=oxygenDrain;
        this.stunDuration=stunDuration;
    }


}
