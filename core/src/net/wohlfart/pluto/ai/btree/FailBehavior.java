package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;

public class FailBehavior extends AbstractBehaviorLeaf<FailBehavior> {

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    class TaskImpl extends AbstractLeafTask {

        @Override
        public void tick(float delta, SceneGraph graph) {
            assert context != null;
            context.remove(this); // remove this task from the queue
            parent.reportState(State.FAILURE); // signal the parent task
        }

    }

}
