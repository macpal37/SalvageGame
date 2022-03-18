package com.xstudios.salvage.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController {


    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector2 cameraPosition;

    private Rectangle bounds;

    private final float DEFAULT_CAMERA_SPEED = 10f;
    private float cameraSpeed;

    private float aspectRatio;

    public CameraController(float width, float height){
        aspectRatio = width/height;
        camera = new OrthographicCamera();
//        viewport = new ScreenViewport(width*aspectRatio,height,camera)
        viewport = new ScreenViewport(camera);
        viewport.apply();
        cameraPosition = new Vector2(width/2,height/2);
        camera.position.set(cameraPosition,0);
        cameraSpeed = DEFAULT_CAMERA_SPEED;
    }
    public CameraController(float x, float y, float width, float height){
        aspectRatio = width/height;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(width*aspectRatio,height,camera);
        viewport.apply();
        cameraPosition = new Vector2(x,y);
        camera.position.set(cameraPosition,0);
    }
    public void setBounds (int x, int y, int width,int height){
        bounds = new Rectangle(x-width/2,y-height/2,width/2,height/2);
    }

    public void setCameraSpeed(float speed ){
        cameraSpeed = speed;
    }
    public  float getCameraSpeed(){
        return cameraSpeed;
    }
    public void resize (int width, int height){
        viewport.update(width,height);
        cameraPosition = new Vector2((float)width/2,(float)height/2);
        camera.position.set(cameraPosition,0);

    }

    public OrthographicCamera getCamera(){
        return  camera;
    }

    public void render(){
        camera.update();
        InputController input = InputController.getInstance();
        if(input.getHorizontal()!=0)
        {

           translate(input.getHorizontal()*cameraSpeed,0);
        }
        if(input.getVertical()!=0)
        {
           translate(0,input.getVertical()*cameraSpeed);
        }

    }

    public void setCameraPosition(float x, float y){
        cameraPosition.set(x,y);
        camera.position.set(cameraPosition,0);
    }


    public void translate(float x, float y){
        if(cameraPosition.x+x<bounds.width&&cameraPosition.x+x>bounds.x
        &&cameraPosition.y+y<bounds.height&&cameraPosition.y+y>bounds.y){
            cameraPosition.add(x, y);
            camera.position.set(cameraPosition,0);
        }

    }

}
