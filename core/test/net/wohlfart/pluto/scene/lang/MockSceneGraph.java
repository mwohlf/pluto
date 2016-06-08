package net.wohlfart.pluto.scene.lang;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.PickSystem;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.IPickFacade;
import net.wohlfart.pluto.scene.ISceneGraph;

public class MockSceneGraph implements ISceneGraph {

    @Override
    public void dispose() {
    }

    @Override
    public void execute(Runnable command) {
    }

    @Override
    public FutureEntity create(IEntityCommand comand) {
        return new FutureEntity(this);
    }

    @Override
    public FutureEntity findEntity(long uid) {
        return null;
    }

    @Override
    public long getProcessingCount() {
        return 0;
    }

    @Override
    public void destroy(long uid, Entity entity) {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void update(float deltaTime, Matrix4 calculateTransform) {
    }

    @Override
    public void render() {
        // TODO Auto-generated method stub

    }

    @Override
    public Camera getCamera() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PickSystem getPickSystem() {
        return null;
    }

    @Override
    public IPickFacade getPickFacade() {
        return null;
    }

}
