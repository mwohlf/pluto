package net.wohlfart.pluto.entity;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.wohlfart.pluto.Logging;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.ScaleValue;
import net.wohlfart.pluto.scene.properties.HasBehavior;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.scene.properties.HasScaleMethod;
import net.wohlfart.pluto.scene.properties.HasUid;
import net.wohlfart.pluto.scene.properties.IsPickable;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.util.Utils;

/**
 * factory for common entity components position, rotation and their velocities
 *
 * <F> the factory (sub-)type the parameters for the entity being produced
 */
@SuppressWarnings({ "unchecked", "ClassWithTooManyMethods" })
public abstract class AbstractEntityCommand<F extends AbstractEntityCommand<F>> implements IEntityCommand {

    public final Position position = new Position();

    public final Quaternion rotation = new Quaternion(Vector3.X, 0);

    public final ScaleValue scale = new ScaleValue();

    public IBehavior behavior;

    private long uid = NULL_UID;

    /*
    protected F reset() {
        reset(position);
        reset(rotation);
        return (F) this;
    }
    */

    @Override
    public long getUid() {
        // assert this.uid != IEntityCommand.NULL_UID : "uid is invalid";
        return uid;
    }

    @EntityProperty(name = "uid", type = "Long")
    public F withUid(long uid) {
        this.uid = uid;
        return (F) this;
    }

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert Utils.isRenderThread() : "<runNow> should run on the render thread, default implementation is empty";
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert !Utils.isRenderThread() : "<runAsync> should run off the render thread, default implementation is empty";
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert Utils.isRenderThread() : "<runSync> should run on the render thread, default implementation is empty";
        futureEntity.set(IEntityCommand.NULL_ENTITY);
    }

    protected boolean isEmpty(Vector3 v) {
        return Float.isNaN(v.x) || Float.isNaN(v.y) || Float.isNaN(v.z);
    }

    protected boolean isEmpty(Quaternion q) {
        return Float.isNaN(q.x) || Float.isNaN(q.y) || Float.isNaN(q.z) || Float.isNaN(q.w);
    }

    protected boolean isEmpty(Position p) {
        return Double.isNaN(p.x) || Double.isNaN(p.y) || Double.isNaN(p.z);
    }

    protected boolean isEmpty(Color c) {
        return Float.isNaN(c.r) || Float.isNaN(c.g) || Float.isNaN(c.b) || Float.isNaN(c.a);
    }

    protected void reset(Vector3 v) {
        v.set(Float.NaN, Float.NaN, Float.NaN);
    }

    protected void reset(Quaternion q) {
        q.set(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    protected void reset(Position p) {
        p.set(Double.NaN, Double.NaN, Double.NaN);
    }

    protected void reset(Color c) {
        c.set(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    /**
     * create an entity that has forward directed velocity together with angular
     * velocity, position and rotation this method might run outside the
     * rendering thread
     */
    public Entity create(EntityPool entityPool) {
        final Entity entity = entityPool.createEntity();

        if (behavior != null) {
            entity.add(entityPool.createComponent(HasBehavior.class)
                    .withBehavior(behavior));
        }

        if (uid != NULL_UID) { // TODO
            entity.add(entityPool.createComponent(HasUid.class)
                    .withUid(uid));
        }

        // we need at least rotation...
        if (isEmpty(rotation)) {
            entity.add(entityPool.createComponent(HasRotation.class)
                    .withRotation(MovementSystem.NULL_ROTATION));
        } else {
            entity.add(entityPool.createComponent(HasRotation.class)
                    .withRotation(rotation));
        }

        // ..and position to calculate the transform matrix
        if (isEmpty(position)) {
            entity.add(entityPool.createComponent(HasPosition.class)
                    .withPosition(MovementSystem.NULL_POSITION));
        } else {
            entity.add(entityPool.createComponent(HasPosition.class)
                    .withPosition(position));
        }

        entity.add(entityPool.createComponent(HasScaleMethod.class)
                .withPositionScaling(scale));

        return entity;
    }

    @EntityProperty(name = "position", type = "Position")
    public F withPosition(Position position) {
        this.position.set(position.x, position.y, position.z);
        return (F) this;
    }

    public F withPosition(Vector3 position) {
        this.position.set(position.x, position.y, position.z);
        return (F) this;
    }

    public F withPosition(int x, int y, int z) {
        this.position.set(x, y, z);
        return (F) this;
    }

    public F withPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        return (F) this;
    }

    @EntityProperty(name = "rotation", type = "Quaternion")
    public F withRotation(Quaternion rotation) {
        //System.err.println(" setting rotation: " + rotation);
        this.rotation.set(rotation);
        return (F) this;
    }

    @EntityProperty(name = "behavior", type = "Behavior")
    public F withBehavior(IBehavior behavior) {
        this.behavior = behavior;
        return (F) this;
    }

    protected void makePickable(EntityPool entityPool, final ModelInstance element, final Entity entity) {
        element.calculateTransforms();
        final IsPickable pickable = entityPool.createComponent(IsPickable.class);
        final float range = calculateRange(element);
        entity.add(pickable
                .withTransform(element.transform)
                .withPickRange(range));
        Logging.ROOT.debug("<makePickable> range: " + range);
    }

    // TODO:
    private float calculateRange(ModelInstance element) {
        final BoundingBox boundingBox = element.calculateBoundingBox(new BoundingBox());
        float result = 0;
        result = Math.max(result, boundingBox.max.len());
        result = Math.max(result, boundingBox.min.len());
        return result;
    }

}
