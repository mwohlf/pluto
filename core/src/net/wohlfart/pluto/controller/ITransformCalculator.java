package net.wohlfart.pluto.controller;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

/**
 * for pulling a transform matrix from some kind of user input or from a robot
 */
public interface ITransformCalculator {

    Matrix4 IDT_MATRIX = new Matrix4().idt();
    Quaternion IDT_QUATERNION = new Quaternion().idt();

    Matrix4 calculateTransform(long now, float deltaSeconds);

}
