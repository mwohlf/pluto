package net.wohlfart.pluto;

import org.junit.Assert;

import com.badlogic.gdx.math.Matrix4;

import net.wohlfart.pluto.controller.ITransformCalculator;
import net.wohlfart.pluto.scene.Position;

public final class CustomAssert {

    public static final float DELTA = 0.00001f;

    public static void assertRotateOnly(Matrix4 matrix) {
        Assert.assertEquals(1f, matrix.getScaleX(), DELTA);
        Assert.assertEquals(1f, matrix.getScaleY(), DELTA);
        Assert.assertEquals(1f, matrix.getScaleZ(), DELTA);
    }

    public static void assertIdt(Matrix4 matrix) {
        for (int i = 0; i < ITransformCalculator.IDT_MATRIX.val.length; i++) {
            Assert.assertEquals(ITransformCalculator.IDT_MATRIX.val[i], matrix.val[i], DELTA);
        }
    }

    public static void assertEquals(Position pos1, Position pos2) {
        Assert.assertEquals(pos1.x, pos2.x, DELTA);
        Assert.assertEquals(pos1.y, pos2.y, DELTA);
        Assert.assertEquals(pos1.z, pos2.z, DELTA);
    }

}
