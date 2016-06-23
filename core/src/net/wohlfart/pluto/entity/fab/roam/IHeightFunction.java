package net.wohlfart.pluto.entity.fab.roam;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

// returns value in [0...1]
@FunctionalInterface
public interface IHeightFunction {

    // returns [0...1]
    float calculate(Vector3 vect);

    class Range implements IHeightFunction {
        private final IHeightFunction delegate;
        private final float min;
        private final float delta;

        public Range(float min, float max, IHeightFunction delegate) {
            assert (min <= max);
            this.min = min;
            this.delta = max - min;
            this.delegate = delegate;
        }

        @Override
        public float calculate(Vector3 vect) {
            return delegate.calculate(vect) * delta + min;
        }

    }

    class XAxis implements IHeightFunction {

        @Override
        public float calculate(Vector3 vect) {
            return vect.y < -0.95f
                    ? ((vect.x > 0 && vect.z > 0)
                            ? 1f
                            : 0.5f)
                    : 0.5f;
        }

    }

    class Const implements IHeightFunction {

        private final float scalar;

        public Const(float scalar) {
            this.scalar = scalar;
        }

        @Override
        public float calculate(Vector3 vect) {
            return scalar;
        }

    }

    class Sinus implements IHeightFunction {

        private final float wavelength;
        private final float height;

        public Sinus(float wavelength, float height) {
            this.wavelength = wavelength / 4;
            this.height = height;
        }

        @Override
        public float calculate(Vector3 vect) {
            final float d = (float) Math.sqrt(vect.x * vect.x + vect.y * vect.y) / wavelength;
            return MathUtils.sin(d) * height;
        }

    }

    class Simplex implements IHeightFunction {

        private final double w;

        public Simplex(double w) {
            this.w = w;
        }

        @Override
        public float calculate(Vector3 vect) {
            // simplex noise is in the range [-1...+1] we need [0...1]
            return (float) (1f + SimplexNoise.noise(vect.x, vect.y, vect.z, w)) / 2f;
        }

    }

}
