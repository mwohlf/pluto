package net.wohlfart.pluto.entity.fab;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.scene.properties.IsSteerable;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;

// TODO: load files async
@EntityElement(type = "Object")
public class ObjectCommand extends AbstractEntityCommand<ObjectCommand> {

    final ObjLoader loader = new ObjLoader();

    protected String fileName;

    /*

    TODO: async loading doesn't work
    this is broken

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool) {
        final ObjLoader.ObjLoaderParameters params = new ObjLoader.ObjLoaderParameters(false);
        params.loadedCallback = new LoadedCallback() {
            @Override
            public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
                final ModelInstance element = new ModelInstance(assetManager.get(fileName, Model.class));
                finished(element, resourceManager, entityPool);
            }
        };
        resourceManager.load(this.fileName, Model.class, params);
        resourceManager.finishLoading();
    }
     */

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);

        final Model model = loader.loadModel(Gdx.files.internal(fileName));

        final ModelInstance element = new ModelInstance(model);

        entity.add(entityPool.createComponent(HasRenderables.class)
                .withDelegate(element));

        entity.add(entityPool.createComponent(HasTransformMethod.class)
                .withSetterTransformMethod(element.transform));

        entity.add(entityPool.createComponent(IsSteerable.class)
                .withForward(new Vector3(Vector3.X)));

        makePickable(entityPool, element, entity);

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    @EntityProperty(name = "file", type = "String")
    public ObjectCommand withFile(String fileName) {
        this.fileName = fileName;
        return this;
    }

}
