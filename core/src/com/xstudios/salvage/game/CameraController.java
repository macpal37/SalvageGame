package com.xstudios.salvage.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController {


    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 cameraPosition;

    private Vector3 targetPosition;

    private Rectangle bounds;

    private final float DEFAULT_CAMERA_SPEED = 10f;



    private float smoothSpeed = 0.125f;


    private float cameraSpeed;

    private float aspectRatio;

    public CameraController(float width, float height){
        aspectRatio = width/height;
        camera = new OrthographicCamera();
//        viewport = new ScreenViewport(width*aspectRatio,height,camera)
        viewport = new ScreenViewport(camera);
        viewport.apply();
        cameraPosition = new Vector3(width/2,height/2,0);
        camera.position.scl(cameraPosition);
        cameraSpeed = DEFAULT_CAMERA_SPEED;
    }
    public CameraController(float x, float y, float width, float height){
        aspectRatio = width/height;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(width*aspectRatio,height,camera);
        viewport.apply();
        cameraPosition = new Vector3(x,y,0);
        camera.position.scl(cameraPosition);
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
        setCameraPosition((float)width/2,(float)height/2);

    }

    public OrthographicCamera getCamera(){
        return  camera;
    }

    public void render(){
        camera.update();
        InputController input = InputController.getInstance();


    }

    public void setCameraPosition(Vector2 newPos){
        cameraPosition.set(newPos.x, newPos.y,0);
        camera.position.set(cameraPosition);
    }

    public void setCameraPosition(float x, float y){
        cameraPosition.set(x, y,0);
        camera.position.set(cameraPosition);
    }


    public void translate(float x, float y){
        if(cameraPosition.x+x<bounds.width&&cameraPosition.x+x>bounds.x
        &&cameraPosition.y+y<bounds.height&&cameraPosition.y+y>bounds.y){
            updatePosition(x,y);
        }

    }
    private void updatePosition(float x, float y){
        cameraPosition.add(x, y,0);
        camera.position.set(cameraPosition);
    }
    public void setSmoothSpeed(float smoothSpeed) {
        this.smoothSpeed = smoothSpeed;
    }
    public float getSmoothSpeed() {
        return smoothSpeed;
    }
}
