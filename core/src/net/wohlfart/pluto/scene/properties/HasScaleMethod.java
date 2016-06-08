package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import net.wohlfart.pluto.scene.ScaleMethod;
import net.wohlfart.pluto.scene.ScaleValue;

public class HasScaleMethod implements Component, Poolable {
    private ScaleMethod scaling;

    @Override
    public void reset() {
        scaling = null;
    }

    public HasScaleMethod withPositionScaling(ScaleValue value) {
        this.scaling = new ScaleMethod.PositionScaleMethod(value);
        return this;
    }

    public ScaleMethod getScaleMethod() {
        return scaling;
    }

}
