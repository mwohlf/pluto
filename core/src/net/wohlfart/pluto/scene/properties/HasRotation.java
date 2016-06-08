package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasRotation implements Component, Poolable {
    private final Quaternion rotation = new Quaternion();

    @Override
    public void reset() {
        rotation.idt();
    }

    public HasRotation withRotation(Quaternion quaternion) {
        rotation.set(quaternion);
        return this;
    }

    public Quaternion getRotation() {
        return rotation;
    }

}
