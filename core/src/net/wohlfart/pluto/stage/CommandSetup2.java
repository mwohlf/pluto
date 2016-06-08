package net.wohlfart.pluto.stage;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.SeekBehavior;
import net.wohlfart.pluto.entity.CameraSetup;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.effects.LightCommand;
import net.wohlfart.pluto.entity.fab.IcosphereCommand;
import net.wohlfart.pluto.entity.fab.WaypointCommand;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.shader.SkyboxCommand;

// move the cam to a position
public class CommandSetup2 extends AbstractCommandFactory {

    // TODO: convert this into a DSL parser
    @Override
    protected void setup(ISceneGraph graph, FutureGraph futureGraph) {
        // final Random random = new Random();
        final long[] uid = new long[1];
        uid[0]++;

        add(new CommandSupplier() {
            @Override
            public List<IEntityCommand> get(ISceneGraph graph) {
                final long waypoint1Id = uid[0]++;
                final WaypointCommand waypoint1 = new WaypointCommand()
                        .withPosition(20, 20, 20)
                        .withUid(waypoint1Id);

                final long waypoint2Id = uid[0]++;
                final WaypointCommand waypoint2 = new WaypointCommand()
                        .withPosition(21, 20, 20)
                        .withUid(waypoint2Id);

                return Arrays.asList(
                        waypoint1,
                        waypoint2,
                        new CameraSetup()
                                .withUid(uid[0]++)
                                .withBehavior(new SeekBehavior()
                                        //.withForward(Vector3.Z.scl(-1))
                                        .withMoveSpeed(3f)
                                        .withRotationSpeed(0.4f)
                //.withEntity(graph.findEntity(waypoint1Id))
                ))

                ;
            }
        });

        add(new CommandSupplier() {
            @Override
            public List<IEntityCommand> get(ISceneGraph graph) {
                return Arrays.asList(
                        new SkyboxCommand().withUid(uid[0]++).withStyle(SkyboxCommand.TEST),
                        new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                        new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                        new LightCommand().withUid(uid[0]++).withColor(Color.WHITE).withDirection(new Vector3(-1, -1, 1)),
                        new IcosphereCommand().withPosition(0, 0, -30),
                        CommandUtil.planet(uid));
            }
        });

    }

}
