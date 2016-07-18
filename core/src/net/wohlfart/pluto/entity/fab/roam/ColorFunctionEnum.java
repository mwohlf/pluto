package net.wohlfart.pluto.entity.fab.roam;

import com.badlogic.gdx.graphics.Color;

import net.wohlfart.pluto.entity.fab.roam.IColorFunction.Const;
import net.wohlfart.pluto.entity.fab.roam.IColorFunction.GradientHeight;
import net.wohlfart.pluto.entity.fab.roam.IColorFunction.GrayscaleHeight;
import net.wohlfart.pluto.entity.fab.roam.IColorFunction.VectorColor;
import net.wohlfart.pluto.util.ISupplier;

// TODO: reuse the functions since they are immutable
public enum ColorFunctionEnum implements ISupplier<IColorFunction> {
    BLUE() {
        @Override
        public IColorFunction get() {
            return new Const(Color.BLUE);
        }
    },
    VECTOR() {
        @Override
        public IColorFunction get() {
            return new VectorColor();
        }
    },
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
            return new GrayscaleHeight(new SimplexIteration(2, 0.7f, 0.5f));
        }
    };

    @Override
    public abstract IColorFunction get();

}
