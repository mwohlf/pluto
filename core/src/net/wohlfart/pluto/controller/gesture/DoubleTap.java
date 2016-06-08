package net.wohlfart.pluto.controller.gesture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.controller.IGesture;
import net.wohlfart.pluto.controller.ITransformCalculator;
import net.wohlfart.pluto.controller.UserEventListener;
import net.wohlfart.pluto.controller.gesture.Touch.Action;
import net.wohlfart.pluto.util.Utils;

/*
 * remember the last touch downs for each digit and trigger an event if a double touch has been detected
 */
public class DoubleTap implements ITransformCalculator, IGesture {

    private static final long TIMEOUT_MS = 400;
    private static final float RANGE_DOTS = 20;

    private final Touch lastTouchDown = new Touch();

    private final Camera cam;
    private final int digit;
    private final UserEventListener userEventListener;

    public DoubleTap(Camera cam, int digit, UserEventListener userEventListener) {
        this.cam = cam;
        this.digit = digit;
        this.userEventListener = userEventListener;
    }

    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        return ITransformCalculator.IDT_MATRIX;
    }

    @Override
    public void offer(Touch current) {
        if (current.isOneOf(Action.DOWN)
                && current.digit == this.digit) {
            process(current);
        }
    }

    private void process(Touch current) {
        if (lastTouchDown.isOneOf(Touch.Action.DOWN)
                && Utils.isClose(DoubleTap.RANGE_DOTS / Gdx.graphics.getDensity(), lastTouchDown, current)
                && DoubleTap.TIMEOUT_MS > current.time - lastTouchDown.time) {
            userEventListener.pick(cam.getPickRay(current.x, current.y));
            current.consume();
            lastTouchDown.consume();
        } else {
            lastTouchDown.set(current);
        }

    }

    @Override
    public void cancel() {
        lastTouchDown.consume();
    }

    @Override
    public boolean isInProgress() {
        return false;
    }

}
