package com.xstudios.salvage.game;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.xstudios.salvage.game.models.DiverModel;

public class LightController {

    private PointLight light ;


    private RayHandler rayHandler;

    public PointLight getLight() {
        return light;
    }

    public LightController(World world){

        rayHandler = new RayHandler(world);
        light = new PointLight(rayHandler,100, Color.BLACK,20,0,0);
//        light.setSoftnessLength(5.0f);
        short s = 0x001;
        light.setContactFilter(s,s,s);
//        rayHandler.setAmbientLight(0.5f);


    }
    public RayHandler getRayHandler() {
        return rayHandler;
    }
    public void render(){

    }
DiverModel diver;
    public void setBody(DiverModel diver){
         light.setContactFilter((short) 1,(short)1,(short)1);
        light.setPosition(diver.getX()*diver.getDrawScale().x,diver.getY()*diver.getDrawScale().y);
        this.diver = diver;
    }

    public void update(CameraController cameraController){
//        rayHandler.updateAndRender();
        short s = 0x001;
        light.setContactFilter(s,s,s);
        rayHandler.setCombinedMatrix(cameraController.getCamera().combined.cpy().scl(32f),cameraController.
                        getCamera().position.x,cameraController.getCamera().position.y,
                cameraController.getCamera().viewportWidth,cameraController.getCamera().viewportHeight);
    }

}


