package net.wohlfart.pluto.ai.btree;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBehaviorNode<T extends AbstractBehaviorNode<T>> implements IBehavior<T> {

    protected final List<IBehavior<?>> children = new ArrayList<>();

    protected BehaviorContext context;

    @SuppressWarnings("unchecked")
    @Override
    public T withContext(BehaviorContext context) {
        this.context = context;
        for (final IBehavior<?> behavior : children) {
            behavior.withContext(context);
        }
        return (T) this;
    }

    @Override
    public <S extends IBehavior<S>> void addChild(IBehavior<S> behavior) {
        behavior.withContext(context);
        children.add(behavior);
    }

    @Override
    public <U extends IBehavior<U>> void removeChild(IBehavior<U> behavior) {
        children.remove(behavior);
    }

}
