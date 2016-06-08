package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasUid implements Component, Poolable {

    private long uid;

    @Override
    public void reset() {
        withUid(Long.MIN_VALUE);
    }

    public long getUid() {
        return uid;
    }

    public HasUid withUid(long uid) {
        this.uid = uid;
        return this;
    }

}
