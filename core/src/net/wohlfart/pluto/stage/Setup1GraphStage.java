package net.wohlfart.pluto.stage;

import net.wohlfart.pluto.AbstractGraphStage;
import net.wohlfart.pluto.IStageManager;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;

public class Setup1GraphStage extends AbstractGraphStage {

    protected final ICommandFactory commandFactory;

    public Setup1GraphStage(IStageManager stageManager) {
        super(stageManager);
        commandFactory = new CommandSetup1();
    }

    @Override
    public float getTasksLeft() {
        return commandFactory.commandsLeft();
    }

    @Override
    public FutureGraph setupGraph(ISceneGraph graph) {
        final FutureGraph futureGraph = new FutureGraph();
        commandFactory.start(graph, futureGraph);
        return futureGraph;
    }

}
