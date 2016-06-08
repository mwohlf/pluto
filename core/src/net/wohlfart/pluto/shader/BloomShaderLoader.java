package net.wohlfart.pluto.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.wohlfart.pluto.Logging;

final class BloomShaderLoader {

    private BloomShaderLoader() {
    }

    public static ShaderProgram createShader(String vertexName, String fragmentName) {

        final String vertexShader = Gdx.files.classpath("bloom/bloomshaders/" + vertexName + ".vertex.glsl")
                .readString();
        final String fragmentShader = Gdx.files.classpath("bloom/bloomshaders/" + fragmentName + ".fragment.glsl")
                .readString();
        ShaderProgram.pedantic = false;
        final ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) {
            Logging.ROOT.error("can't compile shader " + shader.getLog());
        } else {
            Logging.ROOT.info("<createShader> " + shader.getLog());
        }
        return shader;
    }
}
