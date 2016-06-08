package net.wohlfart.pluto.shader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.util.Utils;

@Deprecated
public class TextureDataLoader extends AsynchronousAssetLoader<TextureData[], AssetLoaderParameters<TextureData[]>> {

    public TextureDataLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String string, FileHandle fileHandle, AssetLoaderParameters<TextureData[]> parameter) {
        final Array<AssetDescriptor> dependencies = new Array<>();
        final String[] paths = getPaths(string);
        for (final String path : paths) {
            dependencies.add(new AssetDescriptor<>(path, TextureData.class));
        }
        return dependencies;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<TextureData[]> parameter) {
    }

    @Override
    public TextureData[] loadSync(AssetManager manager, String string, FileHandle fileHandle, AssetLoaderParameters<TextureData[]> parameter) {
        final String[] paths = getPaths(string);
        final TextureData[] textureData = new TextureData[Utils.CUBE_PARTS.size()];
        for (int i = 0; i < Utils.CUBE_PARTS.size(); i++) {
            textureData[i] = manager.get(paths[i]);
        }
        return textureData;
    }

    private String[] getPaths(String subPath) {
        final String[] result = new String[Utils.CUBE_PARTS.size()];
        for (int i = 0; i < Utils.CUBE_PARTS.size(); i++) {
            result[i] = ResourceManager.CUBEMAP_PATH + subPath + Utils.CUBE_PARTS.get(i);
        }
        return result;
    }

}
