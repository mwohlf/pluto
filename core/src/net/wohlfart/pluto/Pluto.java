package net.wohlfart.pluto;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.I18NBundle;

import aurelienribon.tweenengine.TweenManager;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.transition.FadeOverTransition;
import net.wohlfart.pluto.util.Utils;

/**
 * common entry point implements the stage manager for setting and preparing the
 * next stage until it is ready, then switch over with a transition effect
 *
 * see:
 * http://bitiotic.com/blog/2013/05/23/libgdx-and-android-application-lifecycle/
 * https://developer.nvidia.com/fixing-common-android-lifecycle-issues-games for
 * info about android'values call order on stop/resume/pause/home...
 */
public class Pluto implements ApplicationListener, IStageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pluto.class.getSimpleName());

    // the stage or the transition that is currently being rendered
    @Nonnull
    private IStage renderTarget = IStage.RED_STAGE;

    // the next stage being prepared
    @Nonnull
    private IStage nextStage = IStage.NULL_STAGE;

    // a transition being rendered while preparing the next stage
    @Nonnull
    private IStageTransition transition = IStageTransition.NULL_TRANSITION;

    private ResourceManager resourceManager;
    private TweenManager tweenManager;

    @Override
    public void create() {
        Utils.initRenderThreadName(Thread.currentThread().getName());
        Utils.initTickCount();
        I18NBundle.setExceptionOnMissingKey(true);
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        LOGGER.debug("<create> in Pluto setting up StageManagement");

        resourceManager = new ResourceManager();
        if (resourceManager.getInfoEnabled()) {
            GLProfiler.enable();
        }

        tweenManager = resourceManager.getTweenManager();

        scheduleTransitionToStage(new IStageTransition.SwitchTransition(), new IntroStage(this));
        final IStage initialStage = resourceManager.getStageRepository().createInstance(Pluto.this, StageRepository.INITIAL_STAGE_KEY);
        scheduleTransitionToStage(new FadeOverTransition(this), initialStage);
    }

    /**
     * The resize() callback may be invoked multiple times in a row, and may be
     * invoked before or after pause/resume transitions. It may also be called
     * redundantly in some cases, so only do simple, idempotent work in the
     * .resize() callback. Only guarantee is that resize won't be called before
     * create()
     */
    @Override
    public void resize(int width, int height) {
        LOGGER.debug("<resize> in " + this.getClass().getSimpleName());
        renderTarget.resize(width, height);
    }

    /**
     * called in the main loop dispatching to update and render calls preparing
     * next state if needed
     */
    @Override
    public void render() {
        final float delta = Gdx.graphics.getDeltaTime();
        // need the time from the transition not the next stage since transition might need longer for some effects
        if (nextStage != IStage.NULL_STAGE && transition.getTasksLeft() <= 0) {
            switchState();
        } else {
            resourceManager.update();
        }
        tweenManager.update(delta);
        renderTarget.render();
    }

    private void switchState() {
        LOGGER.debug("<switchState> current stage is '" + renderTarget + "' nextStage is '" + nextStage + "'");
        // remember to dispose
        final IStageTransition oldTransition = transition;
        transition = IStageTransition.NULL_TRANSITION;
        renderTarget = nextStage;
        nextStage = IStage.NULL_STAGE; // ready to switch to the next state
        renderTarget.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        oldTransition.dispose();
    }

    /**
     * call with the next stage, create will be called on the new stage this
     * method might return early, switching is done as soon as the stage returns
     * 0 in its openTasks method
     */
    @Override
    public void scheduleTransitionToStage(@Nonnull IStageTransition newTransition, @Nonnull IStage newStage) {
        assert newStage != null : "new Stage is null";
        LOGGER.debug("<scheduleTransitionToStage> is: " + newStage.getClass().getSimpleName());
        // is there another stage waiting?
        if (nextStage != IStage.NULL_STAGE) {
            LOGGER.error("<scheduleTransitionToStage>"
                    + " can't switch state, current next state not yet ready, still preparing '" + nextStage + "'"
                    + " ignoring, not switching to '" + newStage + "'");
            return;
        }
        newStage.create(); // this should return early and be called only once
        nextStage = newStage;
        // transition for disposing, init with the current and the new stage
        transition = newTransition;
        transition.initTransition(renderTarget, newStage);
        // the new render target is the transition
        renderTarget = transition;
        // early switch
        if (nextStage != IStage.NULL_STAGE && transition.getTasksLeft() <= 0) {
            switchState();
        }
    }

    /**
     * The .pause() callback should be "quick" as it blocks the Android UI from
     * moving to the next activity until it is complete.
     */
    @Override
    public void pause() {
        LOGGER.debug("<pause>");
        renderTarget.pause();
    }

    /**
     * The .resume() callback is only invoked after a .pause(). This is in
     * contrast to the normal Android lifecycle where resume is also invoked on
     * the first start of the application. Thus in libgdx, a .resume() is always
     * preceded by a .pause().
     */
    @Override
    public void resume() {
        LOGGER.debug("<resume>");
        renderTarget.resume();
    }

    @Override
    public void dispose() {
        LOGGER.debug("<dispose> call for '" + this.getClass().getSimpleName() + "'");
        renderTarget.dispose();
        resourceManager.dispose();
    }

    @Nonnull
    @Override
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    @Nonnull
    @Override
    public IStage getCurrentStage() {
        return renderTarget;
    }

}
