package net.wohlfart.pluto.scene.properties;

import javax.annotation.Nonnull;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

import net.wohlfart.pluto.scene.TransformMethod;

public class HasTransformMethod implements Component, Poolable {

    // how the transformation is applied to the entity
    @Nonnull
    private TransformMethod transformMethod = TransformMethod.NULL;

    @Override
    public void reset() {
        transformMethod = TransformMethod.NULL;
    }

    /**
     * apply rotation an translation to the matrix
     */
    public HasTransformMethod withSetterTransformMethod(Matrix4 matrix) {
        this.transformMethod = new TransformMethod.TransformSetter(matrix);
        return this;
    }

    /**
     * only apply the rotation to the direction vector
     */
    public HasTransformMethod withDirectionTransformMethod(Vector3 vector) {
        this.transformMethod = new TransformMethod.TransformDirection(vector);
        return this;
    }

    /**
     * only apply the rotation to the matrix
     */
    public HasTransformMethod withRotationTransformMethod(Matrix4 matrix) {
        this.transformMethod = new TransformMethod.TransformRotation(matrix);
        return this;
    }

    /**
     * apply translation and rotation to the matrix and set the vector to the translation
     */
    public HasTransformMethod withPositionTransformMethod(Vector3 vector, Matrix4 matrix) {
        this.transformMethod = new TransformMethod.TransformPosition(vector, matrix);
        return this;
    }

    /**
     * only apply the translation to the matrix, no rotation
     */
    public HasTransformMethod withTranslation(Matrix4 matrix) {
        this.transformMethod = new TransformMethod.TranslatePosition(matrix);
        return this;
    }

    /**
     * custom transform method
     */
    public HasTransformMethod withTransformMethod(TransformMethod transformMethod) {
        this.transformMethod = transformMethod;
        return this;
    }

    @Nonnull
    public TransformMethod getTransformMethod() {
        return transformMethod;
    }

}
