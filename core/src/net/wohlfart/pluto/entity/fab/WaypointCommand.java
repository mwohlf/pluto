package net.wohlfart.pluto.entity.fab;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;

@EntityElement(type = "Waypoint")
public class WaypointCommand extends AbstractEntityCommand<WaypointCommand> {
    static final float L = 1f;

    private short[] indices;

    private float[] vertices;

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        indices = createIndices((short) (WaypointCommand.ARROW_VERTICES.length / 3));
        vertices = createVertices((short) ((short) (WaypointCommand.ARROW_VERTICES.length / 3) * 7));
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);

        final ModelInstance element = new ModelInstance(createModel());

        entity.add(entityPool.createComponent(HasRenderables.class)
                .withDelegate(element));

        entity.add(entityPool.createComponent(HasTransformMethod.class)
                .withSetterTransformMethod(element.transform));

        makePickable(entityPool, element, entity);

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    private Model createModel() {
        final Mesh mesh = new Mesh(true,
                WaypointCommand.ARROW_VERTICES.length / 3 * 7, WaypointCommand.ARROW_INDICES.length * 3,
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

        mesh.setIndices(indices);
        mesh.setVertices(vertices);

        final MeshPart meshPart = new MeshPart(Long.toString(getUid(), Character.MAX_RADIX), mesh, 0, mesh.getNumIndices(), GL20.GL_LINES);

        final NodePart nodePart = new NodePart(meshPart, new Material());

        final Node node = new Node();
        node.parts.add(nodePart);

        final Model model = new Model();
        model.nodes.add(node);
        return model;
    }

    private short[] createIndices(final short vertCount) {
        final short[] result = new short[WaypointCommand.ARROW_INDICES.length * 3];
        for (int i = 0; i < WaypointCommand.ARROW_INDICES.length; i += 1) {
            result[i + WaypointCommand.ARROW_INDICES.length * 0] = (short) (WaypointCommand.ARROW_INDICES[i] + vertCount * 0);
            result[i + WaypointCommand.ARROW_INDICES.length * 1] = (short) (WaypointCommand.ARROW_INDICES[i] + vertCount * 1);
            result[i + WaypointCommand.ARROW_INDICES.length * 2] = (short) (WaypointCommand.ARROW_INDICES[i] + vertCount * 2);
        }
        return result;
    }

    private float[] createVertices(final short arrowSize) {
        final float[] verts = new float[arrowSize * 3];
        final Color[] colors = { Color.BLUE, Color.GREEN, Color.RED };
        for (int k = 0; k < colors.length; k++) {
            for (int i = 0, j = 0; i < WaypointCommand.ARROW_VERTICES.length; i += 3, j += 7) {
                //noinspection PointlessArithmeticExpression
                verts[j + 0 + arrowSize * k] = WaypointCommand.ARROW_VERTICES[i + ((0 + k) % 3)];
                verts[j + 1 + arrowSize * k] = WaypointCommand.ARROW_VERTICES[i + ((1 + k) % 3)];
                verts[j + 2 + arrowSize * k] = WaypointCommand.ARROW_VERTICES[i + ((2 + k) % 3)];
                verts[j + 3 + arrowSize * k] = colors[k].r; // color
                verts[j + 4 + arrowSize * k] = colors[k].g;
                verts[j + 5 + arrowSize * k] = colors[k].b;
                verts[j + 6 + arrowSize * k] = colors[k].a;
            }
        }
        return verts;
    }

    // vertices[0] is the direction of the arrow
    private static final float[] ARROW_VERTICES = { // @formatter:off
            +0.00f*WaypointCommand.L, +0.00f*WaypointCommand.L, +1.00f*WaypointCommand.L,  // tip is in z direction <-- end
            +0.00f*WaypointCommand.L, +0.00f*WaypointCommand.L, +0.00f*WaypointCommand.L,  // base <-- start
            +0.02f*WaypointCommand.L, +0.02f*WaypointCommand.L, +0.90f*WaypointCommand.L,  // tip right
            -0.02f*WaypointCommand.L, +0.02f*WaypointCommand.L, +0.90f*WaypointCommand.L,  // tip left
            -0.02f*WaypointCommand.L, -0.02f*WaypointCommand.L, +0.90f*WaypointCommand.L,  // tip top
            +0.02f*WaypointCommand.L, -0.02f*WaypointCommand.L, +0.90f*WaypointCommand.L,  // tip bottom
    };

    private static final short[] ARROW_INDICES = {
            1, 0, // shaft
            2, 0, // tip1
            3, 0, // tip2
            4, 0, // tip3
            5, 0, // tip4
    }; // @formatter:on

    @Override
    public String toString() {
        return "WaypointCommand ["
                + position
                + "]";
    }

}
