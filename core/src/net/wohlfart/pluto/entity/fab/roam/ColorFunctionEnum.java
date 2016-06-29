package net.wohlfart.pluto.entity.fab.roam;

import net.wohlfart.pluto.entity.fab.roam.IColorFunction.GradientHeight;
import net.wohlfart.pluto.entity.fab.roam.IColorFunction.GrayscaleHeight;
import net.wohlfart.pluto.util.ISupplier;

// TODO: reuse the functions since they are immutable
public enum ColorFunctionEnum implements ISupplier<IColorFunction> {
    ROCK1() {
        @Override
        public IColorFunction get() {
            return new GradientHeight(new SimplexIteration(6, 1f, 0.007f));
        }
    },
    ROCK2() {
        @Override
        public IColorFunction get() {
            return new GradientHeight(new SimplexIteration(6, 0.7f, 100f));
        }
    },
    ASTROID1() {
        @Override
        public IColorFunction get() {
            return new GrayscaleHeight(new SimplexIteration(4, 0.7f, 0.05f));
        }
    };

    @Override
    public abstract IColorFunction get();

}
