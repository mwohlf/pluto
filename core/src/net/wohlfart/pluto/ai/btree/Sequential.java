package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.ITask.AbstractNodeTask;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.lang.EntityElement;

/*
 * execute each child behavior one by one
 *
 * this behavior returns when a sub behavior returns fail
 */
@EntityElement(type = "Sequential")
public class Sequential extends AbstractBehaviorNode {

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    class TaskImpl extends AbstractNodeTask {

        protected State state = State.INVALID;

        protected int nextPos;

        @Override
        public ITask initialize(Entity entity, ITask parent) {
            super.initialize(entity, parent);
            this.state = State.RUNNING;
            this.nextPos = 0;
            return this;
        }

        // called from child's behavior with the child's state, the child must already be removed
        @Override
        public void reportState(State state) {
            switch (state) {
                case RUNNING: // child task is still running, no changes
                    this.state = State.RUNNING;
                    break;
                case SUCCESS: // child state finished, need to execute parent(this) in next tick
                    this.state = State.RUNNING;
                    context.add(this); // next tick will pick the next subtask
                    break;
                case FAILURE: // child state failed, need to execute parent(this) in next tick
                    this.state = State.FAILURE;
                    context.add(this); // next tick will signal failure
                    break;
                default:
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
                    createNext(delta);
                    break;
                case FAILURE:
                    getParent().reportState(State.FAILURE);
                    break;
                default:
                    getParent().reportState(this.state = State.INVALID);
            }
        }

        protected void createNext(float delta) {
            if (nextPos >= children.size()) {
                // we are done
                getParent().reportState(this.state = State.SUCCESS);
            } else {
                context.add(children.get(nextPos).createTask(getEntity(), this));
                getParent().reportState(state = State.RUNNING);
                nextPos++;
            }
        }

    }

}
