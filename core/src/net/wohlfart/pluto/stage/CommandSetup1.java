package net.wohlfart.pluto.stage;

import java.util.Arrays;
import java.util.List;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.SeekBehavior;
import net.wohlfart.pluto.entity.CameraSetup;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.effects.LightCommand;
import net.wohlfart.pluto.entity.fab.IcosphereCommand;
import net.wohlfart.pluto.entity.fab.ObjectCommand;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.shader.SkyboxCommand;
import net.wohlfart.pluto.util.IConsumer;

// closeup cam move
public class CommandSetup1 extends AbstractCommandFactory {

    // TODO: convert this into a DSL parser
    @Override
    public void setup(ISceneGraph graph, FutureGraph futureGraph) {
        // final Random random = new Random();
        final long[] uid = new long[1];
        uid[0]++;

        final long shipId = uid[0]++;

        final SeekBehavior behavior = new SeekBehavior()
                //.withForward(Vector3.Z.scl(-1))
                .withMoveSpeed(3f)
                .withRotationSpeed(0.4f);

        add(new CommandSupplier() {
            @Override
            public List<IEntityCommand> get(ISceneGraph graph) {
                return Arrays.asList(
                        //new CameraSetup().withUid(uid[0]++),

                        attachBehavior(graph, new CameraSetup().withUid(uid[0]++), behavior),

                        new SkyboxCommand().withUid(uid[0]++).withStyle(SkyboxCommand.BLUE),
                        new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                        new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                        new LightCommand().withUid(uid[0]++).withColor(Color.WHITE).withDirection(new Vector3(-1, -1, 1)),
                        new IcosphereCommand().withPosition(0, 0, -30),
                        CommandUtil.planet(uid),
                        new ObjectCommand()
                                .withUid(shipId)
                                .withPosition(0, 10, -60)
                                .withFile("obj/ships/bug1.obj"));
            }

        });
    }

    private IEntityCommand attachBehavior(ISceneGraph graph, CameraSetup command, SeekBehavior behavior) {
        command.withBehavior(behavior);
        graph.findEntity(command.getUid()).then(new IConsumer<Entity>() {
            @Override
            public void apply(Entity target) {
                behavior.withEntity(target);
            }
        });
        return command;
    }

}
