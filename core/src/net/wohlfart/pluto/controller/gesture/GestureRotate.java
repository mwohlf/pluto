package net.wohlfart.pluto.controller.gesture;

import com.badlogic.gdx.math.Matrix4;

import net.wohlfart.pluto.controller.IGesture;
import net.wohlfart.pluto.controller.ITransformCalculator;

// see: http://stackoverflow.com/questions/10682019/android-two-finger-rotation
public class GestureRotate implements ITransformCalculator, IGesture {

    @Override
    public void offer(Touch digitTouch) {
        // TODO Auto-generated method stub
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

    @Override
    public Matrix4 calculateTransform(long now, float deltaSeconds) {
        // TODO Auto-generated method stub
        return null;
    }

}
