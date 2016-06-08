package net.wohlfart.pluto.scene.properties;

import net.wohlfart.pluto.Camera;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasCamera implements Component, Poolable {

    private Camera cam;

    @Override
    public void reset() {
    }

    public HasCamera withCamera(Camera cam) {
        this.cam = cam;
        return this;
    }

    public Camera getCamera() {
        return cam;
    }

}
