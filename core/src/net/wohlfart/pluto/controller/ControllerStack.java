package net.wohlfart.pluto.controller;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ObjectMap;

import net.wohlfart.pluto.scene.ISceneGraph;

public class ControllerStack extends InputMultiplexer implements IControllerStack {

    private final Matrix4 transform = new Matrix4();

    private final CommandInput commandInput;
    private final GestureInputStack screenInput;
    private final DesktopInput keyInput;
    private final CamRobotInput robotInput;

    // TODO: remove cam and graph if not needed
    public ControllerStack(ISceneGraph graph, ObjectMap<String, Command> commandMap, UserEventListener listener) {
        // a chain of InputProcessors
        addProcessor(commandInput = new CommandInput(commandMap));
        addProcessor(screenInput = new GestureInputStack(graph.getCamera(), listener));
        addProcessor(keyInput = new DesktopInput());
        addProcessor(robotInput = graph.getCamera().getRobotInput());
    }

    /**
     * @param delta
     *            time since last call in seconds
     */
    @Override
    public Matrix4 calculateTransform(long now, float delta) {
        if (!keyInput.hasTransform() && !screenInput.hasTransform() && !robotInput.hasTransform()) {
            return ITransformCalculator.IDT_MATRIX;
        } else {
            transform.idt();
            transform.mulLeft(keyInput.calculateTransform(now, delta));
            transform.mulLeft(screenInput.calculateTransform(now, delta));
            transform.mulLeft(robotInput.calculateTransform(now, delta));
            return transform;
        }
    }

    @Override
    public CamRobotInput getRobotInput() {
        return robotInput;
    }

    @Override
    public CommandInput getCommandInput() {
        return commandInput;
    }

}
