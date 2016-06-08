package net.wohlfart.pluto.scene.properties;

import net.wohlfart.pluto.scene.LightMethod;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HasLightMethod implements Component, Poolable {
    private LightMethod lightMethod;

    @Override
    public void reset() {
        lightMethod = null;
    }

    public LightMethod getLightMethod() {
        return lightMethod;
    }

    public HasLightMethod withPointLight(PointLight pointLight) {
        lightMethod = new LightMethod.Point(pointLight);
        return this;
    }

    public HasLightMethod withAmbientLight(ColorAttribute colorAttribute) {
        lightMethod = new LightMethod.Ambient(colorAttribute);
        return this;
    }

    public HasLightMethod withDirectionalLight(DirectionalLight directionalLight) {
        lightMethod = new LightMethod.Directional(directionalLight);
        return this;
    }
}
