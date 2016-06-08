package net.wohlfart.pluto;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;

import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.lang.ParseableStage;
import net.wohlfart.pluto.util.Utils;

public class StageRepository {

    // prefix in the i18n file
    public static final String STAGE_NAME_PREFIX = "config.stage.label.";

    // stage to start with
    public static final String INITIAL_STAGE_KEY = "initialStage";

    // container
    private static final ObjectMap<String, IStageCreator> STAGE_FACTORIES = new ObjectMap<>();

    public StageRepository(FileHandleResolver resolver) {
        // setup the initial stage
        STAGE_FACTORIES.put(StageRepository.INITIAL_STAGE_KEY, new IStageCreator() {
            @Override
            public IStage createInstance(IStageManager stageManager) {
                return new ParseableStage(stageManager, ResourceManager.STAGE_PATH + "stage1.scene");
            }
        });
        STAGE_FACTORIES.put("stage2", new IStageCreator() {
            @Override
            public IStage createInstance(IStageManager stageManager) {
                return new ParseableStage(stageManager, ResourceManager.STAGE_PATH + "stage2.scene");
            }
        });
    }

    /**
     * @return keys for the registered stages
     */
    public Keys<String> getStageKeys() {
        return STAGE_FACTORIES.keys();
    }

    /**
     * @param stageManager the container for rendering stages
     * @param key id of the requested stage available keys can be retrieved with getStageKeys()
     * @return the stage
     */
    public IStage createInstance(IStageManager stageManager, String key) {
        assert Utils.isRenderThread();
        assert STAGE_FACTORIES.containsKey(key) : "key not found in StageRepository: '" + key + "'";
        final IStage result = STAGE_FACTORIES.get(key).createInstance(stageManager);
        assert result != null : "null returned for key '" + key + "'";
        return result;
    }

}
