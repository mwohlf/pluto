package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import net.wohlfart.pluto.ai.btree.AbstractBehaviorLeaf;
import net.wohlfart.pluto.ai.btree.ITask;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.scene.properties.IsSteerable;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.util.Utils;

/**
 * seek a target entity and follow
 * TODO: checkout
 * BSpline derivativeAt()
 */
// see: http://www.red3d.com/cwr/steer/gdc99/
@EntityElement(type = "Seek")
public class SeekBehavior extends AbstractBehaviorLeaf<SeekBehavior> {

    static final float TARGET_EPSILON = 1.0f;

    static final Vector3 tmpVector1 = new Vector3();

    static final Vector3 tmpVector2 = new Vector3();

    static final Position tmpPosition = new Position();

    static final Quaternion tmpQuaternion1 = new Quaternion();

    static final Quaternion tmpQuaternion2 = new Quaternion();

    private static final Pool<TaskImpl> POOL = new Pool<TaskImpl>(16, 1000) {
        @Override
        protected TaskImpl newObject() {
            return new TaskImpl();
        }
    };

    protected Entity target;

    protected float moveSpeed; // units per second
    protected float rotationSpeed; // units per second
    protected Vector3 forwardVector;
    protected Vector3 upVector;

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        //  assert target != null : "need a target";
        assert entity.getComponent(HasPosition.class) != null : "entity needs a position";
        assert entity.getComponent(HasRotation.class) != null : "entity needs a rotation";
        assert entity.getComponent(IsSteerable.class) != null : "entity needs to be steerable";
        forwardVector = entity.getComponent(IsSteerable.class).getForward();
        upVector = entity.getComponent(IsSteerable.class).getUp();
        moveSpeed = entity.getComponent(IsSteerable.class).getMoveSpeed();
        rotationSpeed = entity.getComponent(IsSteerable.class).getRotationSpeed();
        assert MathUtils.isEqual(new Vector3(forwardVector).dot(upVector), 0f, 0.001f);
        return SeekBehavior.POOL.obtain().initialize(this, entity, parent);
    }

    @EntityProperty(name = "waypoint", type = "Entity")
    public SeekBehavior withEntity(Entity target) {
        assert target != null : "target cant be null";
        assert target.getComponent(HasPosition.class) != null : "target needs a position, target was: " + target;
        assert target.getComponent(HasRotation.class) != null : "target needs a rotation, target was: " + target;
        this.target = target;
        return this;
    }

    static class TaskImpl extends AbstractLeafTask {

        private SeekBehavior behavior;

        public ITask initialize(SeekBehavior behavior, Entity entity, ITask parent) {
            assert parent != null : "parent must not be null";
            this.behavior = behavior;
            return super.initialize(entity, parent);
        }

        @Override
        public void tick(float delta, SceneGraph graph) {
            behavior.target.getComponent(HasPosition.class).getPosition().get(SeekBehavior.tmpVector1);
            entity.getComponent(HasPosition.class).getPosition().get(SeekBehavior.tmpVector2);
            if (SeekBehavior.tmpVector1.epsilonEquals(SeekBehavior.tmpVector2, SeekBehavior.TARGET_EPSILON)) {
                behavior.context.remove(this);
                parent.reportState(State.SUCCESS);
                SeekBehavior.POOL.free(this); // resets the current task
            } else {
                calculate(delta);
                parent.reportState(State.RUNNING);
            }
        }

        private boolean calculate(float delta) {
            // tmpVector1 is a vector from current entity's position to the target's position in target space
            SeekBehavior.tmpPosition.set(0.0, 0.0, 0.0)
                    .sub(entity.getComponent(HasPosition.class).getPosition())
                    .add(behavior.target.getComponent(HasPosition.class).getPosition());
            SeekBehavior.tmpPosition.get(SeekBehavior.tmpVector1);
            SeekBehavior.tmpQuaternion1.set(entity.getComponent(HasRotation.class).getRotation());
            SeekBehavior.tmpVector1.mul(SeekBehavior.tmpQuaternion1.conjugate());
            SeekBehavior.tmpVector1.nor();

            // calculate the rotation to look at the target, colinear check to avoid rounding errors
            if (!SeekBehavior.tmpVector1.isCollinear(behavior.forwardVector, 0.00001f)) {
                SeekBehavior.tmpQuaternion1.setFromCross(SeekBehavior.tmpVector1, behavior.forwardVector);
                Utils.scale(SeekBehavior.tmpQuaternion1, delta * behavior.rotationSpeed);
                entity.getComponent(HasRotation.class).getRotation().mul(SeekBehavior.tmpQuaternion1.conjugate());
            }

            // calculate the rotation to match the up vector from the targte
            tmpQuaternion1.set(entity.getComponent(HasRotation.class).getRotation()); // entity orientation
            tmpQuaternion2.set(behavior.target.getComponent(HasRotation.class).getRotation()); // target orientation
            tmpVector1.set(Utils.getForwardVector(tmpQuaternion1)).nor(); // flight direction
            tmpVector2.set(Utils.getUpVector(tmpQuaternion2)).nor(); // target up vector
            if (!SeekBehavior.tmpVector2.isCollinear(tmpVector1, 0.001f)) {
                //System.out.println("target: " + tmpQuaternion2);
                //System.out.println("entityForward: " + tmpVector1);
                //tmpVector1.set(Utils.getUpVector(tmpQuaternion1)); // entity up vector
                final float angle1 = tmpQuaternion2.getAngleAround(tmpVector1);
                //System.out.println("angle1: " + angle1);

                final float angle2 = tmpQuaternion1.getAngleAround(tmpVector1);
                final float angle = 0.7f; //(angle2 - angle1) * 0.5f;
                tmpQuaternion2.set(Vector3.X, angle);
                entity.getComponent(HasRotation.class).getRotation().mul(SeekBehavior.tmpQuaternion2);
            }

            SeekBehavior.tmpVector1.set(behavior.forwardVector);
            //SeekBehavior.tmpVector1.scl(-1);
            SeekBehavior.tmpQuaternion1.set(entity.getComponent(HasRotation.class).getRotation());
            SeekBehavior.tmpVector1.mul(SeekBehavior.tmpQuaternion1 /*.conjugate()*/);
            SeekBehavior.tmpVector1.scl(delta * behavior.moveSpeed);
            entity.getComponent(HasPosition.class).getPosition().move(SeekBehavior.tmpVector1);

            return true;
        }

    }

}
