package net.wohlfart.pluto.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.wohlfart.pluto.Logging;

/**
 * see: http://stackoverflow.com/questions/22553409/libgdx-how-to-use-shader-in-3d
 */
public class SkyboxShader extends BaseShader {

    private static final String PATH = "shader/cubemap/skybox";

    private static final Setter skyboxSetter = new LocalSetter() {
        @Override
        public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
            //noinspection CastToConcreteClass
            final int unit = shader.context.textureBinder
                    .bind(((SkyboxAttribute) (combinedAttributes.get(SkyboxAttribute.Type))).textureDescription);
            shader.set(inputID, unit);
        }
    };

    private static final class Inputs {
        public static final Uniform skybox = new Uniform("u_skybox");
        public static final Uniform worldTrans = new Uniform("u_worldTrans");
        public static final Uniform projTrans = new Uniform("u_projTrans");
        public static final Uniform viewTrans = new Uniform("u_viewTrans");
        public static final Uniform projViewTrans = new Uniform("u_projViewTrans");
    }

    public SkyboxShader(Renderable firstRenderable) {
        register(Inputs.skybox, skyboxSetter);
        register(Inputs.worldTrans, Setters.worldTrans);
        register(Inputs.projTrans, Setters.projTrans);
        register(Inputs.viewTrans, Setters.viewTrans);
        register(Inputs.projViewTrans, Setters.projViewTrans);
        final ShaderProgram shaderProgram = new ShaderProgram(
                Gdx.files.internal(SkyboxShader.PATH + ".vertex.glsl").readString(),
                Gdx.files.internal(SkyboxShader.PATH + ".fragment.glsl").readString());
        if (!shaderProgram.isCompiled()) {
            Logging.ROOT.error("<init> shader failed to compile", new IllegalArgumentException(shaderProgram.getLog()));
        } else {
            Logging.ROOT.info("<init> shader compiled path: '" + PATH + "'");
        }
        init(shaderProgram, firstRenderable);
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
        return renderable.material.has(SkyboxAttribute.Type);
    }

    @Override
    public void init() {
        // nothing to do
    }

}
