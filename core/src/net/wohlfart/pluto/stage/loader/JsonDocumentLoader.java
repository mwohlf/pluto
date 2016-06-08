package net.wohlfart.pluto.stage.loader;

import java.io.InputStream;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * loading the json tree of a stage document
 */
public class JsonDocumentLoader extends AsynchronousAssetLoader<JsonValue, JsonDocumentLoader.Parameters> {

    private JsonValue document;

    public JsonDocumentLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    // called on the executor thread first
    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String string, FileHandle fileHandle, Parameters parameter) {
        return null;
    }

    // called on the executor thread second
    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, Parameters parameter) {
        final FileHandle resolvedFile = resolve(parameter.path);

        final InputStream input = resolvedFile.read();

        document = new JsonReader().parse(input);
        if (document == null) {
            document = new JsonValue(JsonValue.ValueType.nullValue);
        }
    }

    // called on the render thread third
    @Override
    public JsonValue loadSync(AssetManager manager, String string, FileHandle fileHandle, Parameters parameter) {
        return document;
    }

    public static class Parameters extends AssetLoaderParameters<JsonValue> {

        private final String path;

        public Parameters(String path, LoadedCallback loadedCallback) {
            //noinspection AssignmentToSuperclassField
            this.loadedCallback = loadedCallback;
            this.path = path;
        }
    }

}
