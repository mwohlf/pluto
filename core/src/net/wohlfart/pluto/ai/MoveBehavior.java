package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.btree.AbstractBehaviorLeaf;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.ai.btree.ITask;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.lang.EntityProperty;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;

/**
 * move an entity for a fixed distance
 */
@EntityElement(type = "Move")
public class MoveBehavior extends AbstractBehaviorLeaf {

    public final Vector3 tmpVector = new Vector3();

    private Vector3 direction;

    private float speed = Float.NaN;

    private Vector3 movePerSec;

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    public MoveBehavior withMove(Vector3 movePerSec) {
        this.movePerSec.set(movePerSec);
        return this;
    }

    @EntityProperty(name = "direction", type = "Vector3")
    public MoveBehavior withDirection(Vector3 direction) {
        this.direction = direction;
        update();
        return this;
    }

    @EntityProperty(name = "speed", type = "Float")
    public MoveBehavior withSpeed(float speed) {
        this.speed = speed;
        update();
        return this;
    }

    private void update() {
        if (this.direction != null && !Float.isNaN(this.speed)) {
            movePerSec = new Vector3(direction).nor().scl(speed);
        }
    }

    @Override
    public void addChild(IBehavior behavior) {
        throw new IllegalArgumentException("can't add child");
    }

    class TaskImpl extends AbstractLeafTask {

        @Override
        public void tick(float delta, SceneGraph graph) {
            calculate(delta);
            getParent().reportState(State.RUNNING);
        }

        private void calculate(float deltaTime) {
            tmpVector.set(movePerSec).scl(deltaTime);
            // transform the velocity from object space
            getEntity().getComponent(HasRotation.class).getRotation().transform(tmpVector);
            // add the transformed velocity to the position
            getEntity().getComponent(HasPosition.class).getPosition().move(tmpVector);
        }

    }

}
