package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public class IsSteerable implements Component, Poolable {

    float mass;
    Vector3 velocity;
    float maxForce;
    float maxSpeed;

    Vector3 forward = new Vector3(0, 0, 1);

    // from HasRotation:
    //    Quaternion orientation;

    // from HasPosition:
    //    Position position;

    @Override
    public void reset() {
        mass = 1;
        velocity = new Vector3().set(Vector3.X);
        maxForce = 1;
        maxSpeed = 1;
    }

    public IsSteerable withForward(Vector3 forward) {
        this.forward = forward;
        return this;
    }

    public Vector3 getForward() {
        return forward;
    }

}
