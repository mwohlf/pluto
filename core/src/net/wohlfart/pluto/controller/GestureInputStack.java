package net.wohlfart.pluto.controller;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Matrix4;
import net.wohlfart.pluto.Camera;
import net.wohlfart.pluto.controller.gesture.DoubleTap;
import net.wohlfart.pluto.controller.gesture.SingleSwipe;
import net.wohlfart.pluto.controller.gesture.Touch;
import net.wohlfart.pluto.util.Utils;

import java.util.ArrayList;
import java.util.Collection;

// TODO: maybe split this into two classes, one for touch, one for gestures...
public class GestureInputStack extends InputAdapter implements ITransformCalculator {

    public static final int MAX_DIGIT = 5;
    public static final int DIGIT_1 = 0;

    // used to propagate what's happening to the Gestures
    static final Touch DIGIT_TOUCH = new Touch();

    static final float SPEED = 0.5f;

    // for pick ray and rotation angle calculation
    final Camera cam;

    private final Collection<IGesture> gestures = new ArrayList<>();

    // final transform matrix
    private final Matrix4 transform = new Matrix4().idt();

    public GestureInputStack(Camera camera, UserEventListener userEventListener) {
        this.cam = camera;
        // supported gestures, collection of what is used atm
        //this.gestures.add(this.longTap);
        this.gestures.add(new DoubleTap(cam, GestureInputStack.DIGIT_1, userEventListener));
        this.gestures.add(new SingleSwipe(cam, GestureInputStack.DIGIT_1));
        //this.gestures.add(new GestureLongTap(cam));
    }

    // collecting the transforms detected by the gestures
    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        transform.idt();
        for (final IGesture gesture : gestures) {
            transform.mulLeft(gesture.calculateTransform(now, deltaSeconds));
        }
        return transform;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        GestureInputStack.DIGIT_TOUCH
                .down()
                .position(screenX, screenY)
                .digit(Math.min(pointer, GestureInputStack.MAX_DIGIT - 1))
                .time(Utils.currentTickCount());
        for (final IGesture gesture : gestures) {
            gesture.offer(GestureInputStack.DIGIT_TOUCH);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        GestureInputStack.DIGIT_TOUCH
                .up()
                .position(screenX, screenY)
                .digit(Math.min(pointer, GestureInputStack.MAX_DIGIT - 1))
                .time(Utils.currentTickCount());
        for (final IGesture gesture : gestures) {
            gesture.offer(GestureInputStack.DIGIT_TOUCH);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        GestureInputStack.DIGIT_TOUCH
                .move().position(screenX, screenY)
                .digit(Math.min(pointer, GestureInputStack.MAX_DIGIT - 1))
                .time(Utils.currentTickCount());
        for (final IGesture gesture : gestures) {
            gesture.offer(GestureInputStack.DIGIT_TOUCH);
        }
        return true;
    }

    public boolean hasTransform() {
        return true; /*longTap.isRunning() ||*/
        //swipe.isRunning();
    }

}
