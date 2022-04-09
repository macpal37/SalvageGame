package com.xstudios.salvage.game.models;

public class HazardModel extends Wall{
    /** the amount of oxygen that this hazard drains per frame*/
    private float oxygenDrain;

    /** number of frames the stun lasts for*/
    private float stunDuration;

    public float getOxygenDrain(){return oxygenDrain;}

    public float getStunDuration(){return stunDuration;}

    public HazardModel(float oxygenDrain, float stunDuration, float[] points){
        this(oxygenDrain, stunDuration, points, 0,0);
    }

    public HazardModel(float oxygenDrain, float stunDuration, float[] points, float x, float y){
        super(points, x,y);
        this.oxygenDrain=oxygenDrain;
        this.stunDuration=stunDuration;
    }


}
