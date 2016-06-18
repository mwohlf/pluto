package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;

public class TriggerBehavior extends AbstractBehaviorLeaf {

    private final StateHolder stateHolder;

    public TriggerBehavior(StateHolder stateHolder) {
        this.stateHolder = stateHolder;
    }

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        return new TaskImpl().initialize(entity, parent);
    }

    class TaskImpl extends AbstractLeafTask {

        // prototype constructor
        TaskImpl() {
            this(null);
        }

        // clone constructor
        private TaskImpl(ITask parentTask) {
            initialize(null, parentTask);
        }

        @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH", justification = "I know what I am doing")
        @Override
        public void tick(float delta, SceneGraph graph) {
            assert context != null;
            final State state = stateHolder.getState();
            switch (state) {
                default:
                    context.remove(this);
                case RUNNING:
                    parent.reportState(state);
            }
        }

    }

    public interface StateHolder {

        State getState();

    }

}
