package net.wohlfart.pluto.entity.fab.roam;

import net.wohlfart.pluto.entity.fab.roam.IHeightFunction.Range;
import net.wohlfart.pluto.util.ISupplier;

// TODO: reuse the fucntions since they are immutable
public enum HeightFunctionEnum implements ISupplier<IHeightFunction> {
    ROCK1() {
        @Override
        public IHeightFunction get() {
            return new SimplexIteration(6, 1f, 0.007f);
        }
    },
    ROCK2() {
        @Override
        public IHeightFunction get() {
            return new SimplexIteration(6, 0.7f, 100f);
        }
    },
    ASTROID1() {
        @Override
        public IHeightFunction get() {
            return new Range(1, 5, new SimplexIteration(4, 0.7f, 0.05f));
        }
    };

    @Override
    public abstract IHeightFunction get();

}
