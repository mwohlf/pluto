package net.wohlfart.pluto.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

import net.wohlfart.pluto.Logging;

public class SceneShaderProvider extends DefaultShaderProvider {

    @Override
    public Shader getShader(Renderable renderable) {
        return super.getShader(renderable);
    }

    // TODO initialize/precompile the shaders

    // creates a shader that will be reused if possible and if
    // Shader.canRender() returns true for a different Renderable
    @Override
    protected Shader createShader(final Renderable renderable) {
        // preferred
        if (renderable.material.has(SkyboxAttribute.Type)) {
            Logging.ROOT.info("<createShader> SkyboxShader for " + renderable);
            return new SkyboxShader(renderable);
        } else if (renderable.material.has(CubemapAttribute.EnvironmentMap)) {
            Logging.ROOT.info("<createShader> CubemapShader for " + renderable);
            return new CubemapShader(renderable);
        } else {
            Logging.ROOT.info("<createShader> MainShader for " + renderable);
            return new MainShader(renderable);
        }
    }

}
