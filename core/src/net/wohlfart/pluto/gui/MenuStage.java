package net.wohlfart.pluto.gui;

import java.util.Comparator;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.wohlfart.pluto.IStage;
import net.wohlfart.pluto.IStageManager;
import net.wohlfart.pluto.IStageTransition;
import net.wohlfart.pluto.StageRepository;
import net.wohlfart.pluto.gui.item.ActionCommand;
import net.wohlfart.pluto.gui.item.SingleSelectCommand;
import net.wohlfart.pluto.gui.item.ToggleCommand;
import net.wohlfart.pluto.gui.widget.Choice;
import net.wohlfart.pluto.gui.widget.LabelHandle;
import net.wohlfart.pluto.resource.MusicAccessor;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.transition.FadeOverTransition;

/**
 * menu stage is closely tied to the resource manager, for one we are able to
 * change resources and the next thing is that the changed resources do
 * influence the look of the menu itself
 *
 * see:
 * http://bitiotic.com/blog/2013/05/23/libgdx-and-android-application-lifecycle/
 */
public class MenuStage extends Stage implements IStage {

    private static final String BACKGROUND_IMAGE = "texture/tiles/spacetile.jpg";
    private static final String BACKGROUND_MUSIC = "music/intro.ogg";

    protected final IStageManager stageManager;
    protected final ResourceManager resourceManager;
    protected final StageRepository stageRepository;

    protected TweenManager tweenManager;

    protected Music music;

    protected WidgetContainer mainMenu;
    protected WidgetContainer configMenu;
    protected WidgetContainer infoSection;

    private Texture background;
    private float x;
    private float y;

    private SpriteBatch batch;

    private final Matrix4 normalProjection = new Matrix4();
    private LabelHandle label;

    public MenuStage(IStageManager stageManager) {
        this(new GuiViewport(), stageManager);
    }

    private MenuStage(GuiViewport viewport, IStageManager stageManager) {
        this.stageManager = stageManager;
        this.resourceManager = stageManager.getResourceManager();
        this.stageRepository = resourceManager.getStageRepository();
        super.setViewport(viewport);
    }

    @Override
    public void create() {
        tweenManager = resourceManager.getTweenManager();

        music = Gdx.audio.newMusic(Gdx.files.internal(MenuStage.BACKGROUND_MUSIC));

        music.setLooping(true);
        music.setVolume(0);
        music.play();
        if (resourceManager.getMusicEnabled()) {
            Tween.to(music, MusicAccessor.VOLUME, 7f)
                    .cast(Music.class)
                    .target(1f)
                    .start(tweenManager);
        }

        mainMenu = createMainMenu(resourceManager);
        configMenu = createConfigMenu(resourceManager);
        infoSection = createInfoSection(resourceManager);

        background = new Texture(MenuStage.BACKGROUND_IMAGE);
        background.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

        addListener((event) -> {
            if (event instanceof InputEvent) {
                final InputEvent inputEvent = (InputEvent) event;
                if (inputEvent.getType() == Type.keyTyped && inputEvent.getKeyCode() == Keys.ESCAPE) {
                    stageManager.scheduleTransitionToStage(new IStageTransition.SwitchTransition(), IStage.SYSTEM_EXIT);
                    return true;
                }
            }
            return false;
        });

        addActor(mainMenu);
        addActor(configMenu);
        addActor(infoSection);

        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(this);

        // trigger updates to all observers, this is a hack since the resources didn't really change
        // however this is the easiest way to update the widgets with the latest values from the resource manager
        // TODO: check if we can poll the data in the widgets constructor
        resourceManager.notifyObservers();
        configMenu.setVisible(false);
        mainMenu.setVisible(true);
        infoSection.setVisible(true);
    }

    @Override
    public void render() {
        final float delta = Gdx.graphics.getDeltaTime();
        x += (delta * 15f) % background.getWidth();
        y += (delta * 10f) % background.getHeight();
        label.setText("fps: " + Gdx.graphics.getFramesPerSecond());
        draw();
        act();
    }

    @Override
    public void pause() {
    }

    private WidgetContainer createInfoSection(ResourceManager resourceManager) {
        final WidgetContainer result = new WidgetContainer(resourceManager, Layout.SOUTH);
        label = result.add("hello world");
        resourceManager.addObserver(result);
        return result;
    }

