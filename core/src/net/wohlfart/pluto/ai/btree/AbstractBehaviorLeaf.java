package net.wohlfart.pluto.ai.btree;

public abstract class AbstractBehaviorLeaf implements IBehavior {

    private BehaviorTaskContext context;

    @SuppressWarnings("unchecked")
    @Override
    public AbstractBehaviorLeaf withContext(BehaviorTaskContext context) {
        this.context = context;
        return this;
    }

    @Override
    public BehaviorTaskContext getContext() {
        return context;
    }

    @Override
    public void addChild(IBehavior behavior) {
        throw new IllegalArgumentException("can't add child");
    }

    @Override
    public void removeChild(IBehavior behavior) {
        throw new IllegalArgumentException("can't remove child");
    }

}
