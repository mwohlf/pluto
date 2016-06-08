package net.wohlfart.pluto.stage.loader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.wohlfart.pluto.CustomAssert;
import net.wohlfart.pluto.ai.MoveToBehavior;
import net.wohlfart.pluto.ai.btree.IBehavior;
import net.wohlfart.pluto.entity.Callback;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.properties.HasBehavior;
import net.wohlfart.pluto.scene.properties.HasLightMethod;
import net.wohlfart.pluto.scene.properties.HasPosition;

@SuppressFBWarnings(justification = "hack for testclasses")
@SuppressWarnings({ "FieldCanBeLocal", "InstanceofInterfaces" })
public class JsonConverterTest {

    // the unit under test
    private JsonConverter jsonConverter;

    private ResourceManager resourceManager;
    private SceneGraph graph;
    private Entity entity;
    private Environment environment;

    @Before
    public void setup() {
        jsonConverter = new JsonConverter();
        resourceManager = new ResourceManager(new ClasspathFileHandleResolver());
        graph = new SceneGraph(resourceManager);
        environment = new Environment();
    }

    @Ignore
    @Test
    public void testLight() throws Exception {
        final JsonValue json = new JsonReader().parse(""
                + "{001: {"
                + "   type: light,"
                + "   color: \"0.5,0.5,0.5,1\""
                + "}}").child;
        final CountDownLatch latch = new CountDownLatch(1);
        entity = null;
        jsonConverter.runEntityCommand(graph, json, new Callback<Entity>() {
            @Override
            public void ready(Entity created) {
                entity = created;
                Assert.assertNotNull(entity);
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        Assert.assertNotNull(entity);
        entity.getComponent(HasLightMethod.class).getLightMethod().register(environment);
        final Attribute attribute = environment.get(ColorAttribute.AmbientLight);
        Assert.assertNotNull(attribute);
    }

    @Ignore
    @Test
    public void testBox() throws Exception {
        final JsonValue json = new JsonReader().parse(""
                + "{001: {"
                + "   type: box,"
                + "   length: 5,"
                + "   rotation: {axis: \"0,0,1\", degree: 45},"
                + "   texture: \"texture/ash_uvgrid01.png\","
                + "   position: \"0,0,-70\","
                + "   behavior: {"
                + "      spin: {axis: \"0,1,0\", angle: 10},"
                + "      spin: {axis: \"1,0,0\", angle: 20},"
                + "   }"
                + "}}").child;
        final CountDownLatch latch = new CountDownLatch(1);
        entity = null;
        jsonConverter.runEntityCommand(graph, json, new Callback<Entity>() {
            @Override
            public void ready(Entity created) {
                entity = created;
                Assert.assertNotNull(entity);
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        CustomAssert.assertEquals(new Position(0, 0, -70), entity.getComponent(HasPosition.class).getPosition());
        final IBehavior<?> behavior = entity.getComponent(HasBehavior.class).getBehavior();
        Assert.assertNotNull(behavior);
    }

    @Ignore
    @Test
    public void testMoveToBehavior() throws Exception {
        final JsonValue json = new JsonReader().parse(""
                + "{001: {"
                + "   type: box,"
                + "   length: 5,"
                + "   rotation: {axis: \"0,0,1\", degree: 45},"
                + "   texture: \"texture/ash_uvgrid01.png\","
                + "   position: \"0,0,-70\","
                + "   behavior: {"
                + "     moveTo: {"
                + "       speed: 5,"
                + "       waypoint: {"
                + "         uid: 2001,"
                + "         position: \"+50,0,-70\","
                + "       }"
                + "     },"
                + "   }"
                + "}}").child;
        final CountDownLatch latch = new CountDownLatch(1);
        entity = null;
        jsonConverter.runEntityCommand(graph, json, new Callback<Entity>() {
            @Override
            public void ready(Entity created) {
                entity = created;
                Assert.assertNotNull(entity);
                Assert.assertNotNull(entity.getComponent(HasPosition.class));
                Assert.assertNotNull(entity.getComponent(HasPosition.class).getPosition());
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        CustomAssert.assertEquals(new Position(0, 0, -70), entity.getComponent(HasPosition.class).getPosition());
        final IBehavior<?> behavior = entity.getComponent(HasBehavior.class).getBehavior();
        Assert.assertNotNull(behavior);
        Assert.assertTrue(behavior instanceof MoveToBehavior);
        final Entity waypoint = graph.findEntity(2001).get();
        Assert.assertNotNull(waypoint);
        //behaviorExecutor.tick(1f, graph);
        //Assert.assertEquals(1, behaviorExecutor.getSize());
    }

}
