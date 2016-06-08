package net.wohlfart.pluto.controller.gesture;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.controller.IGesture;
import net.wohlfart.pluto.controller.ITransformCalculator;
import net.wohlfart.pluto.controller.gesture.Touch.Action;

public class GestureLongTap implements ITransformCalculator, IGesture {

    private static final Vector3 FORWARD_VEC = new Vector3().set(Vector3.Z);

    private static final float ROTATION_SPEED = 50f;
    private static final float MOVE_SPEED = 5f;

    private final Camera cam;

    private final Touch pointerPosition;

    private final Matrix4 transform = new Matrix4().idt();

    private final Vector3 tmpVector = new Vector3();
    private final Quaternion tmpQuaternion = new Quaternion();

    private float start = Long.MAX_VALUE;

    public GestureLongTap(Camera cam) {
        this.cam = cam;
        pointerPosition = new Touch();
    }

    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        // startTime < now < stopTime
        if (start > now) {
            return ITransformCalculator.IDT_MATRIX;
        } else {
            return transform(deltaSeconds);
        }
    }

    @Override
    public void offer(Touch touch) {
        if (touch.isOneOf(Action.DOWN)) {
            start = touch.time + Touch.DELAY_TRIGGER_MSEC;
            pointerPosition.set(touch);
        }
        if (touch.isOneOf(Action.MOVE)) {
            pointerPosition.set(touch);
        }
        if (touch.isOneOf(Action.UP)) {
            start = Long.MAX_VALUE;
            pointerPosition.consume();
        }
    }

    private Matrix4 transform(float deltaSeconds) {
        tmpVector.set(pointerPosition).z = 1;
        cam.unproject(tmpVector).nor();
        tmpQuaternion.setFromCross(GestureLongTap.FORWARD_VEC, tmpVector);
        transform.set(tmpQuaternion);
        // transform.translate(tmpVector.scl(-GestureLongTap.MOVE_SPEED * deltaSeconds));
        return transform;
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInProgress() {
        // TODO Auto-generated method stub
        return false;
    }

}
