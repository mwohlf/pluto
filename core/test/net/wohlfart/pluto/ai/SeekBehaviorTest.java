package net.wohlfart.pluto.ai;

import org.junit.Test;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Quaternion;

import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;

public class SeekBehaviorTest {

    @Test
    public void smokeTest() {

        final SeekBehavior seekBehavior = new SeekBehavior();

        final PooledEngine entityPool = new PooledEngine();
        final Entity entity = new Entity();

        entity.add(entityPool.createComponent(HasPosition.class)
                .withPosition(100, 100, 100));
        entity.add(entityPool.createComponent(HasRotation.class)
                .withRotation(new Quaternion()));

        seekBehavior.withEntity(entity);

        // TODO
    }
}
