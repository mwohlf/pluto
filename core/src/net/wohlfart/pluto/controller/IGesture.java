package net.wohlfart.pluto.controller;

import net.wohlfart.pluto.controller.gesture.Touch;

public interface IGesture extends ITransformCalculator {

    // called initially when gesture might be started
    void offer(Touch digitTouch);

    // called when gesture is stopped
    void cancel();

    // check if a gesture is still in progress and delivers a transform not equal to IDT
    boolean isInProgress();

}
