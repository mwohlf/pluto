package net.wohlfart.pluto;

import javax.annotation.Nonnull;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import net.wohlfart.pluto.controller.Command;
import net.wohlfart.pluto.controller.CommandInput;
import net.wohlfart.pluto.controller.ControllerStack;
import net.wohlfart.pluto.controller.IControllerStack;
import net.wohlfart.pluto.controller.UserEventListener.PickRayListener;
import net.wohlfart.pluto.event.EventBus;
import net.wohlfart.pluto.gui.MenuStage;
import net.wohlfart.pluto.hud.Hud;
import net.wohlfart.pluto.hud.TextLayer;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureGraph;
import net.wohlfart.pluto.scene.ISceneGraph;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.transition.FadeOverTransition;
import net.wohlfart.pluto.util.Utils;

/**
 * base class for providing a scene graph with a camera and hud
 */
public abstract class AbstractGraphStage extends ApplicationAdapter implements IStage {

    protected static final Logger LOGGER = LoggerService.forClass(AbstractGraphStage.class);

    protected final EventBus eventBus = new EventBus();

    protected final IStageManager stageManager;

    protected final ResourceManager resourceManager;

    protected final ISceneGraph graph;

    protected IControllerStack controller; // might be changed in subclass constructor

    protected Hud hud;

    protected AbstractGraphStage(@Nonnull IStageManager theStageManager) {
        stageManager = theStageManager;
        resourceManager = stageManager.getResourceManager();
        graph = new SceneGraph(resourceManager);
    }

    @Override
    public void create() {
        AbstractGraphStage.LOGGER.debug("<create> start for '" + this.getClass().getSimpleName() + "'");
        logManagedCachesStatus();
        final long start = Utils.currentTickCount();
        setupGraph(graph).whenReady(this::sceneCreated); // cam is not available before the call to this method
        final long end = Utils.currentTickCount();
        AbstractGraphStage.LOGGER.debug("<create> at " + end + " ticks in setupScene: " + (end - start));
    }

    /**
     * implement in subclass
     * return immediately with future,
     * push commands into the graph
     * set the graph in the future when ready
     *
     * @param graph set into the returning FutureGraph when ready
     */
    public abstract FutureGraph setupGraph(ISceneGraph graph);

    /**
     * callback after scene is created, note that render and resize might be called earlier
     */
    private void sceneCreated() {

        hud = new Hud(graph, eventBus, resourceManager);
        controller = new ControllerStack(
                graph,
                resourceManager.getCommandMap(),
                new PickRayListener(graph.getPickFacade()));

        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(controller);

        setupCommands();
        AbstractGraphStage.LOGGER.debug("<create> finished for '" + this.getClass().getSimpleName() + "'");
    }

    private void setupCommands() {
        AbstractGraphStage.LOGGER.debug("<setupCommands> in AbstractGraphStage");

        final CommandInput commandInput = controller.getCommandInput();
        commandInput.putKeyAction(Keys.ESCAPE, EXIT_COMMAND.getKey());
        commandInput.putKeyAction(Keys.BACK, EXIT_COMMAND.getKey());
        commandInput.putKeyAction(Keys.I, TextLayer.INFO_ACTION);
        commandInput.putKeyAction(Keys.C, AbstractGraphStage.CONTINUOUS_RENDERING_COMMAND.getKey());

        resourceManager.getCommandMap().put(EXIT_COMMAND.getKey(), EXIT_COMMAND);
        resourceManager.getCommandMap().put(CONTINUOUS_RENDERING_COMMAND.getKey(), CONTINUOUS_RENDERING_COMMAND);
    }

    @Override
    public void resize(int width, int height) {
        AbstractGraphStage.LOGGER.debug("<resize> in " + this.getClass().getSimpleName());
        logManagedCachesStatus();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        graph.resize(width, height);
    }

    @Override
    public void render() {
        final long now = TimeUtils.millis();
        final float deltaTime = Gdx.graphics.getDeltaTime();

        // fire events from last render cycle
        eventBus.fireAllEvents();
        // process user input and update scene graph
        graph.update(deltaTime, controller.calculateTransform(now, deltaTime));
        clean();
        graph.render();
        hud.render();
    }

    private void clean() {
        Gdx.graphics.getGL20().glClearColor(0.3f, 0.3f, 0.3f, 0);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
        Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO); // reset blend function
        Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
    }

    @Override
    public void pause() {
        AbstractGraphStage.LOGGER.debug("<pause>");
        AbstractGraphStage.LOGGER.debug("<pause> resources: " + resourceManager.getDiagnostics());
        logManagedCachesStatus();
    }

    @Override
    public void resume() {
        AbstractGraphStage.LOGGER.debug("<resume>");
        AbstractGraphStage.LOGGER.debug("<resume> resources: " + resourceManager.getDiagnostics());
        resourceManager.finishLoading();
        logManagedCachesStatus();
    }

    @Override
    public void dispose() {
        AbstractGraphStage.LOGGER.debug("<dispose> call for '" + this.getClass().getSimpleName() + "'");
        final CommandInput commandInput = controller.getCommandInput();
        commandInput.removeKeyAction(Keys.ESCAPE, EXIT_COMMAND.getKey());
        commandInput.removeKeyAction(Keys.BACK, EXIT_COMMAND.getKey());
        commandInput.removeKeyAction(Keys.C, AbstractGraphStage.CONTINUOUS_RENDERING_COMMAND.getKey());
        commandInput.removeKeyAction(Keys.I, TextLayer.INFO_ACTION);
        graph.dispose();
        hud.dispose();
        resourceManager.getCommandMap().remove(EXIT_COMMAND.getKey());
        resourceManager.getCommandMap().remove(AbstractGraphStage.CONTINUOUS_RENDERING_COMMAND.getKey());
    }

    protected void logManagedCachesStatus() {
        AbstractGraphStage.LOGGER.debug("<logManagedCachesStatus> " + Mesh.getManagedStatus());
        AbstractGraphStage.LOGGER.debug("<logManagedCachesStatus> " + Texture.getManagedStatus());
        AbstractGraphStage.LOGGER.debug("<logManagedCachesStatus> " + Cubemap.getManagedStatus());
        AbstractGraphStage.LOGGER.debug("<logManagedCachesStatus> " + ShaderProgram.getManagedStatus());
        AbstractGraphStage.LOGGER.debug("<logManagedCachesStatus> " + GLFrameBuffer.getManagedStatus());
    }

    @Override
    public String toString() {
        return AbstractGraphStage.class.getSimpleName();
    }

    private final Command EXIT_COMMAND = new Command() {
        public static final String EXIT_COMMAND_KEY = "exit";

        @Override
        public String getKey() {
            return EXIT_COMMAND_KEY;
        }

        @Override
        public void execute() {
            stageManager.scheduleTransitionToStage(new FadeOverTransition(stageManager), new MenuStage(stageManager));
        }

    };

    private static final Command CONTINUOUS_RENDERING_COMMAND = new Command() {
        public static final String CONTINUOUS_RENDERING_COMMAND_KEY = "continuous";

        @Override
        public String getKey() {
            return CONTINUOUS_RENDERING_COMMAND_KEY;
        }

        @Override
        public void execute() {
            final boolean isContinuous = Gdx.graphics.isContinuousRendering();
            Gdx.graphics.setContinuousRendering(!isContinuous);
        }

    };

}
