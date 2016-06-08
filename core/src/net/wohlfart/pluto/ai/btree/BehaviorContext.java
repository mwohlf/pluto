package net.wohlfart.pluto.ai.btree;

/**
 *
 */
public interface BehaviorContext {

    void remove(ITask task);

    void add(ITask task);

    boolean contains(ITask task);
}
