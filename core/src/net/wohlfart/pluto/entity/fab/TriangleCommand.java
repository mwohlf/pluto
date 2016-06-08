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
import com.badlogic.gdx.math.MathUtils;

import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.stage.loader.EntityElement;

@EntityElement(type = "Triangle")
public class TriangleCommand extends AbstractEntityCommand<TriangleCommand> {

    public float length = Float.NaN;

    // calculated in async method, used in sync
    private float[] vertices;

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        vertices = new float[] {
                MathUtils.cosDeg(90) * this.length, MathUtils.sinDeg(90) * this.length, 0, Color.toFloatBits(255, 0, 0, 255),
                MathUtils.cosDeg((90 + 360 / 3)) * this.length, MathUtils.sinDeg((90 + 360 / 3)) * this.length, 0, Color.toFloatBits(0, 255, 0, 255),
                MathUtils.cosDeg((90 - 360 / 3)) * this.length, MathUtils.sinDeg((90 - 360 / 3)) * this.length, 0, Color.toFloatBits(0, 0, 255, 255),
        };
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);

        final Mesh mesh = new Mesh(true, 3, 3, // isStatic, sizes
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)); // TODO: why 4 its just a single float??

        mesh.setVertices(vertices);
        mesh.setIndices(new short[] { 0, 1, 2 });

        final MeshPart meshPart = new MeshPart(Long.toString(getUid(), Character.MAX_RADIX), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);

        final NodePart nodePart = new NodePart(meshPart, new Material());

        final Node node = new Node();
        node.parts.add(nodePart);

        final Model model = new Model();
        model.nodes.add(node);

        final ModelInstance element = new ModelInstance(model);

        entity.add(entityPool.createComponent(HasRenderables.class)
                .withDelegate(element));

        entity.add(entityPool.createComponent(HasTransformMethod.class)
                .withSetterTransformMethod(element.transform));

        makePickable(entityPool, element, entity);

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    @EntityProperty(name = "length", type = "Float")
    public TriangleCommand withLength(float length) {
        this.length = length;
        return this;
    }

}
