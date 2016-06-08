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
    Vector3 up = new Vector3(0, 1, 0);
    float moveSpeed;
    float rotationSpeed;

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

    public IsSteerable withUp(Vector3 up) {
        this.up = up;
        return this;
    }

    public Vector3 getUp() {
        return up;
    }

    public IsSteerable withMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
        return this;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public IsSteerable withRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

}
