package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.btree.AbstractBehaviorLeaf;
import net.wohlfart.pluto.ai.btree.ITask;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.effects.LaserBeamCommand;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;

@EntityElement(type = "FireLaser")
public class FireLaser extends AbstractBehaviorLeaf {

    protected Entity target;

    protected float timeout = 5; // in seconds

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        //assert this.target != null : "no target at: " + System.identityHashCode(this);
        return new TaskImpl().initialize(entity, parent);
    }

    @EntityProperty(name = "target", type = "Entity")
    public FireLaser withTarget(Entity target) {
        this.target = target;
        return this;
    }

    @EntityProperty(name = "timeout", type = "Float")
    public FireLaser withTimeout(float timeout) {
        this.timeout = timeout;
        return this;
    }

    class TaskImpl extends AbstractLeafTask {
        private float time = 0;
        private final int uid = 10_000;
        private final Vector3 begin = new Vector3();
        private final Vector3 end = new Vector3();

        @Override
        public void tick(float delta, SceneGraph graph) {
            assert getContext() != null;
            if (time == 0) {
                graph.create(createBeam());
            } else if (time <= timeout) {
                this.getEntity().getComponent(HasPosition.class).getPosition().get(begin);
                target.getComponent(HasPosition.class).getPosition().get(end);
                getParent().reportState(State.RUNNING); // signal the parent task
            } else {
                graph.destroy(uid, graph.findEntity(uid).get());
                getContext().remove(this); // remove this task from the queue
                getParent().reportState(State.SUCCESS); // signal the parent task
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
