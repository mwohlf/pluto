package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

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
public class SeekBehavior extends AbstractBehaviorLeaf {

    static final float TARGET_EPSILON = 1.0f;

    static final Vector3 tmpVector1 = new Vector3();

    static final Vector3 tmpVector2 = new Vector3();

    static final Position tmpPosition = new Position();

    static final Quaternion tmpQuaternion1 = new Quaternion();

    static final Quaternion tmpQuaternion2 = new Quaternion();

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
        assert rotationSpeed != 0 : "rotationSpeed is zero";
        assert moveSpeed != 0 : "moveSpeed is zero";
        assert MathUtils.isEqual(new Vector3(forwardVector).dot(upVector), 0f, 0.001f);
        return new TaskImpl().initialize(entity, parent);
    }

    @EntityProperty(name = "target", type = "Entity")
    public SeekBehavior withTarget(Entity target) {
        assert target != null : "target cant be null";
        assert target.getComponent(HasPosition.class) != null : "target needs a position, target was: " + target;
        assert target.getComponent(HasRotation.class) != null : "target needs a rotation, target was: " + target;
        this.target = target;
        return this;
    }

    class TaskImpl extends AbstractLeafTask {

        @Override
        public void tick(float delta, SceneGraph graph) {
            target.getComponent(HasPosition.class).getPosition().get(SeekBehavior.tmpVector1);
            getEntity().getComponent(HasPosition.class).getPosition().get(SeekBehavior.tmpVector2);
            if (SeekBehavior.tmpVector1.epsilonEquals(SeekBehavior.tmpVector2, SeekBehavior.TARGET_EPSILON)) {
                getContext().remove(this);
                getParent().reportState(State.SUCCESS);
            } else {
                calculate(delta);
                getParent().reportState(State.RUNNING);
            }
        }

        private boolean calculate(float delta) {
            // tmpVector1 is a vector from current entity's position to the target's position in target space
            SeekBehavior.tmpPosition.set(0.0, 0.0, 0.0)
                    .sub(getEntity().getComponent(HasPosition.class).getPosition())
                    .add(target.getComponent(HasPosition.class).getPosition());
            SeekBehavior.tmpPosition.get(SeekBehavior.tmpVector1);
            SeekBehavior.tmpQuaternion1.set(getEntity().getComponent(HasRotation.class).getRotation());
            SeekBehavior.tmpVector1.mul(SeekBehavior.tmpQuaternion1.conjugate());
            SeekBehavior.tmpVector1.nor();

            // calculate the rotation to look at the target, collinear check to avoid rounding errors
            if (!SeekBehavior.tmpVector1.isOnLine(forwardVector, 0.00001f)) {
                SeekBehavior.tmpQuaternion1.setFromCross(SeekBehavior.tmpVector1, forwardVector);
                Utils.scale(SeekBehavior.tmpQuaternion1, delta * rotationSpeed);
                getEntity().getComponent(HasRotation.class).getRotation().mul(SeekBehavior.tmpQuaternion1.conjugate());//.nor();
            }

            // calculate the rotation to match the up vector from the target
            tmpQuaternion1.set(getEntity().getComponent(HasRotation.class).getRotation()); // entity orientation
            //System.err.println("  behavior.forwardVector: " + behavior.forwardVector);
            //tmpQuaternion1.conjugate();
            //tmpQuaternion1.transform(tmpVector1.set(behavior.forwardVector)); // flight direction
            //System.err.println("  flight direction: " + tmpVector1.nor());

            tmpQuaternion2.set(target.getComponent(HasRotation.class).getRotation()); // target orientation
            //System.err.println("  tmpQuaternion2: " + tmpQuaternion2);
            tmpVector2.set(Utils.getYVector(tmpQuaternion2)).nor(); // target up vector

            tmpVector1.set(forwardVector);
            tmpVector1.mul(tmpQuaternion1);
            //System.err.println("  tmpVector1 (forward): " + tmpVector1);

            SeekBehavior.tmpVector1.set(forwardVector);
            SeekBehavior.tmpQuaternion1.set(getEntity().getComponent(HasRotation.class).getRotation());
            SeekBehavior.tmpVector1.mul(SeekBehavior.tmpQuaternion1 /*.conjugate()*/);
            SeekBehavior.tmpVector1.scl(delta * moveSpeed);
            getEntity().getComponent(HasPosition.class).getPosition().move(SeekBehavior.tmpVector1);

            return true;
        }

    }

}
