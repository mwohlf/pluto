package net.wohlfart.pluto.scene;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;

public interface LightMethod {

    void register(Environment environment);

    void unregister(Environment environment);

    LightMethod NULL = new LightMethod() {

        @Override
        public void register(Environment environment) {
            // do nothing
        }

        @Override
        public void unregister(Environment environment) {
            // do nothing
        }

    };

    final class Ambient implements LightMethod {
        private final ColorAttribute ambientLight;

        public Ambient(ColorAttribute ambientLight) {
            this.ambientLight = ambientLight;
        }

        @Override
        public void register(Environment environment) {
            environment.set(ambientLight);
        }

        @Override
        public void unregister(Environment environment) {
            environment.remove(ambientLight.type);
        }

    }

    final class Directional implements LightMethod {
        private final BaseLight<DirectionalLight> directionalLight;

        public Directional(BaseLight<DirectionalLight> directionalLight) {
            this.directionalLight = directionalLight;
        }

        @Override
        public void register(Environment environment) {
            environment.add(directionalLight); // TODO: simplify by using the attribute class
        }

        @Override
        public void unregister(Environment environment) {
            environment.remove(directionalLight);
        }

    }

    final class Point implements LightMethod {
        private final BaseLight<PointLight> pointLight;

        public Point(BaseLight<PointLight> pointLight) {
            this.pointLight = pointLight;
        }

        @Override
        public void register(Environment environment) {
            environment.add(pointLight); // TODO: simplify by using the attribute class
        }

        @Override
        public void unregister(Environment environment) {
            environment.remove(pointLight);
        }

    }

}
