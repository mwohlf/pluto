package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool.Poolable;

public class IsPickable implements Component, Poolable {

    private float radius;
    private Matrix4 transform = new Matrix4();

    @Override
    public void reset() {
        radius = 0;
    }

    public IsPickable withPickRange(float radius) {
        this.radius = radius;
        return this;
    }

    public IsPickable withTransform(Matrix4 transform) {
        this.transform = transform;
        return this;
    }

    public float getPickRange() {
        return radius;
    }

    public Matrix4 getTransform() {
        return transform;
    }

}
