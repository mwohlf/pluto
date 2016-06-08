package net.wohlfart.pluto.scene.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValueTest {

    @Test
    public void testDouble() {
        final Value doubleValue = Value.of(1d);
        assertTrue(doubleValue.isDouble());
        assertEquals(1d, doubleValue.asDouble(), 0.001);
        assertFalse(doubleValue.isBehavior());
    }

    @Test
    public void testLong() {
        final Value longValue = Value.of(1l);
        assertTrue(longValue.isLong());
        assertEquals((long) 1, (long) longValue.asLong());
        assertFalse(longValue.isBehavior());
    }

}
