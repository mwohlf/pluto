package net.wohlfart.pluto.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import net.wohlfart.pluto.IStage;
import net.wohlfart.pluto.IStageManager;
import net.wohlfart.pluto.IStageTransition;
import net.wohlfart.pluto.resource.FadeAccessor;
import net.wohlfart.pluto.resource.FontManager;
import net.wohlfart.pluto.util.Utils;

public class FadeOverTransition implements IStageTransition {

    private static final int FADE_OUT_TIME = 2;

    private static final int FADE_IN_TIME = 3;

    protected final Matrix4 normalProjection = new Matrix4();

    protected final IStageManager stageManager;

    protected final TweenManager tweenManager;

    private final SpriteBatch batch;

    private Fade fadeOld;

    private Fade fadeNew;

    private final float outTimer;

    private final float inTimer;

    private Timeline timeline;

    private BitmapFont bitmapFont;

    public FadeOverTransition(final IStageManager stageManager) {
        this(stageManager, FadeOverTransition.FADE_OUT_TIME, FadeOverTransition.FADE_IN_TIME);
    }

    private FadeOverTransition(final IStageManager stageManager, int outTimer, int inTimer) {
        this.stageManager = stageManager;
        this.tweenManager = stageManager.getResourceManager().getTweenManager();
        this.batch = new SpriteBatch();
        this.batch.setProjectionMatrix(normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.outTimer = outTimer;
        this.inTimer = inTimer;
    }

    @Override
    public IStageTransition initTransition(IStage oldStage, final IStage newStage) {
        this.fadeOld = new Fade(oldStage);
        this.fadeNew = new Fade(newStage);
        create();
        return this;
    }

    @Override
    public float getTasksLeft() {
        return fadeNew.getStage().getTasksLeft()
                + (timeline.getFullDuration() - timeline.getCurrentTime());
    }

    @Override
    public void create() {
        //this.fadeNew.getStage().create();
        //this.fadeNew.getStage().resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        fadeOld.setOpacity(1);
        fadeNew.setOpacity(0);
        bitmapFont = stageManager.getResourceManager().getFont(FontManager.FontKey.TEXT_FONT);
        bitmapFont.setColor(1.0f, 1.0f, 1.0f, 0.7f);
        timeline = Timeline.createSequence()
                .push(Tween.to(fadeOld, FadeAccessor.OPACITY, outTimer).target(0f))
                .push(Tween.call((int trigger, BaseTween<?> baseTween) -> timeline.pause()))
                .push(Tween.to(fadeNew, FadeAccessor.OPACITY, inTimer).target(1f));
        timeline.start(tweenManager);
    }

    @Override
    public void render() {
        assert Utils.isRenderThread();

        fadeOld.render();
        fadeNew.render();

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
        batch.begin();
        fadeOld.render(batch);
        fadeNew.render(batch);
        if (timeline.isPaused()) {
            if (fadeNew.getStage().getTasksLeft() <= 0) {
                timeline.resume();
            } else {
                bitmapFont.draw(batch, "waiting...", 50, 50);
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        fadeOld.getStage().dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public String toString() {
        return "FadeInOut"
                + " old: " + fadeOld
                + " new: " + fadeNew;
    }

}
