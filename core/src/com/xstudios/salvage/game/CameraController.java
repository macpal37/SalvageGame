package com.xstudios.salvage.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController {


    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 cameraPosition;
    private Vector2 cameraPosition2D;

    private Vector3 targetPosition;

    private Rectangle bounds;

    private final float DEFAULT_CAMERA_SPEED = 10f;

    private float smoothSpeed = 0.125f;


    private float cameraSpeed;

    private float aspectRatio;


    public void setZoom(float z) {
        camera.zoom = z;
    }


    public CameraController(float width, float height) {
        aspectRatio = width / height;


        camera = new OrthographicCamera();

        viewport = new ScreenViewport();
        viewport.apply();
        cameraPosition = new Vector3(width / 2, height / 2, 0);
        cameraPosition2D = new Vector2(500, 500);
        camera.position.scl(cameraPosition);

        cameraSpeed = DEFAULT_CAMERA_SPEED;

    }

    public CameraController(float x, float y, float width, float height) {
        aspectRatio = width / height;
        camera = new OrthographicCamera();
        viewport = new FillViewport(width * aspectRatio, height, camera);
        viewport.apply();
        cameraPosition = new Vector3(x, y + 100, 0);
        cameraPosition2D = new Vector2(0, 0);
        camera.position.scl(cameraPosition);
    }


    public void setBounds(int x, int y, int width, int height) {
        bounds = new Rectangle(x - width / 2, y - height / 2, width / 2, height / 2);
    }

    public void setCameraSpeed(float speed) {
        cameraSpeed = speed;
    }

    public float getCameraSpeed() {
        return cameraSpeed;
    }

    public void resize (int width, int height){
        viewport.update(width,height);
        setCameraPosition((float)width/2,(float)height/2);
    }


    public OrthographicCamera getCamera() {

        return camera;
    }

    public void render() {
        camera.update();
    }

    public void setCameraPosition(Vector2 newPos) {
        cameraPosition.set(newPos.x, newPos.y, 0);
        camera.position.set(cameraPosition);
    }

    public void setCameraPosition(float x, float y) {
        cameraPosition.set(x, y, 0);
        camera.position.set(cameraPosition);
    }

    public Vector2 getCameraPosition2D() {
        cameraPosition2D.x = cameraPosition.x;
        cameraPosition2D.y = cameraPosition.y;
        return cameraPosition2D;
    }

    public void setViewport(int width, int height){
        viewport.update(width, height);
    }

    private void updatePosition(float x, float y) {
        cameraPosition.add(x, y, 0);
        camera.position.set(cameraPosition);
    }

    public float getSmoothSpeed() {
        return smoothSpeed;
    }

    public float getCameraWidth() {
        return viewport.getScreenWidth();
    }

    public float getCameraHeight() {
        return viewport.getScreenHeight();
    }
}

