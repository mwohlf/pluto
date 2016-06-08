package net.wohlfart.pluto.shader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.util.Utils;

public class CubeTexturesLoader extends AsynchronousAssetLoader<TextureData[], CubeTexturesLoader.Parameters> {

    public CubeTexturesLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    // called on the executor thread first
    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String string, FileHandle fileHandle, Parameters parameter) {
        final Array<AssetDescriptor> dependencies = new Array<>();
        final String[] paths = Utils.getPaths(parameter.path);
        for (final String path : paths) {
            dependencies.add(new AssetDescriptor<>(path, Texture.class));
        }
        return dependencies;
    }

    // called on the executor thread second
    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, Parameters parameter) {
    }

    // called on the render thread third
    @Override
    public TextureData[] loadSync(AssetManager manager, String string, FileHandle fileHandle, Parameters parameter) {
        final String[] paths = Utils.getPaths(parameter.path);
        final TextureData[] textureData = new TextureData[Utils.CUBE_PARTS.size()];
        for (int i = 0; i < Utils.CUBE_PARTS.size(); i++) {
            textureData[i] = manager.get(paths[i], Texture.class).getTextureData();
        }
        return textureData;
    }

    static class Parameters extends AssetLoaderParameters<TextureData[]> {

        private final String path;

        Parameters(String path, LoadedCallback loadedCallback) {
            //noinspection AssignmentToSuperclassField
            this.loadedCallback = loadedCallback;
            this.path = path;
        }
    }

}
