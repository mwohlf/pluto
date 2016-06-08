package net.wohlfart.pluto.scene.lang;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BehaviorAssignTest {

    @Test
    public void smokeTest() throws InterruptedException {
        final String script = ""
                + "waypoint = Waypoint {"
                + "      uid: 1,"
                + "      position: Position{x:0.0, y:30.0, z:-200.0}"
                + "};";

        final Scope scope = new MockEvalScope().invoke(script);
        final Value<?> path = scope.resolve("waypoint");
        assertNotNull(path);
    }

    @Test
    public void simpleTest() throws InterruptedException {
        final String script = ""
                + "waypoint = Waypoint {"
                + "      uid: 1,"
                + "      position: Position{x:0.0, y:30.0, z:-200.0}"
                + "};";

        final Scope scope = new MockEvalScope().invoke(script);
        final Value<?> path = scope.resolve("waypoint");
        assertNotNull(path);
    }

    @Test
    public void parallelTest() throws InterruptedException {
        final String script = ""
                + "waypoint = Waypoint {"
                + "      uid: 1,"
                + "      position: Position{x:0.0, y:30.0, z:-200.0}"
                + "};";

        final Scope scope = new MockEvalScope().invoke(script);
        final Value<?> path = scope.resolve("waypoint");
        assertNotNull(path);
    }

    @Test
    public void sequentialTest() throws InterruptedException {
        final String script = ""
                + "waypoint = Waypoint {"
                + "      uid: 1,"
                + "      position: Position{x:0.0, y:30.0, z:-200.0}"
                + "};";

        final Scope scope = new MockEvalScope().invoke(script);
        final Value<?> path = scope.resolve("waypoint");
        assertNotNull(path);
    }

}
