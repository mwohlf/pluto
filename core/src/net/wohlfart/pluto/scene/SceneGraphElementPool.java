package net.wohlfart.pluto.scene;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.utils.LongMap;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.ai.btree.BehaviorExecutor;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.scene.properties.HasBehavior;
import net.wohlfart.pluto.scene.properties.HasCamera;
import net.wohlfart.pluto.scene.properties.HasLightMethod;
import net.wohlfart.pluto.scene.properties.HasUid;
import net.wohlfart.pluto.util.Utils;

public class SceneGraphElementPool extends EntityPool {

    // invariant: must only be accessed from the render thread
    private final LongMap<FutureEntity> uids = new LongMap<>();

    // position, perspective, picking, ...
    private Camera camera;

    public SceneGraphElementPool(final Environment environment, final BehaviorExecutor behaviorExecutor) {
        // add to behavior executor if we have behavior
        //noinspection unchecked
        addEntityListener(Family.all(HasBehavior.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                behaviorExecutor.attachBehavior(entity, entity.getComponent(HasBehavior.class).getBehavior());
            }

            @Override
            public void entityRemoved(Entity entity) {
                behaviorExecutor.removeBehavior(entity);
            }
        });
        // add to environment if we have a light
        //noinspection unchecked
        addEntityListener(Family.all(HasLightMethod.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                entity.getComponent(HasLightMethod.class).getLightMethod().register(environment);
            }

            @Override
            public void entityRemoved(Entity entity) {
                entity.getComponent(HasLightMethod.class).getLightMethod().unregister(environment);
            }
        });
        // TODO: check if we can get the cam by its property instead of using this listener
        //noinspection unchecked
        addEntityListener(Family.all(HasCamera.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                camera = entity.getComponent(HasCamera.class).getCamera();
            }

            @Override
            public void entityRemoved(Entity entity) {
                assert camera == entity.getComponent(HasCamera.class).getCamera();
                camera = null;
            }
        });
        //noinspection unchecked
        addEntityListener(Family.all(HasUid.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                assert Utils.isRenderThread();
                final long uid = entity.getComponent(HasUid.class).getUid();
                final boolean success = uids.get(uid).set(entity);
                assert success;
            }

            @Override
            public void entityRemoved(Entity entity) {
                assert Utils.isRenderThread();
                final long uid = entity.getComponent(HasUid.class).getUid();
                uids.remove(uid);
            }
        });
    }

    public FutureEntity register(long uid, SceneGraph sceneGraph) {
        assert Utils.isRenderThread();
        if (uid == 0) {
            return new FutureEntity(sceneGraph);
        }
        // uid != 0
        if (!uids.containsKey(uid)) {
            uids.put(uid, new FutureEntity(sceneGraph));
        }
        return uids.get(uid);
    }

    public Camera getCamera() {
        return camera;
    }

    public void unregister(long uid, Entity entity) {
        assert Utils.isRenderThread();
        uids.remove(uid);
        this.removeEntity(entity);
    }

}
