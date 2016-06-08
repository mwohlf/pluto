package net.wohlfart.pluto.ai.btree;

public abstract class AbstractBehaviorLeaf<T extends AbstractBehaviorLeaf<T>> implements IBehavior<T> {

    protected BehaviorContext context;

    @SuppressWarnings("unchecked")
    @Override
    public T withContext(BehaviorContext context) {
        this.context = context;
        return (T) this;
    }

    @Override
    public <S extends IBehavior<S>> void addChild(IBehavior<S> behavior) {
        throw new IllegalArgumentException("can't add child");
    }

    @Override
    public <U extends IBehavior<U>> void removeChild(IBehavior<U> behavior) {
        throw new IllegalArgumentException("can't remove child");
    }

}
