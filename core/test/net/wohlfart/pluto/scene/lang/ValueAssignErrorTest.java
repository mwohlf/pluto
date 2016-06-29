package net.wohlfart.pluto.scene.lang;

import org.junit.Test;

public class ValueAssignErrorTest {

    @Test(expected = EvalException.class)
    public void unknownTest() throws InterruptedException {
        final String script = ""
                + "a = c;";
        final Scope scope = new MockEvalScope().invoke(script);
    }

    @Test(expected = EvalException.class)
    public void missingSemicolonTest() throws InterruptedException {
        final String script = ""
                + "a = c";
        final Scope scope = new MockEvalScope().invoke(script);
    }

}
