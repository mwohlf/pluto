package net.wohlfart.pluto.scene;

import javax.annotation.Nonnull;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.Ray;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.Logging;
import net.wohlfart.pluto.ai.btree.BehaviorExecutor;
import net.wohlfart.pluto.entity.AnimationSystem;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.MovementSystem;
import net.wohlfart.pluto.entity.PickSystem;
import net.wohlfart.pluto.entity.ScaleSystem;
import net.wohlfart.pluto.resource.Executor.BackgroundWorker;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.util.Utils;

/**
 *
 * core container for anything renderable
 *
 * see: https://github.com/libgdx/libgdx/wiki/ModelBatch
 *
 */
public class SceneGraph implements ISceneGraph {

    protected final PickSystem pickSystem = new PickSystem();

    private final MovementSystem movementSystem = new MovementSystem();

    private final AnimationSystem animationSystem = new AnimationSystem();

    private final ScaleSystem scaleSystem = new ScaleSystem();

    @Nonnull
    protected final BehaviorExecutor behaviorExecutor;

    private final IPickSystem pickFacade;

    // lights, attributes
    protected final Environment environment;

    // content of the graph
    protected final SceneGraphElementPool entityPool;

    // commands that are not yet finished
    protected long processingCount = 0;

    protected final ResourceManager resourceManager;

    protected ModelRenderLayer modelRenderLayer;

    public SceneGraph(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        behaviorExecutor = new BehaviorExecutor();
        environment = new Environment();
        entityPool = new SceneGraphElementPool(environment, behaviorExecutor);
        modelRenderLayer = new ModelRenderLayer(environment, entityPool);
        pickFacade = createPickFacade();

        entityPool.addSystem(movementSystem);
        entityPool.addSystem(animationSystem);
        entityPool.addSystem(pickSystem);
        entityPool.addSystem(scaleSystem);
    }

    /**
     * main method to inject entities into the scene graph
     *
     * command has 3 methods:
     * - runNow() - executed immediately
     * - runAsync() - execute on the worker thread later
     * - runSync() - executed finally on the render thread
     */
    @Override
    public FutureEntity create(IEntityCommand command) {
        assert Utils.isRenderThread() : "<create> running on wrong thread, must only be called from render thread";
        processingCount++;
        final FutureEntity futureEntity = entityPool.register(command.getUid(), this);
        futureEntity.then(new Runnable() {
            @Override
            public void run() {
                assert Utils.isRenderThread() : "<create.run> running on wrong thread, must only be called from render thread";
                processingCount--;
            }
        });
        command.runNow(resourceManager, entityPool, futureEntity);
        if (futureEntity.isDone()) {
            // early exit
            return futureEntity;
        } // if the future is not done at this point we have to run the async background task

        resourceManager.schedule(new CommandWorker<>(command, futureEntity));
        return futureEntity;
    }

    @Override
    public long getTaskCount() {
        return processingCount;
    }

    @Override
    public FutureEntity findEntity(long uid) {
        return entityPool.register(uid, this);
    }

    @Override
    public void destroy(long uid, Entity entity) {
        entity.removeAll();
        entityPool.unregister(uid, entity);
    }

    @Override
    public void update(float deltaTime, Matrix4 transform) {
        behaviorExecutor.tick(deltaTime, this);
        animationSystem.update(deltaTime);
        movementSystem.applyCamMovement(transform);
        entityPool.update(deltaTime);
    }

    @Override
    public void render() {
        modelRenderLayer.render();
    }

    @Override
    public void dispose() {
        modelRenderLayer.dispose();
        entityPool.clearPools();
    }

    @Override
    public Camera getCamera() {
        final Camera cam = entityPool.getCamera();
        assert cam != null : "cam is still null";
        return cam;
    }

    @Override
    public void resize(int viewportWidth, int viewportHeight) {
        getCamera().resize(viewportWidth, viewportHeight);
    }

    @Override
    public IPickSystem getPickFacade() {
        return pickFacade;
    }

    @Override
    public PickSystem getPickSystem() {
        return pickSystem;
    }

    // TODO: refactor, this is ugly
    private IPickSystem createPickFacade() {
        return new IPickSystem() {

            @Override
            public Ray getPickRay(float screenX, float screenY) {
                return entityPool.getCamera().getPickRay(screenX, screenY);
            }

            @Override
            public Entity getPickedEntity(Ray pickRay) {
                return pickSystem.pick(pickRay);
            }

            @Override
            public void selectEntity(Entity nextPick) {
                pickSystem.select(nextPick);
            }
        };
    }

    @Override // execute on the render thread
    public void execute(@Nonnull Runnable runnable) {
        resourceManager.invokeLater(runnable);
    }

    private final class CommandWorker<I extends O, O extends IEntityCommand> extends BackgroundWorker<I, O> {

        private CommandWorker(I input, FutureEntity futureEntity) {
            super(input, futureEntity);
        }

        // command param is used as commandInput
        @Override
        public O backgroundTask(final I commandInput) {
            assert !Utils.isRenderThread() : "<backgroundTask> running on wrong thread, must not run on the render thread";
            commandInput.runAsync(resourceManager, entityPool, futureEntity); // using side effects on the factory
            return commandInput; // this becomes the backgroundOutput for the onSuccess call
        }

        //
        @Override
        public void onSuccess(final I commandInput, final O backgroundOutput) {
            assert Utils.isRenderThread() : "<onSuccess> running on wrong thread";
            if (!futureEntity.isDone()) {
                // final step, running on render thread again
                backgroundOutput.runSync(resourceManager, entityPool, futureEntity);
            }
        }

        @Override
        public void onFailure(Throwable ex) {
            assert Utils.isRenderThread() : "<onFailure> running on wrong thread";
            futureEntity.setException(ex);
            Logging.ROOT.error("failure executing BackgroundWorker", ex);
        }
    }
}
