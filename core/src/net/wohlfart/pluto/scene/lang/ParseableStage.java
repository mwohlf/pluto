package net.wohlfart.pluto.scene.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.AbstractGraphStage;
import net.wohlfart.pluto.IStageManager;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.stage.SceneLanguageLexer;
import net.wohlfart.pluto.stage.SceneLanguageParser;

public class ParseableStage extends AbstractGraphStage {

    protected static final Logger LOGGER = LoggerService.forClass(ParseableStage.class);

    private final String filename;

    private final FutureGraph futureGraph;

    public ParseableStage(IStageManager stageManager, String filename) {
        super(stageManager);
        this.filename = filename;
        this.futureGraph = new FutureGraph();
    }

    @Override
    public float getTasksLeft() {
        final long tasksLeft = graph.getProcessingCount();
        if (tasksLeft == 0) {
            futureGraph.set(graph);
        }
        return tasksLeft;
    }

    @Override
    public FutureGraph setupGraph(ISceneGraph graph) {
        final FileHandle handle = Gdx.files.internal(filename);
        try (BufferedReader reader = new BufferedReader(handle.reader())) {
            final SceneLanguageLexer lexer = new SceneLanguageLexer(new ANTLRInputStream(reader));

            // pass the tokens to the parser
            final SceneLanguageParser parser = new SceneLanguageParser(new CommonTokenStream(lexer));
            parser.setBuildParseTree(true);
            //parser.setErrorHandler(new BailErrorStrategy());
            final ParseTree tree = parser.parse();

            final Scope scope = new Scope();
            final Map<String, Function> functions = new HashMap<String, Function>();

            // TODO: do we need the return value from the visit call?
            new EvalVisitor(graph, scope, functions).visit(tree);

            LOGGER.info("<setupGraph> scope: " + scope);
            LOGGER.info("<setupGraph> functions: " + functions);

        } catch (final IOException ex) {
            throw new GdxRuntimeException(ex);
        }
        return futureGraph;
    }

}
