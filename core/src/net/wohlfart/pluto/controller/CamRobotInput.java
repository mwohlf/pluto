package net.wohlfart.pluto.controller;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.MovementSystem;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;

/**
 * a robot, position and rotation are set by external behavior implementations
 */
public class CamRobotInput extends InputAdapter implements ITransformCalculator {

    private final HasPosition nextPosition;
    private final HasRotation nextRotation;

    private final Matrix4 tmpMatrix = new Matrix4();
    private final Vector3 tmpVector = new Vector3();

    public CamRobotInput(Entity entity, EntityPool entityPool) {

        nextRotation = entityPool.createComponent(HasRotation.class)
                .withRotation(MovementSystem.NULL_ROTATION);

        nextPosition = entityPool.createComponent(HasPosition.class)
                .withPosition(MovementSystem.NULL_POSITION);
    }

    public HasPosition getHasPosition() {
        return nextPosition;
    }

    public HasRotation getHasRotation() {
        return nextRotation;
    }

    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        tmpMatrix.idt();
        tmpMatrix.rotate(nextRotation.getRotation().conjugate());
        nextPosition.getPosition().get(tmpVector);
        tmpMatrix.setTranslation(tmpVector.scl(-1));

        nextRotation.getRotation().set(MovementSystem.NULL_ROTATION); // reset because cam has no rotation
        nextPosition.getPosition().set(MovementSystem.NULL_POSITION); // reset because the cam is always at 0/0/0

        return tmpMatrix;
    }

    public boolean hasTransform() {
        return true;
    }

}
