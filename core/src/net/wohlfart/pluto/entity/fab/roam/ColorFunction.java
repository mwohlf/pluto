package net.wohlfart.pluto.entity.fab.roam;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public interface ColorFunction {

    Color calculate(Vector3 vector);

    class Const implements ColorFunction {
        private final Color color = new Color();

        public Const(Color color) {
            this.color.set(color);
        }

        @Override
        public Color calculate(Vector3 vector) {
            return color;
        }
    }

    class VectorColor implements ColorFunction {
        private final Color color = new Color();

        @Override
        public Color calculate(Vector3 vector) {
            return color.set(Math.abs(vector.x), Math.abs(vector.y), Math.abs(vector.z), 1f);
        }
    }

    class GrayscaleHeight implements ColorFunction {
        private final IHeightFunction delegate;
        private final Color color = new Color();

        public GrayscaleHeight(IHeightFunction delegate) {
            this.delegate = delegate;
        }

        @Override
        public Color calculate(Vector3 vector) {
            final float h = delegate.calculate(vector);
            color.set(h, h, h, 1f);
            return color;
        }
    }

    class GradientHeight implements ColorFunction {
        private final IHeightFunction delegate;
        private final ColorGradient gradient;

        public GradientHeight(IHeightFunction delegate) {
            this(delegate, ColorGradient.HABITABLE_PLANET);
        }

        public GradientHeight(IHeightFunction delegate, ColorGradient gradient) {
            this.gradient = gradient;
            this.delegate = delegate;
        }

        @Override
        public Color calculate(Vector3 vector) {
            final float h = delegate.calculate(vector);
            return gradient.pick(h);
        }
    }

}
