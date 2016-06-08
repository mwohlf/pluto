package net.wohlfart.pluto.scene.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.scene.Position;

public class ValueAssignTest {

    @Test
    public void smokeTest() throws InterruptedException {
        final String script = ""
                + "a = 2;"
                + "b = false;"
                + "c = false || true;"
                + "d = 5 + 5;"
                + "e = false | true;"
                + "f = false && true;"
                + "g = false & true;";
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(Long.valueOf(2L), scope.resolve("a").asLong());
        assertEquals(Boolean.FALSE, scope.resolve("b").asBoolean());
        assertEquals(Boolean.TRUE, scope.resolve("c").asBoolean());
        assertEquals(new Double(10), scope.resolve("d").asDouble());
        assertEquals(new Long(10), scope.resolve("d").asLong());
        assertEquals(new Float(10), scope.resolve("d").asFloat());
        assertEquals(new Integer(10), scope.resolve("d").asInteger());
        assertEquals(Boolean.TRUE, scope.resolve("e").asBoolean());
        assertEquals(Boolean.FALSE, scope.resolve("f").asBoolean());
        assertEquals(Boolean.FALSE, scope.resolve("g").asBoolean());
    }

    @Test
    public void vectorTest() throws InterruptedException {
        final String script = ""
                + "v1 = Vector{2,3,4};"
                + "v2 = Vector{x:1,z:3,y:2};"
                + "v3 = Vector{z:1};";
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(new Vector3(2, 3, 4), scope.resolve("v1").asVector());
        assertEquals(new Vector3(1, 2, 3), scope.resolve("v2").asVector());
        assertEquals(new Vector3(0, 0, 1), scope.resolve("v3").asVector());
    }

    @Test
    public void positionTest() throws InterruptedException {
        final String script = ""
                + "p1 = Position{2,3,4};"
                + "p2 = Position{x:1,z:3,y:2};"
                + "p3 = Position{z:1};";
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(new Position(2, 3, 4), scope.resolve("p1").asPosition());
        assertEquals(new Position(1, 2, 3), scope.resolve("p2").asPosition());
        assertEquals(new Position(0, 0, 1), scope.resolve("p3").asPosition());
    }

    @Test
    public void quaternionTest() throws InterruptedException {
        final String script = ""
                + "r1 = Rotation{Vector{1,0,0}, angle:2};"
                + "r2 = Rotation{w:4,x:1,z:3,y:2};"
                + "r3 = Rotation{2,3,4,5};";
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(new Quaternion(new Vector3(1, 0, 0), 2.0f), scope.resolve("r1").asQuaternion());
        assertEquals(new Quaternion(1, 2, 3, 4), scope.resolve("r2").asQuaternion());
        assertEquals(new Quaternion(2, 3, 4, 5), scope.resolve("r3").asQuaternion());
    }

    @Test
    public void colorTest() throws InterruptedException {
        final String script = ""
                + "c1 = Color{1, 0, 0, 2};"
                + "c2 = Color{1, 2, 3, 4};"
                + "c3 = Color{a:2, b:3, g:4, r:5};";
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(new Color(1, 0, 0, 2.0f), scope.resolve("c1").asColor());
        assertEquals(new Color(1, 2, 3, 4), scope.resolve("c2").asColor());
        assertEquals(new Color(5, 5, 3, 2), scope.resolve("c3").asColor());
    }

    @Test
    public void booleanTest() throws InterruptedException {
        final String script = ""
                + "b1 = false | true & false;"
                + "b2 = true | false & true;"
                + "b3 = false | false | true;"
                + "b4 = true & true | false & false;"; // order of evaluation
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(Boolean.FALSE, scope.resolve("b1").asBoolean());
        assertEquals(Boolean.TRUE, scope.resolve("b2").asBoolean());
        assertEquals(Boolean.TRUE, scope.resolve("b3").asBoolean());
        assertEquals(Boolean.TRUE, scope.resolve("b4").asBoolean());
    }

    @Test
    public void incDecTest() throws InterruptedException {
        final String script = ""
                + "a = 1;"
                + "a =+ 1;"
                + "b = 0.1;"
                + "b =+ 0.1;"
                + "c = 2;"
                + "c =- 1;"
                + "d = 0.2;"
                + "d =- 0.1;";
        final Scope scope = new MockEvalScope().invoke(script);
        assertEquals(new Long(2), scope.resolve("a").asLong());
        assertEquals(new Double(0.2), scope.resolve("b").asDouble());
        assertEquals(new Long(1), scope.resolve("c").asLong());
        assertEquals(new Double(0.1), scope.resolve("d").asDouble());
    }
}
