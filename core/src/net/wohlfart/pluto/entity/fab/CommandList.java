package net.wohlfart.pluto.entity.fab;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.ashley.core.Entity;

import net.wohlfart.pluto.ai.SeekBehavior;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.ai.btree.Looping;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.util.IConsumer;
import net.wohlfart.pluto.util.Utils;

// a set of waypoints
public class CommandList implements IEntityCommand {

    private final ISceneGraph graph;

    private final Map<IEntityCommand, FutureEntity> list = new LinkedHashMap<>();

    private final AtomicInteger openTasks = new AtomicInteger(0);

    private FutureEntity futureEntity;

    private Entity entity;

    private IBehavior behavior;

    private long uid = IEntityCommand.NULL_UID;

    public CommandList(ISceneGraph graph) {
        this.graph = graph;
    }

    @Override
    public long getUid() {
        assert this.uid != IEntityCommand.NULL_UID : "uid is invalid";
        return uid;
    }

    public CommandList withUid(long uid) {
        assert this.uid == IEntityCommand.NULL_UID : "uid can't be changed";
        this.uid = uid;
        return this;
    }

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        this.futureEntity = futureEntity;
        this.entity = entityPool.createEntity();
        for (final Entry<IEntityCommand, FutureEntity> entry : list.entrySet()) {
            entry.getKey().runNow(resourceManager, entityPool, entry.getValue());
        }
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        for (final Entry<IEntityCommand, FutureEntity> entry : list.entrySet()) {
            if (!entry.getValue().isDone()) {
                entry.getKey().runAsync(resourceManager, entityPool, entry.getValue());
            }
        }
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        for (final Entry<IEntityCommand, FutureEntity> entry : list.entrySet()) {
            if (!entry.getValue().isDone()) {
                entry.getKey().runSync(resourceManager, entityPool, entry.getValue());
            }
        }
    }

    public CommandList add(IEntityCommand command) {
        assert Utils.isRenderThread();
        openTasks.incrementAndGet();
        list.put(command, new FutureEntity(graph).then(new IConsumer<Entity>() {
            @Override
            public void apply(Entity input) {
                assert Utils.isRenderThread();
                openTasks.decrementAndGet();
                if (openTasks.get() == 0) {
                    futureEntity.set(entity); // to trigger the callbacks
                }
            }
        }));
        return this;
    }

    public CommandList addWaypoint(Position position) {
        return add(new WaypointCommand().withPosition(position));
    }

    public IBehavior asLoopBehavior() {
        behavior = new Looping();
        for (final Entry<IEntityCommand, FutureEntity> entry : list.entrySet()) {
            entry.getValue().then(new IConsumer<Entity>() {
                @Override
                public void apply(Entity target) {
                    behavior.addChild(new SeekBehavior()
                            //.withForward(Vector3.X)
                            .withEntity(target));
                }
            });
        }
        return behavior;
    }

}
