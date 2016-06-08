package net.wohlfart.pluto.entity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.IsPickable;

public class PickSystem extends EntitySystem { // todo: implement pick facade

    private final Vector3 positionVector = new Vector3();

    private final Vector3 intersectionVector = new Vector3();

    private ImmutableArray<Entity> pickable = new ImmutableArray<>(new Array<>(0));

    private Entity currentPick = EntityPool.NULL;

    @Override
    public void addedToEngine(Engine engine) {
        //noinspection unchecked
        pickable = engine.getEntitiesFor(Family.all(HasPosition.class, IsPickable.class).get());
    }

    public Entity pick(Ray pickRay) {
        Entity nextPick = EntityPool.NULL;
        float currentDistance = Float.POSITIVE_INFINITY;
        for (final Entity elem : pickable) {
            elem.getComponent(HasPosition.class).getPosition().get(positionVector);
            final IsPickable element = elem.getComponent(IsPickable.class);
            final float radius = element.getPickRange();
            // quick check
            if (!Intersector.intersectRaySphere(pickRay, positionVector, radius, intersectionVector)) {
                continue;
            }
            // TODO: calc bounding box and intersect
            final float dist2elem = pickRay.origin.dst2(positionVector);
            if (Float.compare(dist2elem, currentDistance) < 0) {
                nextPick = elem;
                currentDistance = dist2elem;
            }
        }
        return nextPick;
    }

    public void select(Entity nextPick) {
        this.currentPick = nextPick;
    }

    public Entity getCurrentPick() {
        return currentPick;
    }

}
