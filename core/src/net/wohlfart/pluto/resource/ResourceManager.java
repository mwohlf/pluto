package net.wohlfart.pluto.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.PixmapLoader;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.czyzby.kiwi.log.Logger;
import com.github.czyzby.kiwi.log.LoggerService;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import net.wohlfart.pluto.StageRepository;
import net.wohlfart.pluto.controller.Command;
import net.wohlfart.pluto.resource.Executor.BackgroundWorker;
import net.wohlfart.pluto.resource.FontManager.FontKey;
import net.wohlfart.pluto.shader.CubeTexturesLoader;
import net.wohlfart.pluto.transition.Fade;
import net.wohlfart.pluto.util.Utils;

// TODO: we need async loading for all resources
public class ResourceManager extends AssetManager {

    protected static final Logger LOGGER = LoggerService.forClass(ResourceManager.class);

    private static final String CONFIG_FILE = "config.json";

    private static final String I18N_BUNDLE_FILE = "i18n/bundle";

    private static final String SKIN_FILE = "skin/uiskin.json";

    private static final String PREFERENCES = "user.preferences";

    // directory for cubemaps
    public static final String CUBEMAP_PATH = "texture/cubemap/";

    // directory for stages
    public static final String STAGE_PATH = "stage/";

    private final Config config;

    private final ObjectMap<String, Command> commandMap;

    private final TweenManager tweenManager;

    private final StageRepository stageRepository;

    private final Executor scheduler;

    private final Preferences preferences;

    private FontManager fontManager;

    private Skin skin;

    private I18NBundle i18nBundle;

    // buffered settings

    private Locale locale;

    private float density;

    private String startStage;

    private boolean infoEnabled;

    private boolean musicEnabled;

    // observers for density and local changes
    private final Collection<ResourceObserver> observers = new ArrayList<>();

    public interface ResourceObserver {

        void updateResources(ResourceManager resourceManager);

    }

    public ResourceManager() {
        this(new InternalFileHandleResolver());
    }

    public ResourceManager(FileHandleResolver resolver) {
        super(resolver, false);
        setupCustomLoaders(resolver);

        final FileHandle configFileHandle = resolver.resolve(ResourceManager.CONFIG_FILE);
        assert configFileHandle.exists() : "config file doesn't exist at: '" + ResourceManager.CONFIG_FILE + "'"
                + " resolver was: '" + resolver + "'"
                + " classpaths: " + Utils.printClasspath();

        Texture.setAssetManager(this);
        config = new Config();
        config.load(configFileHandle);
        preferences = Gdx.app.getPreferences(ResourceManager.PREFERENCES);
        commandMap = new ObjectMap<>();
        scheduler = new Executor();
        stageRepository = new StageRepository(resolver);
        tweenManager = createTweenManager();
        // buffered values
        locale = Locale.forLanguageTag(preferences.getString("locale", config.getString("locale", Locale.getDefault().toLanguageTag())));
        density = preferences.getFloat("density", Gdx.graphics.getDensity());
        startStage = getStartStage();
        infoEnabled = preferences.getBoolean("infoEnabled", false);

        refreshResources();
    }

    private String getStartStage() {
        String result = preferences.getString("startStage", "");
        if (!stageRepository.getStageKeys().toArray().contains(result, false)) {
            result = StageRepository.INITIAL_STAGE_KEY;
        }
        return result;
    }

