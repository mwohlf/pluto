package net.wohlfart.pluto.resource;

import net.wohlfart.pluto.transition.Fade;
import aurelienribon.tweenengine.TweenAccessor;

public class FadeAccessor implements TweenAccessor<Fade> {

    public static final int OPACITY = 0;

    @Override
    public int getValues(Fade target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case OPACITY:
                returnValues[0] = target.getOpacity();
                return 1;
            default:
                assert false : "<getValues> unknown tween type: " + tweenType;
                return -1;
        }
    }

    @Override
    public void setValues(Fade target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case OPACITY:
                target.setOpacity(newValues[0]);
                break;
            default:
                assert false : "<setValues> unknown tween type: " + tweenType;
        }

    }

}
