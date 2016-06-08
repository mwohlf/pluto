package net.wohlfart.pluto.entity;

import javax.annotation.Nonnull;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.properties.HasCamera;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasScaleMethod;

public class ScaleSystem extends EntitySystem {
    private static final ImmutableArray<Entity> EMPTY = new ImmutableArray<>(new Array<>());

    @Nonnull
    private ImmutableArray<Entity> withScaledVersions = ScaleSystem.EMPTY;

    @Override
    public void addedToEngine(Engine engine) {
        //noinspection unchecked
        withScaledVersions = engine.getEntitiesFor(Family
                .all(HasPosition.class, HasScaleMethod.class)
                .exclude(HasCamera.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (final Entity entity : withScaledVersions) {
            final Position position = entity.getComponent(HasPosition.class).getPosition();
            entity.getComponent(HasScaleMethod.class).getScaleMethod().apply(position);
        }
    }

}
