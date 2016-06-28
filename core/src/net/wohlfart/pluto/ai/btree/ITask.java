package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.btree.IBehavior.State;
import net.wohlfart.pluto.scene.SceneGraph;

/**
 * runtime data for a behavior tree
 */
public interface ITask {

    Entity getEntity();

    ITask getParent();

    // runtime parent relationship, also initialized the task
    ITask initialize(Entity entity, ITask parent);

    // perform the action, report state to parent
    void tick(float delta, SceneGraph graph);

    // called by a child to change parent's state information
    // child is supposed to destroy/remove itself when calling this method with SUCCESS or FAILURE
    void reportState(State state);

    abstract class AbstractNodeTask implements ITask {

        // the entity to which the behavior is applied to
        private Entity entity;

        // parent task that gets reported this tasks events
        private ITask parent;

        @Override
        public Entity getEntity() {
            return entity;
        }

        @Override
        public ITask getParent() {
            return parent;
        }

        @Override
        public ITask initialize(Entity entity, ITask parent) {
            this.entity = entity;
            this.parent = parent;
            return this;
        }

        @Override
        public abstract void reportState(State state);

        @Override
        public abstract void tick(float delta, SceneGraph graph);

    }

    abstract class AbstractLeafTask implements ITask {

        // the entity to which the behavior is applied to
        private Entity entity;

        // parent task that gets reported this tasks events
        private ITask parent;

        @Override
        public Entity getEntity() {
            return entity;
        }

        @Override
        public ITask getParent() {
            return parent;
        }

        @Override
        public ITask initialize(Entity entity, ITask parent) {
            this.entity = entity;
            this.parent = parent;
            return this;
        }

        @Override
        public void reportState(State state) {
            throw new IllegalArgumentException("this is a leaf task, no child state expected");
        }

        // implement task behavior in child classes
        @Override
        public abstract void tick(float delta, SceneGraph graph);

    }
}
