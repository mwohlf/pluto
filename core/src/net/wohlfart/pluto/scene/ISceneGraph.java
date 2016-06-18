package net.wohlfart.pluto.scene;

import java.util.concurrent.Executor;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.entity.PickSystem;

public interface ISceneGraph extends Disposable, Executor {

    // entity creation

    FutureEntity create(IEntityCommand comand);

    FutureEntity findEntity(long uid);

    long getTaskCount();

    void destroy(long uid, Entity entity);

    // rendering

    void resize(int width, int height);

    void update(float deltaTime, Matrix4 calculateTransform);

    void render();

    // cam and picking

    Camera getCamera();

    PickSystem getPickSystem();

    IPickFacade getPickFacade();

}
