package net.wohlfart.pluto.ai.btree;

/**
 *
 */
public interface BehaviorTaskContext {

    void remove(ITask task);

    void add(ITask task);

    boolean contains(ITask task);
}
