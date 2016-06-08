package net.wohlfart.pluto.stage;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.FireLaser;
import net.wohlfart.pluto.ai.SeekBehavior;
import net.wohlfart.pluto.ai.btree.Delay;
import net.wohlfart.pluto.ai.btree.Looping;
import net.wohlfart.pluto.ai.btree.Parallel;
import net.wohlfart.pluto.entity.CameraSetup;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.effects.LightCommand;
import net.wohlfart.pluto.entity.fab.CommandList;
import net.wohlfart.pluto.entity.fab.IcosphereCommand;
import net.wohlfart.pluto.entity.fab.ObjectCommand;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.shader.SkyboxCommand;

// cam follow a ship
public class CommandSetup3 extends AbstractCommandFactory {

    // TODO: convert this into a DSL parser
    @Override
    public void setup(ISceneGraph graph, FutureGraph futureGraph) {
        // final Random random = new Random();
        final long[] uid = new long[1];
        uid[0]++;

        final long shipId = uid[0]++;

        add(new CommandSupplier() {
            @Override
            public List<IEntityCommand> get(ISceneGraph graph) {
                return Arrays.asList(
                        //new CameraSetup().withUid(uid[0]++),

                        new CameraSetup()
                                .withUid(uid[0]++)
                                /*
                                .withBehavior(new SeekBehavior()
                                        .withForward(Vector3.Z.scl(-1))
                                        .withMoveSpeed(3f)
                                        .withRotationSpeed(0.4f)
                                        .withEntity(graph.findEntity(shipId)))
                                */
                                ,

                        new SkyboxCommand().withUid(uid[0]++).withStyle(SkyboxCommand.BLUE),
                        new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                        new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                        new LightCommand().withUid(uid[0]++).withColor(Color.WHITE).withDirection(new Vector3(-1, -1, 1)),
                        new IcosphereCommand().withPosition(0, 0, -30),
                        CommandUtil.planet(uid));
            }
        });

        add(new CommandSupplier() {
            @Override
            public List<IEntityCommand> get(ISceneGraph graph) {

                final CommandList commandList = new CommandList(graph)
                        .withUid(uid[0]++)
                        .addWaypoint(new Position(0, 0, -70))
                        .addWaypoint(new Position(0, 50, -90))
                        .addWaypoint(new Position(+10, +20, -20))
                        .addWaypoint(new Position(+15, +14, -21))
                        .addWaypoint(new Position(+12, +10, +20))
                        .addWaypoint(new Position(-10, +30, -20))
                        .addWaypoint(new Position(+10, +60, +70))
                        .addWaypoint(new Position(-40, -12, -60))
                        .addWaypoint(new Position(+10, +20, +20))
                        .addWaypoint(new Position(-40, -20, -50))
                        .addWaypoint(new Position(+10, +60, +30))
                        .addWaypoint(new Position(-10, +20, -10));

                final ObjectCommand ship1 = new ObjectCommand()
                        .withUid(uid[0]++)
                        .withPosition(0, 0, -20)
                        .withFile("obj/ships/bug1.obj")
                        .withBehavior(commandList.asLoopBehavior());

                final Parallel parallel = new Parallel();

                final Looping looping = new Looping();
                looping.addChild(new Delay().withTimeout(2f));
                looping.addChild(new FireLaser().withTimeout(2f).withTarget(graph.findEntity(ship1.getUid())));

                parallel.addChild(looping);
                parallel.addChild(new SeekBehavior()
                //.withForward(Vector3.X)
                //.withRotationSpeed(0.7f)
                //.withMoveSpeed(1f)
                //.withEntity(graph.findEntity(ship1.getUid()))
                );

                final ObjectCommand ship2 = new ObjectCommand()
                        //.withUid(uid[0]++)
                        .withUid(shipId)
                        .withPosition(10, 10, -20)
                        .withFile("obj/ships/bug1.obj")
                        .withBehavior(parallel);

                return Arrays.asList(
                        commandList,
                        ship1,
                        ship2);
            }
        });

    }

}
