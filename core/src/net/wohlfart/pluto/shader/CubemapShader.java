package net.wohlfart.pluto.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

import net.wohlfart.pluto.Logging;

/**
 * see: http://stackoverflow.com/questions/22553409/libgdx-how-to-use-shader-in-3d
 */
public class CubemapShader extends DefaultShader {

    private static final String PATH = "shader/cubemap/cubemap";

    public CubemapShader(Renderable firstRenderable) {
        super(firstRenderable, new DefaultShader.Config(
                Gdx.files.internal(CubemapShader.PATH + ".vertex.glsl").readString(),
                Gdx.files.internal(CubemapShader.PATH + ".fragment.glsl").readString()));
        if (!program.isCompiled()) {
            Logging.ROOT.error("<init> shader failed to compile", new IllegalArgumentException(program.getLog()));
        } else {
            Logging.ROOT.info("<init> shader compiled path: '" + PATH + "'");
        }
    }

    @Override
    public void dispose() {
        Logging.ROOT.info("<dispose> call for '" + this.getClass().getSimpleName() + "'");
        super.dispose();
    }

    @Override
    public int compareTo(Shader other) {
        if (other == null) {
            return -1;
        }
        if (other == this) {
            return 0;
        }
        return 0; // TODO: fix this
    }

    @Override
    public boolean canRender(Renderable renderable) {
        // should be consistent with the RenderableProvider
        return renderable.material.has(CubemapAttribute.EnvironmentMap);
    }

}
