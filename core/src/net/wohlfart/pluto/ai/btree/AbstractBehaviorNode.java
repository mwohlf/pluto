package net.wohlfart.pluto.ai.btree;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBehaviorNode implements IBehavior {

    protected final List<IBehavior> children = new ArrayList<>();

    protected BehaviorContext context;

    @SuppressWarnings("unchecked")
    @Override
    public AbstractBehaviorNode withContext(BehaviorContext context) {
        this.context = context;
        for (final IBehavior behavior : children) {
            behavior.withContext(context);
        }
        return this;
    }

    @Override
    public void addChild(IBehavior behavior) {
        behavior.withContext(context);
        children.add(behavior);
    }

    @Override
    public void removeChild(IBehavior behavior) {
        children.remove(behavior);
    }

}
