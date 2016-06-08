package net.wohlfart.pluto.scene;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class FutureGraph extends AbstractFuture<ISceneGraph> {

    public void whenReady(Runnable runnable) {
        addListener(runnable, MoreExecutors.directExecutor());
    }

    @Override
    public boolean set(ISceneGraph graph) {
        return super.set(graph);
    }

}
