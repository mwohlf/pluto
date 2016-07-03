package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.btree.AbstractBehaviorLeaf;
import net.wohlfart.pluto.ai.btree.ITask;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.lang.EntityProperty;
import net.wohlfart.pluto.scene.properties.HasPosition;

/**
 *
 */
@EntityElement(type = "MoveTo")
public class MoveToBehavior extends AbstractBehaviorLeaf {

    private static final float TARGET_EPSILON = 0.1f;

    public final Vector3 tmpVector1 = new Vector3();
    public final Vector3 tmpVector2 = new Vector3();

    private volatile Entity target;

    private volatile float speed = Float.NaN; //

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        assert !Float.isNaN(this.speed) : "no speed at: " + System.identityHashCode(this);
        assert this.target != null : "no target at: " + System.identityHashCode(this);
        return new TaskImpl().initialize(entity, parent);
    }

    @EntityProperty(name = "target", type = "Entity")
    public MoveToBehavior withTarget(Entity target) {
        this.target = target;
        return this;
    }

    @EntityProperty(name = "speed", type = "Float")
    public MoveToBehavior withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public String toString() {
        return "MoveToBehavior [target=" + target + ", "
                + "speed=" + speed + ", "
                + "target=" + target + ", "
                + "hashcode=" + System.identityHashCode(this)
                + "]";
    }

    class TaskImpl extends AbstractLeafTask {

        @Override
        public void tick(float delta, SceneGraph graph) {
            target.getComponent(HasPosition.class).getPosition().get(tmpVector1);
            getEntity().getComponent(HasPosition.class).getPosition().get(tmpVector2);
            if (tmpVector1.epsilonEquals(tmpVector2, MoveToBehavior.TARGET_EPSILON)) {
                getContext().remove(this);
                getParent().reportState(State.SUCCESS);
            } else {
                calculate(delta);
                getParent().reportState(State.RUNNING);
            }
        }

        private void calculate(float delta) {
            target.getComponent(HasPosition.class).getPosition().get(tmpVector1);
            getEntity().getComponent(HasPosition.class).getPosition().get(tmpVector2);
            // calculate delta
            tmpVector1.sub(tmpVector2).nor().scl(delta * speed);
            // add to current position
            getEntity().getComponent(HasPosition.class).getPosition().move(tmpVector1);
        }

    }

}
