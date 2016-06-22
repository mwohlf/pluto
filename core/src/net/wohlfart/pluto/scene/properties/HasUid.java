package net.wohlfart.pluto.scene.properties;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasUid implements Component, Poolable {

    public static long NULL_UID = 0;

    public static long INITIAL_BEHAVIOR_UID = 1_000_000;

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

    public static long next() {
        return INITIAL_BEHAVIOR_UID++;
    }

}
