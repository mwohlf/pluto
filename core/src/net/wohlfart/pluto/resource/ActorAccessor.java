package net.wohlfart.pluto.resource;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActorAccessor implements TweenAccessor<Actor> {
    public static final int IS_VISIBLE_VALUE = 1;
    public static final int IS_NOT_VISIBLE_VALUE = 0;

    public static final int POSITION = 0;
    public static final int VISIBLE = 1;

    @Override
    public int getValues(Actor target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case POSITION:
                returnValues[0] = target.getX();
                returnValues[1] = target.getY();
                return 2;
            case VISIBLE:
                returnValues[0] = target.isVisible() ? IS_VISIBLE_VALUE : IS_NOT_VISIBLE_VALUE;
                return 1;
            default:
                assert false : "<getValues> unknown tween type: " + tweenType;
                return -1;
        }
    }

    @Override
    public void setValues(Actor target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case POSITION:
                target.setPosition(newValues[0], newValues[1]);
                break;
            case VISIBLE:
                target.setVisible(newValues[0] == IS_VISIBLE_VALUE);
                break;
            default:
                assert false : "<setValues> unknown tween type: " + tweenType;
        }

    }

}
