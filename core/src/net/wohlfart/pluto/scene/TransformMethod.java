package net.wohlfart.pluto.scene;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * this defines different ways to apply the user input or animation created transformation to graph entities
 * this class encapsulates the model to world matrix of a renderable provider
 *
 * - light sources are bound to the environment and need to be de-rotated
 * - skybox doesn't need translation just transformation
 * - directional light doesn't move, the direction needs to be adjusted when the scene rotates
 */
public interface TransformMethod {

    TransformMethod NULL = new TransformMethod() {
        @Override
        public void apply(Position tmpTranslation, Quaternion tmpRotation) {
            // do nothing
        }
    };

    void apply(Position tmpTranslation, Quaternion tmpRotation);

    class TransformSetter implements TransformMethod {
        private final Matrix4 matrix;

        public TransformSetter(Matrix4 matrix) {
            this.matrix = matrix;
        }

        @Override
        public void apply(Position position, Quaternion rotation) {
            this.matrix.set(position.logVector3(), rotation);
            // TODO: add scaling here
            //final float scale = 1000f / position.floatDist2();
            final float scale = 1f;
            this.matrix.scale(scale, scale, scale);
        }
    }

    // only used for light sources that have renderables
    class TransformPosition implements TransformMethod {
        private final Matrix4 matrix;
        private final Vector3 position;

        public TransformPosition(Vector3 position, Matrix4 matrix) {
            this.position = position;
            this.matrix = matrix;
        }

        @Override
        public void apply(Position tmpTranslation, Quaternion tmpRotation) {
            // update the matrix
            this.matrix.set(tmpTranslation.logVector3(), tmpRotation);
            // update the position according to the matrix
            this.position.set(tmpTranslation.logVector3());
        }
    }

    // skybox, only rotating
    class TransformRotation implements TransformMethod {
        private final Matrix4 matrix;
        private final Matrix4 tmpMatrix = new Matrix4();

        public TransformRotation(Matrix4 targetMatrix) {
            this.matrix = targetMatrix;
        }

        @Override
        public void apply(Position tmpTranslation, Quaternion tmpRotation) {
            // only set the rotation
            this.matrix.mulLeft(tmpMatrix.set(tmpRotation));
        }
    }

    // directional light
    class TransformDirection implements TransformMethod {
        private final Vector3 direction;

        public TransformDirection(Vector3 direction) {
            this.direction = direction;
        }

        @Override
        public void apply(Position tmpTranslation, Quaternion tmpRotation) {
            direction.mul(tmpRotation).nor();
        }
    }

    // for decals
    class TranslatePosition implements TransformMethod {
        private final Matrix4 matrix;

        public TranslatePosition(Matrix4 targetMatrix) {
            this.matrix = targetMatrix;
        }

        @Override
        public void apply(Position tmpTranslation, Quaternion tmpRotation) {
            matrix.setToTranslation(tmpTranslation.logVector3());
        }

    }

}
