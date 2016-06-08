package net.wohlfart.pluto.resource;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.audio.Music;

public class MusicAccessor implements TweenAccessor<Music> {

    public static final int VOLUME = 0;

    @Override
    public int getValues(Music target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case VOLUME:
                returnValues[0] = target.getVolume();
                return 1;
            default:
                assert false : "<getValues> unknown tween type: " + tweenType;
                return -1;
        }
    }

    @Override
    public void setValues(Music target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case VOLUME:
                target.setVolume(newValues[0]);
                break;
            default:
                assert false : "<setValues> unknown tween type: " + tweenType;
        }

    }

}
