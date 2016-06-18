package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import net.wohlfart.pluto.ai.btree.IBehavior;

public class HasBehavior implements Component, Poolable {
    private IBehavior behavior;

    @Override
    public void reset() {
        behavior = null;
    }

    public HasBehavior withBehavior(IBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    public IBehavior getBehavior() {
        return behavior;
    }

}
