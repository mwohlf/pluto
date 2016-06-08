package net.wohlfart.pluto.scene.properties;

import net.wohlfart.pluto.scene.Position;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasPosition implements Component, Poolable {
    private final Position position = new Position();

    @Override
    public void reset() {
        position.idt();
    }

    public HasPosition withPosition(double x, double y, double z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
        return this;
    }

    public HasPosition withPosition(Position position) {
        this.position.x = position.x;
        this.position.y = position.y;
        this.position.z = position.z;
        return this;
    }

    public void move(Vector3 velocity) {
        this.position.x += velocity.x;
        this.position.y += velocity.y;
        this.position.z += velocity.z;
    }

    public Position getPosition() {
        return position;
    }

}
