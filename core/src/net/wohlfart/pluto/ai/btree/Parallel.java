package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.ITask.AbstractNodeTask;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.lang.EntityElement;

@EntityElement(type = "Parallel")
public class Parallel extends AbstractBehaviorNode {

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    private class TaskImpl extends AbstractNodeTask {

        protected State state = State.INVALID;

        @Override
        public ITask initialize(Entity entity, ITask parent) {
            super.initialize(entity, parent);
            this.state = State.RUNNING;
            return this;
        }

        // called from child with the child's state
        // TODO: fix the behavior
        @Override
        public void reportState(State state) {
            switch (state) {
                case RUNNING: // child task is still running, no changes
                case SUCCESS: // child state finished, need to execute parent(this) in next tick
                    this.state = State.RUNNING;
                    break;
                case FAILURE: // child state failed, need to execute parent(this) in next tick
                    this.state = State.FAILURE;
                    break;
                default:
                    // not sure how this could happen
                    this.state = State.INVALID;
                    context.add(this);
            }
        }

        @Override
        public void tick(float delta, SceneGraph graph) {
            assert context.contains(this) : "task not in executor list";
            context.remove(this); // execution happens in the subtask
            switch (this.state) {
                case RUNNING:
                    runAllChildren(delta);
                    break;
                case FAILURE:
                    getParent().reportState(State.FAILURE);
                    break;
                default:
                    getParent().reportState(State.INVALID);
            }
        }

        private void runAllChildren(float delta) {
            for (final IBehavior behavior : children) {
                context.add(behavior.createTask(getEntity(), this));
            }
        }

    }

}
