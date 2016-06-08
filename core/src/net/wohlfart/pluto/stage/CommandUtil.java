package net.wohlfart.pluto.stage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.SpinBehavior;
import net.wohlfart.pluto.entity.effects.LightCommand;
import net.wohlfart.pluto.entity.fab.CubeCommand;
import net.wohlfart.pluto.entity.fab.roam.ColorFunction;
import net.wohlfart.pluto.entity.fab.roam.RoamBodyCommand;
import net.wohlfart.pluto.entity.fab.roam.SimplexIteration;
import net.wohlfart.pluto.scene.Position;

public class CommandUtil {

    static RoamBodyCommand planet(long[] uid) {
        final SimplexIteration surface = new SimplexIteration(6, 1f, 0.007f);
        return new RoamBodyCommand()
                .withDetails(4)
                .withRadius(6)
                .withHeightFunction(surface)
                .withColorFunction(new ColorFunction.GradientHeight(surface))
                .withBehavior(new SpinBehavior()
                        .withAxis(new Vector3(0, 0, 1))
                        .withAngle(10f))
                .withPosition(0, 0, -50);
    }

    static LightCommand[] lights(long[] uid) {
        return new LightCommand[] {
                new LightCommand().withUid(uid[0]++).withColor(Color.GRAY),
                new LightCommand().withUid(uid[0]++).withColor(Color.WHITE).withDirection(new Vector3(-1, -1, 1)),
                new LightCommand().withUid(uid[0]++).withColor(Color.WHITE).withPosition(new Position(0, 20, -30)),
        };
    }

    static CubeCommand cube(long[] uid) {
        return new CubeCommand()
                .withUid(uid[0]++)
                .withPosition(0, 10, -30)
                .withTextureFile("texture/ash_uvgrid01.png")
                .withLength(3)
                .withBehavior(new SpinBehavior().withRotation(new Quaternion(1, 1, 1, 2).nor()))
                .withRotation(new Quaternion(1, 1, 1, 2).nor());
    }

}
