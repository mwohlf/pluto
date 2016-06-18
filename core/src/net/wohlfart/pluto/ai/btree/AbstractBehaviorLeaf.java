package net.wohlfart.pluto.ai.btree;

public abstract class AbstractBehaviorLeaf implements IBehavior {

    protected BehaviorContext context;

    @SuppressWarnings("unchecked")
    @Override
    public AbstractBehaviorLeaf withContext(BehaviorContext context) {
        this.context = context;
        return this;
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
