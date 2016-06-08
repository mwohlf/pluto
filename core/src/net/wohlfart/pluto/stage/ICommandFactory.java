package net.wohlfart.pluto.stage;

import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;

public interface ICommandFactory {

    // time or commands left before the graph can be rendered
    float commandsLeft();

    void start(ISceneGraph graph, FutureGraph futureGraph);

}
