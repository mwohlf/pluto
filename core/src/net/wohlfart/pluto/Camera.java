package net.wohlfart.pluto;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import net.wohlfart.pluto.controller.CamRobotInput;
import net.wohlfart.pluto.scene.Position;

public class Camera extends PerspectiveCamera {

    private final CamRobotInput robotInput;

    public Camera(float fieldOfViewY, int viewportWidth, int viewportHeight, CamRobotInput robotInput) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
        this.robotInput = robotInput;
    }

    public void resize(float viewportWidth, float viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        update(true);
    }

    public void setFieldOfView(float fieldOfViewY) {
        this.fieldOfView = fieldOfViewY;
        update(true);
    }

    public CamRobotInput getRobotInput() {
        return robotInput;
    }

    public void project(Position worldCoordinates) {
        project(worldCoordinates, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void project(Position worldCoordinates,
                        float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
        worldCoordinates.prj(this.combined);
        worldCoordinates.x = viewportWidth * (worldCoordinates.x + 1) / 2 + viewportX;
        worldCoordinates.y = viewportHeight * (worldCoordinates.y + 1) / 2 + viewportY;
        worldCoordinates.z = (worldCoordinates.z + 1) / 2;
    }

}
