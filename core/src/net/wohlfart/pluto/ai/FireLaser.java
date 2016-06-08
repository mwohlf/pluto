package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.btree.AbstractBehaviorLeaf;
import net.wohlfart.pluto.ai.btree.ITask;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.effects.LaserBeamCommand;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.util.IConsumer;

public class FireLaser extends AbstractBehaviorLeaf<FireLaser> {

    protected Entity target;

    float waitTime;

    public FireLaser withTimeout(float waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        assert this.target != null : "no target at: " + System.identityHashCode(this);
        return new TaskImpl().initialize(entity, parent);
    }

    @EntityProperty(name = "target", type = "Entity")
    public FireLaser withTarget(Entity target) {
        this.target = target;
        return this;
    }

    @Deprecated
    public FireLaser withTarget(FutureEntity futureEntity) {
        futureEntity.then(new IConsumer<Entity>() {
            @Override
            public void apply(Entity target) {
                FireLaser.this.withTarget(target);
            }
        });
        return this;
    }

    class TaskImpl extends AbstractLeafTask {
        private float time = 0;
        private final int uid = 10_000;
        private final Vector3 begin = new Vector3();
        private final Vector3 end = new Vector3();

        @Override
        public void tick(float delta, SceneGraph graph) {
            assert context != null;
            if (time == 0) {
                graph.create(createBeam());
            } else if (time <= waitTime) {
                // TODO: we need to move start and end of the laser here
                getEntity().getComponent(HasPosition.class).getPosition().get(begin);
                target.getComponent(HasPosition.class).getPosition().get(end);
                parent.reportState(State.RUNNING); // signal the parent task
            } else {
                graph.destroy(uid, graph.findEntity(uid).get());
                context.remove(this); // remove this task from the queue
                parent.reportState(State.SUCCESS); // signal the parent task
            }
            time += delta;
        }

        private IEntityCommand createBeam() {
            //getEntity().getComponent(HasPosition.class).getPosition().get(begin);
            //target.getComponent(HasPosition.class).getPosition().get(end);
            return new LaserBeamCommand()
                    .withUid(uid)
                    .withBegin(getEntity())
                    .withEnd(target);
        }

    }

}
