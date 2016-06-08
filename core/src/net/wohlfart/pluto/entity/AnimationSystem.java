package net.wohlfart.pluto.entity;

import net.wohlfart.pluto.scene.properties.HasUpdateMethod;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

public class AnimationSystem extends EntitySystem {

    private ImmutableArray<Entity> withUpdateMethods = new ImmutableArray<>(new Array<>(0));

    @Override
    public void addedToEngine(Engine engine) {
        //noinspection unchecked
        withUpdateMethods = engine.getEntitiesFor(Family.all(HasUpdateMethod.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (final Entity entity : withUpdateMethods) {
            entity.getComponent(HasUpdateMethod.class).update(deltaTime);
        }
    }

}
