package net.wohlfart.pluto.stage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.util.Utils;

public abstract class AbstractCommandFactory implements ICommandFactory {

    private final List<CommandSupplier> commandSuppliers = new ArrayList<>();
    private final Deque<IEntityCommand> entityDeque = new ArrayDeque<>();
    private final AtomicInteger tasks = new AtomicInteger(1);

    @Override
    public float commandsLeft() {
        assert Utils.isRenderThread();
        return tasks.get();
    }

    @Override
    public void start(ISceneGraph graph, FutureGraph futureGraph) {
        setup(graph, futureGraph);
        graph.execute(new Runnable() {
            @Override
            public void run() {
                assert Utils.isRenderThread();
                for (final CommandSupplier commandSupplier : commandSuppliers) {
                    final List<IEntityCommand> commands = commandSupplier.get(graph);
                    for (final IEntityCommand cmd : commands) {
                        tasks.incrementAndGet();
                        entityDeque.push(cmd);
                    }
                }
                createGraph(entityDeque, graph, futureGraph);
            }
        });
    }

    abstract void setup(ISceneGraph graph, FutureGraph futureGraph);

    protected void createGraph(Deque<IEntityCommand> deque, ISceneGraph graph, FutureGraph futureGraph) {
        if (deque.isEmpty()) {
            tasks.decrementAndGet();
            futureGraph.set(graph); // termination
            return;
        }
        final IEntityCommand futureEntity = deque.pop();
        graph.create(futureEntity).then(() -> {
            //this runs in a callback when the entity is created
            tasks.decrementAndGet();
            // tail recursion would be nice here
            createGraph(deque, graph, futureGraph);
        });
    }

    public void add(CommandSupplier commandSupplier) {
        commandSuppliers.add(commandSupplier);
    }

    public void apply(IEntityCommand cmd) {
        assert Utils.isRenderThread();
        tasks.incrementAndGet();
        entityDeque.push(cmd);
    }

    interface CommandSupplier {

        List<IEntityCommand> get(ISceneGraph graph);

    }

}
