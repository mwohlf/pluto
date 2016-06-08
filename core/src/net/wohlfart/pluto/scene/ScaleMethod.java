package net.wohlfart.pluto.scene;

public interface ScaleMethod {

    void apply(Position value);

    class PositionScaleMethod implements ScaleMethod {

        private final ScaleValue value;

        public PositionScaleMethod(ScaleValue value) {
            this.value = value;
        }

        @Override
        public void apply(Position value) {
            this.value.set(value.floatDist2());
        }

    }

}
