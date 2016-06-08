package net.wohlfart.pluto.entity;

import javax.annotation.Nonnull;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.controller.ITransformCalculator;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.properties.HasCamera;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;

/*
 *
 * circular motion (see: http://www.engineeringtoolbox.com/motion-formulas-d_941.html)
 *
 *    rotation:
 *
 *    torque, momentum:  a measure of the turning force on an object
 *
 *    angular velocity:       w = da / dt  [rad/s]
 *
 *    angular acceleration:   b = dw / dt  [rad/s^2]
 *
 *
 *
 * linear motion
 *
 *    position:        p [m]
 *
 *    speed:   rate of change of position
 *             v = ds / dt   [m/s]
 *
 *    velocity: speed with a direction component (vector)
 *
 *    acceleration:   rate of change of velocity
 *                    a = dv / dt   [m/s^2]
 */
public class MovementSystem extends EntitySystem {
    public static final Quaternion NULL_ROTATION = new Quaternion();
    public static final Quaternion ZERO_ROTATION = new Quaternion(Vector3.X, 0);
    public static final Position NULL_POSITION = new Position(0, 0, 0);

    private static final ImmutableArray<Entity> EMPTY = new ImmutableArray<>(new Array<>());

    private final Position tmpPosition = new Position();
    private final Quaternion tmpRotation = new Quaternion();
    private final Quaternion tmpQuaternion = new Quaternion();
    private final Matrix4 tmpMatrix = new Matrix4();

    @Nonnull
    private ImmutableArray<Entity> withPosRotTransformMethod = MovementSystem.EMPTY;
    @Nonnull
    private ImmutableArray<Entity> withTransformMethodOnly = MovementSystem.EMPTY;
    @Nonnull
    private ImmutableArray<Entity> withRotation = MovementSystem.EMPTY;
    @Nonnull
    private ImmutableArray<Entity> withPosition = MovementSystem.EMPTY;

    @Override
    public void addedToEngine(Engine engine) {

        withPosRotTransformMethod = engine.getEntitiesFor(Family
                .all(HasPosition.class, HasRotation.class, HasTransformMethod.class)
                .exclude(HasCamera.class) // don't move the cam
                .get());

        withTransformMethodOnly = engine.getEntitiesFor(Family
                .all(HasTransformMethod.class)
                .exclude(HasPosition.class, HasRotation.class)
                .get());

        withRotation = engine.getEntitiesFor(Family
                .all(HasRotation.class)
                .exclude(HasCamera.class)
                .get());

        withPosition = engine.getEntitiesFor(Family
                .all(HasPosition.class)
                .exclude(HasCamera.class)
                .get());
    }

    // incoming call for cam movement
    public void applyCamMovement(Matrix4 transform) {
        if (transform == ITransformCalculator.IDT_MATRIX) {
            return; // no movement of cam
        }
        tmpMatrix.set(transform);
        tmpQuaternion.set(tmpMatrix.getRotation(tmpQuaternion).nor());

        // apply rotation to the entities with rotation
        for (final Entity entity : withRotation) {
            final Quaternion rotation = entity.getComponent(HasRotation.class).getRotation();
            if (rotation == MovementSystem.NULL_ROTATION) {
                continue;
            }
            rotation.mulLeft(tmpQuaternion);
        }

        // apply cam rotation/movement to entities with position
        for (final Entity entity : withPosition) {
            final Position position = entity.getComponent(HasPosition.class).getPosition();
            if (position == MovementSystem.NULL_POSITION) {
                continue;
            }
            tmpPosition.set(entity.getComponent(HasPosition.class).getPosition().get(tmpPosition));
            tmpPosition.mul(tmpQuaternion);
            entity.getComponent(HasPosition.class).getPosition().set(tmpPosition);

            // move entity in cam-space
            tmpPosition.withTranslation(tmpMatrix);
            // tmpQuaternion.set(elem.getComponent(HasRotation.class).getRotation());
            tmpPosition.mul(tmpQuaternion);
            entity.getComponent(HasPosition.class).getPosition().move(tmpPosition);
        }

        // apply cam rotation/movement to entities with only a transform method (e.g. lights)
        for (final Entity entity : withTransformMethodOnly) {
            tmpPosition.withTranslation(tmpMatrix);
            tmpRotation.set(tmpMatrix.getRotation(tmpRotation));
            entity.getComponent(HasTransformMethod.class).getTransformMethod().apply(tmpPosition, tmpRotation);
        }

    }

    @Override
    public void update(float deltaTime) {
        callTransformMethod(deltaTime);
    }

    // calculate entity's transform from its position and rotation
    private void callTransformMethod(float deltaTime) {
        for (final Entity entity : withPosRotTransformMethod) {
            final HasRotation r = entity.getComponent(HasRotation.class);
            tmpQuaternion.set(r.getRotation());
            tmpQuaternion.conjugate();

            final HasPosition p = entity.getComponent(HasPosition.class);
            tmpPosition.set(p.getPosition())
                    .mul(tmpQuaternion);

            // todo: remove matrix here
            tmpMatrix.idt()
                    .rotate(r.getRotation())
                    .translate((float) tmpPosition.x, (float) tmpPosition.y, (float) tmpPosition.z);

            tmpPosition.set(p.getPosition())
                    .withTranslation(tmpMatrix);

            entity.getComponent(HasTransformMethod.class).getTransformMethod().apply(tmpPosition, r.getRotation());
        }
    }

}
