package net.wohlfart.pluto.entity.fab;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.SeekBehavior;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.ai.btree.Looping;
import net.wohlfart.pluto.ai.btree.Sequential;
import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.util.IConsumer;

public final class FlightpathBuilder {

    private final SceneGraph graph;
    private final IBehavior<?> flightPath;
    private float moveSpeed;
    private float rotationSpeed;
    private Vector3 forward;

    public static FlightpathBuilder createLoop(SceneGraph graph) {
        return new FlightpathBuilder(graph, new Looping());
    }

    public static FlightpathBuilder createSequence(SceneGraph graph) {
        return new FlightpathBuilder(graph, new Sequential());
    }

    private FlightpathBuilder(SceneGraph graph, IBehavior<?> flightPath) {
        this.graph = graph;
        this.flightPath = flightPath;
    }

    public FlightpathBuilder withMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
        return this;
    }

    public FlightpathBuilder withRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public FlightpathBuilder withForward(Vector3 forward) {
        this.forward = forward;
        return this;
    }

    public FlightpathBuilder addPosition(int x, int y, int z) {
        graph.create(new WaypointCommand().withPosition(x, y, z)).then(new IConsumer<Entity>() {
            @Override
            public void apply(Entity waypoint) {
                addTarget(waypoint);
            }
        });
        return this;
    }

    public FlightpathBuilder addTarget(Entity entity) {
        flightPath.addChild(new SeekBehavior()
                .withEntity(entity)
                .withMoveSpeed(moveSpeed)
                .withRotationSpeed(rotationSpeed))
                //.withForward(forward))
                ;
        return this;
    }

    public FutureEntity apply(AbstractEntityCommand<?> entityCommand) {
        entityCommand.withBehavior(flightPath);
        return graph.create(entityCommand);
    }

}
