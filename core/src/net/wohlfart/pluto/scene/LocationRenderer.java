package net.wohlfart.pluto.scene;

import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;

import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.shader.SceneShaderProvider;

public class LocationRenderer implements Disposable {

    private final ModelBatch modelBatch;

    @SuppressWarnings("unchecked")
    private final Family locationEntities = Family.all(HasPosition.class, HasRotation.class).get();

    private final Environment environment;

    private final SceneGraphElementPool entityPool;

    LocationRenderer(Environment environment, SceneGraphElementPool entityPool) {
        modelBatch = new ModelBatch(null, new SceneShaderProvider(), new SceneRenderableSorter());
        this.environment = environment;
        this.entityPool = entityPool;
    }

    public void render() {
        modelBatch.begin(entityPool.getCamera());
        // TODO
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }
}
