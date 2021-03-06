package net.wohlfart.pluto.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.util.concurrent.AbstractFuture;

import net.wohlfart.pluto.util.IConsumer;
import net.wohlfart.pluto.util.Utils;

public class FutureEntity extends AbstractFuture<Entity> {

    private final Collection<IConsumer<Entity>> consumers = new ArrayList<>();

    public FutureEntity(ISceneGraph sceneGraph) {
        addListener(
                new Runnable() {
                    @Override
                    public void run() {
                        callConsumers(get());
                    }
                },
                sceneGraph);
    }

    protected void callConsumers(Entity entity) {
        if (!Utils.isRenderThread()) {
            throw new IllegalAccessError("<callConsumers> need to run on render thread");
        }
        for (final IConsumer<Entity> consumer : consumers) {
            consumer.apply(entity);
        }
    }

    @Override
    public Entity get() {
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new GdxRuntimeException("<get> unable to find entity", ex);
        }
    }

    @Override
    public boolean set(Entity entity) {
        return super.set(entity);
    }

    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }

    public FutureEntity then(IConsumer<Entity> consumer) {
        if (!Utils.isRenderThread()) {
            throw new IllegalAccessError("<then> need to run on render thread");
        }
        if (isDone()) {
            consumer.apply(get());
        } else {
            consumers.add(consumer);
        }
        return this;
    }

    public FutureEntity then(final Runnable runnable) {
        return then(new EntityIConsumer(runnable));
    }

    private static final class EntityIConsumer implements IConsumer<Entity> {
        private final Runnable runnable;

        private EntityIConsumer(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void apply(Entity ignored) {
            runnable.run();
        }
    }
}
