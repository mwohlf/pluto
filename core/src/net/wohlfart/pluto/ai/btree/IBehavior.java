package net.wohlfart.pluto.ai.btree;

import com.badlogic.ashley.core.Entity;

/**
 * static part of a behavior tree,
 * implementations contain definition data
 * that do not change at runtime
 */
public interface IBehavior<T extends IBehavior<T>> {

    enum State {
        INVALID,
        RUNNING,
        SUCCESS,
        FAILURE,
    }

    // callers are responsible for putting the returned task in the executor
    // the parent needs not to be in the executor queue
    ITask createTask(Entity entity, ITask parent);

    // executor for running tasks
    T withContext(BehaviorContext behaviorExecutor);

    <S extends IBehavior<S>> void addChild(IBehavior<S> behavior);

    <U extends IBehavior<U>> void removeChild(IBehavior<U> behavior);

}
