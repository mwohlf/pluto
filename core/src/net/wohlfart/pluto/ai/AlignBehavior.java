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

@EntityElement(type = "Align")
public class AlignBehavior extends AbstractBehaviorLeaf {

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
        assert MathUtils.isEqual(new Vector3(forwardVector).dot(upVector), 0f, 0.001f);
        return new TaskImpl().initialize(this, entity, parent);
    }

    @EntityProperty(name = "target", type = "Entity")
    public AlignBehavior withTarget(Entity target) {
        assert target != null : "target cant be null";
        assert target.getComponent(HasPosition.class) != null : "target needs a position, target was: " + target;
        assert target.getComponent(HasRotation.class) != null : "target needs a rotation, target was: " + target;
        this.target = target;
        return this;
    }

    static class TaskImpl extends AbstractLeafTask {

        private AlignBehavior behavior;

        public ITask initialize(AlignBehavior behavior, Entity entity, ITask parent) {
            assert parent != null : "parent must not be null";
            this.behavior = behavior;
            return super.initialize(entity, parent);
        }

        @Override
        public void tick(float delta, SceneGraph graph) {
            behavior.target.getComponent(HasPosition.class).getPosition().get(behavior.tmpVector1);
            entity.getComponent(HasPosition.class).getPosition().get(SeekBehavior.tmpVector2);
            if (behavior.tmpVector1.epsilonEquals(behavior.tmpVector2, behavior.TARGET_EPSILON)) {
                behavior.context.remove(this);
                parent.reportState(State.SUCCESS);
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

            // calculate the rotation to look at the target, collinear check to avoid rounding errors
            if (!SeekBehavior.tmpVector1.isOnLine(behavior.forwardVector, 0.00001f)) {
                SeekBehavior.tmpQuaternion1.setFromCross(SeekBehavior.tmpVector1, behavior.forwardVector);
                Utils.scale(SeekBehavior.tmpQuaternion1, delta * behavior.rotationSpeed);
                entity.getComponent(HasRotation.class).getRotation().mul(SeekBehavior.tmpQuaternion1.conjugate());//.nor();
            }

            // calculate the rotation to match the up vector from the target
            tmpQuaternion1.set(entity.getComponent(HasRotation.class).getRotation()); // entity orientation
            //System.err.println("  behavior.forwardVector: " + behavior.forwardVector);
            //tmpQuaternion1.conjugate();
            //tmpQuaternion1.transform(tmpVector1.set(behavior.forwardVector)); // flight direction
            //System.err.println("  flight direction: " + tmpVector1.nor());

            tmpQuaternion2.set(behavior.target.getComponent(HasRotation.class).getRotation()); // target orientation
            //System.err.println("  tmpQuaternion2: " + tmpQuaternion2);
            tmpVector2.set(Utils.getYVector(tmpQuaternion2)).nor(); // target up vector
            // target
            //System.err.println("  tmpQuaternion2 (target x): " + Utils.getXVector(tmpQuaternion2));
            //System.err.println("  tmpQuaternion2 (target y): " + Utils.getYVector(tmpQuaternion2));
            //System.err.println("  tmpQuaternion2 (target z): " + Utils.getZVector(tmpQuaternion2));
            // entity
            //System.err.println("  tmpQuaternion1 (target x): " + Utils.getXVector(tmpQuaternion1));
            //System.err.println("  tmpQuaternion1 (target y): " + Utils.getYVector(tmpQuaternion1));
            //System.err.println("  tmpQuaternion1 (target z): " + Utils.getZVector(tmpQuaternion1));
            //System.err.println("    orthogonal check: " + Utils.getZVector(tmpQuaternion1).dot(vector));

            tmpVector1.set(behavior.forwardVector);
            tmpVector1.mul(tmpQuaternion1);
            //System.err.println("  tmpVector1 (forward): " + tmpVector1);

            if (!tmpVector2.isOnLine(tmpVector1, 0.001f)) {

                tmpVector1.set(behavior.upVector);
                tmpVector1.mul(tmpQuaternion1);
                //System.err.println("  tmpVector1 (up): " + tmpVector1);

                //System.err.println("  isOnLine: " + tmpVector2.isOnLine(tmpVector1, 0.001f));

                tmpVector1.nor();

                // do we need to rotate
                if (!tmpVector2.isCollinear(tmpVector1, 0.001f)) {

                    final float angle1 = tmpQuaternion1.getAngleAround(tmpVector1);
                    //System.err.println("     tmpQuaternion1: " + tmpQuaternion1 + " angle1: " + angle1 + " tmpVector1: " + tmpVector1);

                    final float angle2 = tmpQuaternion2.getAngleAround(tmpVector1);
                    //System.err.println("     tmpQuaternion2: " + tmpQuaternion2 + " angle2: " + angle2 + " tmpVector1: " + tmpVector1);

                    final float angle = (angle1 - angle2) * delta;

                    /*
                    System.err.println(""
                            + " angle1: " + angle1
                            + " angle2: " + angle2
                            + " angle: " + angle
                            + " tmpVector1: " + tmpVector1
                            + " tmpVector2: " + tmpVector2
                            + " tmpQuaternion1: " + tmpQuaternion1
                            + " tmpQuaternion2: " + tmpQuaternion2);
                    */
                    if (angle > 0.001) {
                        tmpQuaternion2.setFromAxis(1, 0, 0, angle);
                        entity.getComponent(HasRotation.class).getRotation().mul(tmpQuaternion2);
                    } else if (angle < -0.001) {
                        tmpQuaternion2.setFromAxis(-1, 0, 0, -angle);
                        entity.getComponent(HasRotation.class).getRotation().mul(tmpQuaternion2);
                    }
                }
            }

            SeekBehavior.tmpVector1.set(behavior.forwardVector);
            SeekBehavior.tmpQuaternion1.set(entity.getComponent(HasRotation.class).getRotation());
            SeekBehavior.tmpVector1.mul(SeekBehavior.tmpQuaternion1 /*.conjugate()*/);
            SeekBehavior.tmpVector1.scl(delta * behavior.moveSpeed);
            entity.getComponent(HasPosition.class).getPosition().move(SeekBehavior.tmpVector1);

            return true;
        }

    }

}
