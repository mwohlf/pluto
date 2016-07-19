package net.wohlfart.pluto.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

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
            return new SkyboxShader(renderable);
        } else if (renderable.material.has(CubemapAttribute.EnvironmentMap)) {
            return new CubemapShader(renderable);
        } else {
            return new MainShader(renderable);
        }
    }

}
