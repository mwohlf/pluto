package net.wohlfart.pluto.scene;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;

import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.shader.SceneShaderProvider;

public class ModelRenderLayer implements Disposable {

    private final ModelBatch modelBatch;

    @SuppressWarnings("unchecked")
    private final Family renderableEntities = Family.all(HasRenderables.class).get();

    private final Environment environment;

    private final SceneGraphElementPool entityPool;

    ModelRenderLayer(Environment environment, SceneGraphElementPool entityPool) {
        modelBatch = new ModelBatch(null, new SceneShaderProvider(), new SceneRenderableSorter());
        this.environment = environment;
        this.entityPool = entityPool;
    }

    public void render() {
        modelBatch.begin(entityPool.getCamera());
        // pick the renderables from the pool
        final ImmutableArray<Entity> renderables = entityPool.getEntitiesFor(renderableEntities);
        for (final Entity elem : renderables) {
            final HasRenderables renderable = elem.getComponent(HasRenderables.class);
            modelBatch.render(renderable, environment);
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }
}
