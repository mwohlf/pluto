package net.wohlfart.pluto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.util.Utils;

public class UtilTest {

    @Test
    public void smokeTest() throws InterruptedException {
        assertEquals(Vector3.Z, Utils.getZVector(new Quaternion()));
        assertEquals(Vector3.Y, Utils.getYVector(new Quaternion()));
        assertEquals(Vector3.X, Utils.getXVector(new Quaternion()));
    }
}
