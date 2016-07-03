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
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.lang.EntityProperty;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.scene.properties.IsSteerable;
import net.wohlfart.pluto.util.Utils;

@EntityElement(type = "Flee")
public class FleeBehavior extends AbstractBehaviorLeaf {

    private static final float TARGET_EPSILON = 0.1f;

    public final Vector3 tmpVector1 = new Vector3();
    public final Vector3 tmpVector2 = new Vector3();
    private final Position tmpPosition = new Position();
    private final Quaternion tmpQuaternion = new Quaternion();

    protected Entity target;

    protected float moveSpeed; // units per second
    protected float rotationSpeed; // units per second

    protected Vector3 forwardVector;
    protected Vector3 upVector;

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        assert target != null : "need a target";
        assert entity.getComponent(HasPosition.class) != null : "entity needs a position";
        assert entity.getComponent(HasRotation.class) != null : "entity needs a rotation";
        assert entity.getComponent(IsSteerable.class) != null : "entity needs to be steerable";
        assert !Float.isNaN(moveSpeed) || !Float.isNaN(rotationSpeed) : "need rotation or movement";
        forwardVector = entity.getComponent(IsSteerable.class).getForward();
        upVector = entity.getComponent(IsSteerable.class).getUp();
        moveSpeed = entity.getComponent(IsSteerable.class).getMoveSpeed();
        rotationSpeed = entity.getComponent(IsSteerable.class).getRotationSpeed();
        assert MathUtils.isEqual(new Vector3(forwardVector).dot(upVector), 0f, 0.001f);
        return new TaskImpl().initialize(entity, parent);
    }

    @EntityProperty(name = "target", type = "Entity")
    public FleeBehavior withTarget(Entity target) {
        assert target.getComponent(HasPosition.class) != null : "waypoint needs a position";
        this.target = target;
        return this;
    }

    public class TaskImpl extends AbstractLeafTask {

        @Override
        public void tick(float delta, SceneGraph graph) {
            target.getComponent(HasPosition.class).getPosition().get(tmpVector1);
            getEntity().getComponent(HasPosition.class).getPosition().get(tmpVector2);
            if (tmpVector1.epsilonEquals(tmpVector2, FleeBehavior.TARGET_EPSILON)) {
                getContext().remove(this);
                getParent().reportState(State.SUCCESS);
            } else {
                calculate(delta);
                getParent().reportState(State.RUNNING);
            }
        }

        private boolean calculate(float delta) {
            // tmpVector1 is a vector from current entity's position to the target's position in target space
            tmpPosition.set(0.0, 0.0, 0.0)
                    .sub(getEntity().getComponent(HasPosition.class).getPosition())
                    .add(target.getComponent(HasPosition.class).getPosition());
            tmpPosition.get(tmpVector1);
            tmpQuaternion.set(getEntity().getComponent(HasRotation.class).getRotation());
            tmpVector1.mul(tmpQuaternion.conjugate());
            tmpVector1.nor();

            // calculate the rotation to look at the target, collinear check to avoid rounding errors
            if (!tmpVector1.isCollinear(forwardVector, 0.00001f)) {
                tmpQuaternion.setFromCross(tmpVector1, forwardVector);
                //tmpQuaternion.nor();
                Utils.scale(tmpQuaternion, delta * rotationSpeed);
                getEntity().getComponent(HasRotation.class).getRotation().mul(tmpQuaternion.conjugate());
            }

            // we should use the forward direction here
            tmpPosition.get(tmpVector1); // reset
            tmpVector1.nor();
            tmpVector1.scl(delta * moveSpeed);
            getEntity().getComponent(HasPosition.class).getPosition().move(tmpVector1);

            return true;
        }

    }

}