    private void setupCustomLoaders(FileHandleResolver resolver) {
        setLoader(BitmapFont.class, new BitmapFontLoader(resolver));
        setLoader(Music.class, new MusicLoader(resolver));
        setLoader(Pixmap.class, new PixmapLoader(resolver));
        setLoader(Sound.class, new SoundLoader(resolver));
        setLoader(TextureAtlas.class, new TextureAtlasLoader(resolver));
        setLoader(Texture.class, new TextureLoader(resolver));
        setLoader(Skin.class, new SkinLoader(resolver));
        //setLoader(ParticleEffect.class, new ParticleEffectLoader(resolver));
        //setLoader(com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.class, new com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader(resolver));
        //setLoader(PolygonRegion.class, new PolygonRegionLoader(resolver));
        setLoader(I18NBundle.class, new I18NBundleLoader(resolver));
        //setLoader(Model.class, ".g3dj", new G3dModelLoader(new JsonReader(), resolver));
        //setLoader(Model.class, ".g3db", new G3dModelLoader(new UBJsonReader(), resolver));
        setLoader(Model.class, ".obj", new ObjLoader(resolver));

        // custom loaders
        setLoader(TextureData[].class, new CubeTexturesLoader(resolver));
        //setLoader(JsonValue.class, new JsonDocumentLoader(resolver));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final void refreshResources() {
        if (fontManager != null) {
            fontManager.dispose();
        }
        fontManager = new FontManager();

        // unload, we might have a different locale
        if (isLoaded(ResourceManager.I18N_BUNDLE_FILE, I18NBundle.class)) {
            unload(ResourceManager.I18N_BUNDLE_FILE);
        }
        load(new AssetDescriptor(ResourceManager.I18N_BUNDLE_FILE, I18NBundle.class, new I18NBundleLoader.I18NBundleParameter(locale)));
        finishLoadingAsset(ResourceManager.I18N_BUNDLE_FILE);
        i18nBundle = this.get(ResourceManager.I18N_BUNDLE_FILE);

        load(new AssetDescriptor<>(ResourceManager.SKIN_FILE, Skin.class, new SkinLoader.SkinParameter()));
        finishLoadingAsset(ResourceManager.SKIN_FILE);
        skin = this.get(ResourceManager.SKIN_FILE);

        notifyObservers();
    }

    public void notifyObservers() {
        for (final ResourceObserver observer : observers) {
            observer.updateResources(this);
        }
    }

    public <I, O> void schedule(BackgroundWorker<I, O> worker) {
        worker.schedule(scheduler);
    }

    public void invokeLater(Runnable runnable) {
        Gdx.app.postRunnable(() -> {
            final long start = Utils.currentTickCount();
            runnable.run();
            final long end = Utils.currentTickCount();
            ResourceManager.LOGGER.info("runnable " + runnable + " at " + end + " took " + (end - start));
        });
    }

    public void storePreferences() {
        preferences.putFloat("density", density);
        preferences.putString("locale", locale.toLanguageTag());
        preferences.putString("startStage", startStage);
        preferences.putBoolean("infoEnabled", infoEnabled);
        preferences.flush();
    }

    public void setDensity(float density) {
        ResourceManager.LOGGER.info("<setDensity> switching from '" + this.density + "' to '" + density + "'");
        this.density = density;
    }

    public void setLocale(Locale locale) {
        ResourceManager.LOGGER.info("<setLocale> switching from '" + this.locale + "' to '" + locale + "'");
        this.locale = locale;
    }

    public void setStartStage(String startStage) {
        ResourceManager.LOGGER.info("<setStartStage> switching from '" + this.startStage + "' to '" + startStage + "'");
        this.startStage = startStage;
    }

    public void setInfoEnabled(boolean profilerEnabled) {
        this.infoEnabled = profilerEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.musicEnabled = soundEnabled;
    }

    public I18NBundle getI18nBundle() {
        return i18nBundle;
    }

    public BitmapFont getFont(FontKey fontKey) {
        return fontManager.getFont(fontKey);
    }

    public Locale getLocale() {
        return locale;
    }

    public float getDensity() {
        return density;
    }

    public String getStartStageKey() {
        return startStage;
    }

    public Config getConfig() {
        return config;
    }

    public TweenManager getTweenManager() {
        return tweenManager;
    }

    public StageRepository getStageRepository() {
        return stageRepository;
    }

    public Skin getSkin() {
        return skin;
    }

    public boolean getInfoEnabled() {
        return infoEnabled;
    }

    public boolean getMusicEnabled() {
        return musicEnabled;
    }

    private TweenManager createTweenManager() {
        final TweenManager result = new TweenManager();
        Tween.setCombinedAttributesLimit(4);
        Tween.registerAccessor(Actor.class, new ActorAccessor());
        Tween.registerAccessor(Music.class, new MusicAccessor());
        Tween.registerAccessor(Fade.class, new FadeAccessor());
        return result;
    }

    @Override
    public void dispose() {
        fontManager.dispose();
        skin.dispose();
        tweenManager.killAll();
        super.dispose();
    }

    public ObjectMap<String, Command> getCommandMap() {
        return commandMap;
    }

    public void addObserver(ResourceObserver listener) {
        observers.add(listener);
    }

    public void removeObserver(ResourceObserver listener) {
        observers.remove(listener);
    }

    public int openTasks() {
        final int queuedAssets = this.getQueuedAssets();
        final int openTasks = scheduler.getOpenTasks();
        return queuedAssets + openTasks;
    }

    public void finishTasks() {
        this.finishLoading();
    }

}