    private WidgetContainer createConfigMenu(final ResourceManager resourceManager) {
        final WidgetContainer result = new WidgetContainer(resourceManager, Layout.CENTER);
        result.add(new ActionCommand("command.label.done") {
            @Override
            public void execute() {
                resourceManager.storePreferences();
                Timeline.createParallel()
                        .push(mainMenu.show())
                        .push(configMenu.hide())
                        .start(tweenManager);
            }
        });

        result.add(new ToggleCommand("command.label.sound") {
            @Override
            public void toggle(boolean toggle) { // TODO: boolean is bad
                if (toggle) {
                    music.play();
                    music.setVolume(7f);
                    resourceManager.setSoundEnabled(true);
                } else {
                    music.pause();
                    music.setVolume(0f);
                    resourceManager.setSoundEnabled(false);
                }
            }
        }).setChecked(resourceManager.getMusicEnabled());

        result.add(new ToggleCommand("command.label.info") {
            @Override
            public void toggle(boolean toggle) {
                resourceManager.setInfoEnabled(toggle);
            }
        }).setChecked(resourceManager.getInfoEnabled());

        result.add(new SingleSelectCommand<Locale>(resourceManager.getConfig().getLocales()) {
            @Override
            public void select(Locale locale) {
                resourceManager.setLocale(locale);
                resourceManager.refreshResources();
            }
        }).select(resourceManager.getLocale());

        final Array<Choice<Float>> densities = resourceManager.getConfig().getDensities();
        addDeviceDensity(densities);
        densities.sort(new FloatChoiceComparator());
        result.add(new SingleSelectCommand<Float>(densities) {
            @Override
            public void select(Float density) {
                resourceManager.setDensity(density);
                resourceManager.refreshResources();
            }
        }).select(resourceManager.getDensity());

        final com.badlogic.gdx.utils.ObjectMap.Keys<String> stageKeys = stageRepository.getStageKeys();
        final Array<Choice<String>> stageChoices = new Array<>();
        for (final String key : stageKeys) {
            stageChoices.add(new Choice<>(StageRepository.STAGE_NAME_PREFIX + key, key));
        }
        result.add(new SingleSelectCommand<String>(stageChoices) {
            @Override
            public void select(String stage) {
                resourceManager.setStartStage(stage);
            }
        }).select(resourceManager.getStartStageKey());

        resourceManager.addObserver(result);
        return result;
    }

    private void addDeviceDensity(Array<Choice<Float>> densities) {
        final float currentDensity = Gdx.graphics.getDensity();
        for (final Choice<Float> density : densities) {
            if (density.getValue().equals(currentDensity)) {
                return;
            }
        }
        densities.add(new Choice.Static<>(" ("
                + Math.round(currentDensity / 0.6 * 96f) + "dpi)",
                currentDensity));
    }

    private WidgetContainer createMainMenu(final ResourceManager resourceManager) {
        final WidgetContainer result = new WidgetContainer(resourceManager, Layout.CENTER);

        result.add(new ActionCommand("command.label.start") {
            @Override
            public void execute() {
                startStage(resourceManager.getStartStageKey()).start(tweenManager);
            }
        });

        result.add(new ActionCommand("command.label.config") {
            @Override
            public void execute() {
                Timeline.createParallel()
                        .push(mainMenu.hide())
                        .push(configMenu.show())
                        .start(tweenManager);
            }
        });

        /*
         * result.add(new ActionCommand("command.label.demo") {
         *
         * @Override public void execute() { mainMenu.setVisible(false); } });
         */
        result.add(new ActionCommand("command.label.exit") {
            @Override
            public void execute() {
                mainMenu.hide().setCallback((eventType, source) -> {
                    Gdx.input.setInputProcessor(null);
                    Gdx.app.exit();
                }).setCallbackTriggers(TweenCallback.COMPLETE)
                        .start(tweenManager);
            }
        });

        resourceManager.addObserver(result);
        return result;
    }

    private Timeline startStage(String stageName) {
        final IStage stage = stageRepository.createInstance(stageManager, stageName);
        return Timeline.createParallel()
                .push(mainMenu.hide())
                .push(Tween.to(music, MusicAccessor.VOLUME, 2f)
                        // TODO: clear other tweens that might still be running on the music
                        .cast(Music.class).target(0f)
                        .setCallback((eventType, source) -> music.stop()))
                .setCallback((eventType, source) -> {
                    Gdx.input.setInputProcessor(null);
                    stageManager.scheduleTransitionToStage(new FadeOverTransition(stageManager), stage);
                }).setCallbackTriggers(TweenCallback.BEGIN);

    }

    @Override
    public void dispose() {
        resourceManager.removeObserver(mainMenu);
        resourceManager.removeObserver(configMenu);
        resourceManager.removeObserver(infoSection);
        music.stop();
        batch.dispose();
        music.dispose();
    }

    @Override
    public void draw() {
        // clear
        Gdx.graphics.getGL20().glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // background
        batch.begin();
        batch.draw(background, 0, 0, (int) x, (int) y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        super.draw();
    }

    @Override
    public void resize(int screenWidth, int screenHeight) {
        batch.setProjectionMatrix(normalProjection.setToOrtho2D(0, 0, screenWidth, screenHeight));
        getViewport().update(screenWidth, screenHeight, true);
        mainMenu.refresh().start(tweenManager);
        configMenu.refresh().start(tweenManager);
        infoSection.refresh().start(tweenManager);
    }

    @Override
    public void resume() {
        // TODO reload resources
    }

    @Override
    public String toString() {
        return MenuStage.class.getSimpleName();
    }

    @Override
    public float getTasksLeft() {
        return 0;
    }

    @SuppressFBWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE", justification = "no need to serialize this class, or any container using this class")
    private static class FloatChoiceComparator implements Comparator<Choice<Float>> {
        @Override
        public int compare(Choice<Float> left, Choice<Float> right) {
            return Float.compare(left.getValue(), right.getValue());
        }
    }

}
