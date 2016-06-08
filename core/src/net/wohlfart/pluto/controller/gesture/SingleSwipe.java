package net.wohlfart.pluto.controller.gesture;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.controller.IGesture;
import net.wohlfart.pluto.controller.ITransformCalculator;
import net.wohlfart.pluto.controller.gesture.Touch.Action;
import net.wohlfart.pluto.util.Utils;

/**
 * detect incoming left mouse click or single finger tap on the screen and move
 * actions, the move will be translated into an rotation
 *
 * not threadsafe must be only called on the render thread
 */
public class SingleSwipe implements ITransformCalculator, IGesture {

    private final Camera cam;
    private final int digit;

    // encoding the state
    private final Touch startTouch = new Touch(); // initial touch down
    private final Touch startDelta = new Touch(); // start of delta for the current transform
    private final Touch endDelta = new Touch(); // end of delta for the current transform

    // used for calculation
    private final Vector3 previousVector = new Vector3();
    private final Vector3 currentVector = new Vector3();
    // contains the last rotation
    private final Quaternion currentRotation = new Quaternion().idt();
    private final Quaternion endRotation = new Quaternion().idt();

    private final Matrix4 transform = new Matrix4().idt();

    private int slowdownTime;

    public SingleSwipe(Camera cam, int digit) {
        this.cam = cam;
        this.digit = digit;
        reset(Touch.INVALID);
    }

    // setup and prepare the calculation of the transform
    @Override
    public void offer(Touch touch) {
        if (touch.digit == this.digit) {
            process(touch);
        }
    }

    private void process(Touch touch) {
        switch (touch.action) {
            case MOVE:
                if (startDelta.isOneOf(Action.DOWN, Action.MOVE)
                        && Utils.isClose(Touch.FUZZY_SQUARE_DOTS, touch, startDelta)) {
                    endDelta.set(touch);
                }
                break;
            case UP:
                if (startDelta.isOneOf(Action.MOVE)) {
                    endDelta.set(touch);
                }
                break;
            case DOWN:
            default:
                reset(touch);
                break;
        }
    }

    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        switch (endDelta.action) {
            case MOVE:
                assert startDelta.isOneOf(Action.MOVE, Action.DOWN) : "invalid state, current move without valid previous";
                calculateCurrentRotation();
                slowdownTime = Integer.MAX_VALUE;
                startDelta.set(endDelta);
                endDelta.set(Touch.INVALID).time = Long.MIN_VALUE;
                transform.set(currentRotation);
                break;
            case UP:
                if (slowdownTime == Integer.MAX_VALUE) {
                    slowdownTime = (int) (endRotation.getAngle() * 1000f * 3f);
                    calculateEndRotation();
                    transform.set(ITransformCalculator.IDT_MATRIX);
                } else if (endDelta.time > now - slowdownTime) { // using MIN values here, wrong order can cause underflow
                    // might have to slowdown if final stop time is still in the future
                    final float fragment = (float) (now - endDelta.time) / slowdownTime;
                    slowdownCurrentRotation(fragment, deltaSeconds);
                    transform.set(currentRotation);
                } else {
                    transform.set(ITransformCalculator.IDT_MATRIX);
                }
                break;
            default:
                transform.set(ITransformCalculator.IDT_MATRIX);
        }
        return transform;
    }

    // current rotation as derived from the last two touch events, touch times don't matter
    private void calculateCurrentRotation() {
        assert startDelta.isOneOf(Action.MOVE, Action.DOWN) : "invalid state, previous move without valid previous";
        previousVector.set(startDelta).z = 1;
        currentVector.set(endDelta).z = 1;
        currentRotation.setFromCross(cam.unproject(previousVector).nor(), cam.unproject(currentVector).nor());
    }

    //
    private void calculateEndRotation() {
        assert endDelta.time > startTouch.time : "previous touch event out of order";
        assert endDelta.time > startDelta.time : "current touch event out of order";
        previousVector.set(startDelta).z = 1;
        currentVector.set(endDelta).z = 1;
        endRotation.setFromCross(cam.unproject(previousVector).nor(), cam.unproject(currentVector).nor());
        final float currentDeltaTime = endDelta.time - startDelta.time;
        Utils.scale(endRotation, 1f / currentDeltaTime);
    }

    // fragment 0: start of the slowdown period
    // fragment 1: end of the slowdown period
    private void slowdownCurrentRotation(float fragment, float deltaSeconds) {
        assert fragment >= 0 && fragment <= 1 : "fragment is invalid: " + fragment;
        assert deltaSeconds > 0 && deltaSeconds < slowdownTime : "deltaSeconds is invalid: " + deltaSeconds;
        currentRotation.set(endRotation);
        Utils.scale(currentRotation, deltaSeconds * 1000f);
        currentRotation.slerp(ITransformCalculator.IDT_QUATERNION, fragment);
    }

    @Override
    public void cancel() {
        reset(Touch.INVALID);
    }

    private void reset(Touch initial) {
        endRotation.idt();
        startTouch.set(initial);
        startDelta.set(initial);
        endDelta.set(initial);
        slowdownTime = Integer.MIN_VALUE;
    }

    @Override
    public boolean isInProgress() {
        // TODO Auto-generated method stub
        return false;
    }

}
