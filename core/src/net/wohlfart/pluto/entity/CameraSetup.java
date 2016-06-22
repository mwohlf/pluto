package net.wohlfart.pluto.entity;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.controller.CamRobotInput;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.properties.HasBehavior;
import net.wohlfart.pluto.scene.properties.HasCamera;
import net.wohlfart.pluto.scene.properties.HasUid;
import net.wohlfart.pluto.scene.properties.IsSteerable;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;

@EntityElement(type = "Cam")
public class CameraSetup implements IEntityCommand {

    protected float nearFrustum = 10f;

    protected float farFrustum = 1000f;

    protected float fieldOfView = 67f;

    private long uid = HasUid.NULL_UID;

    private IBehavior behavior;

    private CamRobotInput robotInput;

    private final Vector3 forward = new Vector3(Vector3.Z).scl(-1);
    private final Vector3 up = new Vector3(Vector3.Y);

    @Override
    public long getUid() {
        assert this.uid != HasUid.NULL_UID : "uid is invalid for CameraSetup";
        return uid;
    }

    @EntityProperty(name = "uid", type = "Long")
    public CameraSetup withUid(long uid) {
        assert this.uid == HasUid.NULL_UID : "uid reset for CameraSetup was " + this.uid;
        assert uid != HasUid.NULL_UID : "uid setting invalid value for CameraSetup " + uid;
        this.uid = uid;
        return this;
    }

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = entityPool.createEntity();

        robotInput = new CamRobotInput(entity, entityPool);

        entity.add(robotInput.getHasPosition());
        entity.add(robotInput.getHasRotation());
        entity.add(entityPool.createComponent(HasCamera.class).withCamera(setupCam()));
        entity.add(entityPool.createComponent(IsSteerable.class)
                .withForward(forward)
                .withUp(up));

        if (behavior != null) {
            entity.add(entityPool.createComponent(HasBehavior.class)
                    .withBehavior(behavior));
        }

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
    }

    private Camera setupCam() {
        final Camera cam = new Camera(fieldOfView, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), robotInput);
        cam.position.set(0f, 0f, 0f);
        cam.lookAt(forward);
        cam.near = nearFrustum;
        cam.far = farFrustum;
        //robotInput.getHasRotation().getRotation().set(Vector3.Y, 180);
        //cam.rotate(Vector3.Y, 180);
        cam.update(true);
        return cam;
    }

    @EntityProperty(name = "nearFrustum", type = "Float")
    public CameraSetup withNearFrustum(float nearFrustum) {
        this.nearFrustum = nearFrustum;
        return this;
    }

    @EntityProperty(name = "farFrustum", type = "Float")
    public CameraSetup withFarFrustum(float farFrustum) {
        this.farFrustum = farFrustum;
        return this;
    }

    @EntityProperty(name = "fieldOfView", type = "Float")
    public CameraSetup withFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        return this;
    }

    @EntityProperty(name = "behavior", type = "Behavior")
    public CameraSetup withBehavior(IBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    @Override
    public String toString() {
        return "CameraSetup ["
                + "nearFrustum=" + nearFrustum + ", "
                + "farFrustum=" + farFrustum + ", "
                + "fieldOfView=" + fieldOfView + ", "
                + "uid=" + uid + ", "
                + "behavior=" + behavior + ", "
                + "robotInput=" + robotInput // might be null
                + "]";
    }

}
