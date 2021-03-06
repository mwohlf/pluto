package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;

public class SuccessBehavior extends AbstractBehaviorLeaf {

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    class TaskImpl extends AbstractLeafTask {

        @Override
        public void tick(float delta, SceneGraph graph) {
            assert getContext() != null;
            getContext().remove(this);
            getParent().reportState(State.SUCCESS);
        }

    }

}
