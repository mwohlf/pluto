package net.wohlfart.pluto.stage;

import java.util.ArrayDeque;
import java.util.Deque;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.wohlfart.pluto.AbstractGraphStage;
import net.wohlfart.pluto.IStageManager;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.stage.loader.JsonConverter;
import net.wohlfart.pluto.stage.loader.JsonDocumentLoader;
import net.wohlfart.pluto.util.Utils;

/**
 * setting up a stage from a json document
 *
 * TODO: move this into a CommandFactory
 */
@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "for testing only")
public class LoadableStage extends AbstractGraphStage {

    protected static final Logger LOGGER = LoggerService.forClass(LoadableStage.class);

    public static final String GRAPH_TAG = "graph";
    public static final String STAGE_KEY_TAG = "stageKey";

    private final String filename;

    private boolean isCreated;

    public LoadableStage(IStageManager stageManager, String filename) {
        super(stageManager);
        this.filename = filename;
    }

    @Override
    public float getTasksLeft() {
        return isCreated ? 0 : 1;
    }

    // load the json and async call the next step
    @Override
    public FutureGraph setupGraph(ISceneGraph graph) {
        assert !isCreated : "stage was already setup";
        final FutureGraph futureGraph = new FutureGraph();
        this.resourceManager.load(
                filename,
                JsonValue.class,
                new JsonDocumentLoader.Parameters(ResourceManager.STAGE_PATH + filename,
                        // the resource callback after the json doc is loaded
                        (assetManager, localPath, type) -> parseJson(futureGraph, assetManager.get(localPath, JsonValue.class))));
        return futureGraph;
    }

    // looping through the scene elements
    private void parseJson(FutureGraph futureGraph, JsonValue jsonValue) {
        assert Utils.isRenderThread();
        for (JsonValue entry = jsonValue.child; entry != null; entry = entry.next) {
            switch (entry.name) {
                case GRAPH_TAG:
                    initGraph(futureGraph, collectElements(entry));
                    break;
                case STAGE_KEY_TAG:
                    LoadableStage.LOGGER.debug("<setupScene> skipping stageKey '" + entry.asString() + "'");
                    break;
                default:
                    LoadableStage.LOGGER.error("<setupScene> ignoring element name '" + entry.name + "'");
            }
        }
    }

    private Deque<JsonValue> collectElements(JsonValue graphValue) {
        final Deque<JsonValue> deque = new ArrayDeque<>();
        for (JsonValue jsonValue = graphValue.child; jsonValue != null; jsonValue = jsonValue.next) {
            deque.add(jsonValue);
        }
        return deque;
    }

    // recursive create the entities for the graph and callback when done
    private void initGraph(FutureGraph futureGraph, Deque<JsonValue> deque) {
        if (deque.isEmpty()) {
            this.isCreated = true;
            futureGraph.set(graph);
            return;
        }
        final JsonValue jsonValue = deque.pop();
        final String name = jsonValue.name;
        try {
            LoadableStage.LOGGER.info("<parseGraphElement> " + name);
            new JsonConverter().runEntityCommand(graph, jsonValue, entity -> {
                // recursion to create the next entity
                LoadableStage.LOGGER.info("<initGraph.ready> '" + name + "' deque next entity");
                initGraph(futureGraph, deque);
            });
        } catch (final Exception ex) {
            throw new GdxRuntimeException("exception initializing object from json value,"
                    + " object name was '" + jsonValue.name + "', error message was: \"" + ex.getMessage() + "\"", ex);
        }
    }

    @Override
    public String toString() {
        return LoadableStage.class.getSimpleName();
    }

}
