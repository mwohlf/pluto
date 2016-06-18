package net.wohlfart.pluto.ai.btree;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.IBehavior.State;
import net.wohlfart.pluto.scene.SceneGraph;

/**
 * contains all running tasks and shared state information of the tasks
 */
public class BehaviorExecutor implements BehaviorContext { // TODO: use the interface

    private final List<ITask> runningTasks = new CopyOnWriteArrayList<>();

    private final Collection<Entity> entityBlacklist = new CopyOnWriteArrayList<>();

    private final NullTask NULL_TASK = new NullTask();

    public BehaviorExecutor attachBehavior(Entity entity, IBehavior behavior) {
        return new BehaviorBuilder(behavior).build(entity);
    }

    public void removeBehavior(Entity entity) {
        entityBlacklist.add(entity);
    }

    // called by the render thread from GraphStage, this ticks every active task in the queue
    public void tick(float delta, SceneGraph graph) {
        if (NULL_TASK.state == State.INVALID) {
            throw new IllegalStateException("executor state is invalid, one of the tasks was invalid");
        }
        for (final ITask runningTask : runningTasks) {
            if (entityBlacklist.contains(runningTask.getEntity())) {
                runningTasks.remove(runningTask);
            } else {
                runningTask.tick(delta, graph);
            }
        }
    }

    public State getLastState() {
        return NULL_TASK.state;
    }

    // called by a behavior when a tasked ended
    @Override
    public void remove(ITask task) {
        runningTasks.remove(task);
    }

    // called by a behavior when a new task was created
    @Override
    public void add(ITask task) {
        runningTasks.add(task);
    }

    public int getSize() {
        return runningTasks.size();
    }

    protected ITask getAtPosition(int pos) {
        return runningTasks.get(pos);
    }

    @Override
    public boolean contains(ITask task) {
        return runningTasks.contains(task);
    }

    private final class BehaviorBuilder {

        private final IBehavior rootBehavior;

        private BehaviorBuilder(IBehavior behavior) {
            this.rootBehavior = behavior;
        }

        private BehaviorExecutor build(Entity entity) {
            // add the created task, NULL_TASK will not be in the executor queue
            // but will receive the state from the created task
            NULL_TASK.state = State.RUNNING;
            add(rootBehavior.withContext(BehaviorExecutor.this).createTask(entity, NULL_TASK));
            return BehaviorExecutor.this;
        }

    }

    private static class NullTask implements ITask {

        private State state = State.SUCCESS;

        // callback from a top level child tasks, not sure if we need the state here
        @Override
        public void reportState(State state) {
            this.state = state;
        }

        @Override
        public void tick(float delta, SceneGraph graph) {
            throw new IllegalArgumentException("executor task should not be ticked");
        }

        @Override
        public Entity getEntity() {
            throw new IllegalArgumentException("executor task should not be assigned to any entity");
        }

        @Override
        public ITask initialize(Entity entity, ITask parent) {
            throw new IllegalArgumentException("no parent allowed for executor task");
        }

        @Override
        public void reset() {
            throw new IllegalArgumentException("executor task should not be reset");
        }
    }

}
