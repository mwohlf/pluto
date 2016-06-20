package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;

@EntityElement(type = "Delay")
public class Delay extends AbstractBehaviorLeaf {

    float waitTime;

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    @EntityProperty(name = "timeout", type = "Float")
    public Delay withTimeout(float waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    class TaskImpl extends AbstractLeafTask {
        private float time = 0;

        @Override
        public void tick(float delta, SceneGraph graph) {
            assert context != null;
            time += delta;
            if (time <= waitTime) {
                parent.reportState(State.RUNNING); // signal the parent task
            } else {
                context.remove(this); // remove this task from the queue
                parent.reportState(State.SUCCESS); // signal the parent task
            }
        }

    }

}
