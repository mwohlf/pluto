package net.wohlfart.pluto.scene.properties;

import javax.annotation.Nonnull;

import net.wohlfart.pluto.scene.UpdateMethod;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasUpdateMethod implements Component, Poolable {

    @Nonnull
    private UpdateMethod updateMethod = UpdateMethod.NULL;

    @Override
    public void reset() {
        updateMethod = UpdateMethod.NULL;
    }

    public HasUpdateMethod withUpdateMethod(UpdateMethod updateMethod) {
        this.updateMethod = updateMethod;
        return this;
    }

    public void update(float delta) {
        updateMethod.update(delta);
    }

}
