package net.wohlfart.pluto.entity.fab;

import java.util.Iterator;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.Logging;
import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.lang.EntityProperty;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.util.Utils;

// see: http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
@EntityElement(type = "Icosphere")
public class IcosphereCommand extends AbstractEntityCommand<IcosphereCommand> {

    public float radius = 1;
    public Color color = new Color().set(0.5f, 0.9f, 0.5f, 1f);
    public int primitiveType = GL20.GL_LINES; // GL20.GL_TRIANGLES

    // created async
    private float[] verticesBuffer;
    private short[] indicesBuffer;

    private final Array<VertexAttribute> attributes = new Array<>(
            new VertexAttribute[] {
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE)
            });

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert !Utils.isRenderThread();
        this.verticesBuffer = createVerticesBuffer();
        this.indicesBuffer = createIndicesBuffer();
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);

        final NodePart nodePart = new NodePart(createMeshPart(), new Material(ColorAttribute.createDiffuse(color)));

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

    private MeshPart createMeshPart() {
        final VertexAttribute[] items = new VertexAttribute[attributes.size];
        final Iterator<VertexAttribute> iter = attributes.iterator();
        int i = 0;
        while (iter.hasNext()) {
            items[i++] = iter.next();
        }

        final Mesh mesh = new Mesh(true,
                this.verticesBuffer.length / getVertexSize(), // vertices count
                this.indicesBuffer.length, // index count
                items);

        mesh.setVertices(this.verticesBuffer);
        mesh.setIndices(this.indicesBuffer);

        return new MeshPart(Long.toString(getUid(), Character.MAX_RADIX), mesh, 0, mesh.getNumIndices(), this.primitiveType);
    }

    public IcosphereCommand withNormalAttribute() {
        return withAttribute(new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
    }

    @EntityProperty(name = "color", type = "Color")
    public IcosphereCommand withColor(Color color) {
        this.color = new Color().set(color);
        return this;
    }

    @EntityProperty(name = "attribute", type = "VertexAttribute")
    public IcosphereCommand withAttribute(VertexAttribute vertexAttribute) {
        attributes.add(vertexAttribute);
        return this;
    }

    @EntityProperty(name = "radius", type = "Float")
    public IcosphereCommand withRadius(float radius) {
        this.radius = radius;
        return this;
    }

    @EntityProperty(name = "primitive", type = "Primitive")
    public IcosphereCommand withPrimitiveType(int primitiveType) {
        this.primitiveType = primitiveType;
        return this;
    }

    private float[] createVerticesBuffer() {
        final int vertexSize = getVertexSize();
        final int initialVerticesCount = IcosphereCommand.INI_VERT_BUFF.length / IcosphereCommand.INI_VERT_SIZE;
        final float[] result = new float[initialVerticesCount * vertexSize];
        final Vector3 pos = new Vector3();
        for (int i = 0; i < initialVerticesCount; i++) {
            //noinspection PointlessArithmeticExpression
            pos.set(
                    IcosphereCommand.INI_VERT_BUFF[i * IcosphereCommand.INI_VERT_SIZE + 0],
                    IcosphereCommand.INI_VERT_BUFF[i * IcosphereCommand.INI_VERT_SIZE + 1],
                    IcosphereCommand.INI_VERT_BUFF[i * IcosphereCommand.INI_VERT_SIZE + 2]).nor();
            int o = 0;
            for (final VertexAttribute attribute : attributes) {
                switch (attribute.usage) {
                    case Usage.Position:
                        //noinspection PointlessArithmeticExpression
                        result[i * vertexSize + o + 0] = pos.x * this.radius;
                        result[i * vertexSize + o + 1] = pos.y * this.radius;
                        result[i * vertexSize + o + 2] = pos.z * this.radius;
                        break;
                    case Usage.Normal:
                        //noinspection PointlessArithmeticExpression
                        result[i * vertexSize + o + 0] = pos.x;
                        result[i * vertexSize + o + 1] = pos.y;
                        result[i * vertexSize + o + 2] = pos.z;
                        break;
                    case Usage.ColorUnpacked:
                    case Usage.ColorPacked:
                    case Usage.TextureCoordinates:
                    default:
                        Logging.ROOT.error("<createScaledVertices> unknown attribute usage: '" + attribute.usage + "' ignoring");
                }
                o += attribute.numComponents;
            }
        }
        return result;
    }

    private int getVertexSize() {
        int vertexSize = 0;
        for (final VertexAttribute attribute : attributes) {
            vertexSize += attribute.numComponents;
        }
        return vertexSize;
    }

    private short[] createIndicesBuffer() {
        switch (this.primitiveType) {
            case GL20.GL_LINES:
                return createLineIndices();
            case GL20.GL_TRIANGLES:
                return createTriangleIndices();
            default:
                Logging.ROOT.error("<createMeshPart> failed for primitiveType: '" + this.primitiveType + "' using empty index buffer");
                return new short[] {};
        }
    }

    private short[] createLineIndices() {
        final short[] result = new short[IcosphereCommand.LINE_INDICES.length];
        System.arraycopy(IcosphereCommand.LINE_INDICES, 0, result, 0, IcosphereCommand.LINE_INDICES.length);
        return result;
    }

    private short[] createTriangleIndices() {
        final short[] result = new short[IcosphereCommand.LINE_INDICES.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = IcosphereCommand.LINE_INDICES[i * 2];
        }
        return result;
    }

    // @formatter:off
    private static final int INI_VERT_SIZE = 3;
    private static final float t = (float) ((1.0f + Math.sqrt(5.0f)) / 2.0f);
    private static final float[] INI_VERT_BUFF = {
            -1f, +IcosphereCommand.t, +0,
            +1f, +IcosphereCommand.t, +0,
            -1f, -IcosphereCommand.t, +0,
            +1f, -IcosphereCommand.t, +0,
            +0f, -1, +IcosphereCommand.t,
            +0f, +1, +IcosphereCommand.t,
            +0f, -1, -IcosphereCommand.t,
            +0f, +1, -IcosphereCommand.t,
            +IcosphereCommand.t,  +0, -1,
            +IcosphereCommand.t,  +0, +1,
            -IcosphereCommand.t,  +0, -1,
            -IcosphereCommand.t,  +0, +1,
    };

    // lines
    private static final short[] LINE_INDICES = {
            0, 11, 11, 5, 5, 0,
            0, 5, 5, 1, 1, 0,
            0, 1, 1, 7, 7, 0,
            0, 7, 7,10,10, 0,
            0, 10,10,11,11, 0,
            1, 5, 5, 9, 9, 1,
            5, 11,11, 4, 4, 5,
            11,10,10, 2, 2,11,
            10, 7, 7, 6, 6,10,
            7, 1, 1, 8, 8, 7,
            3, 9, 9, 4, 4, 3,
            3, 4, 4, 2, 2, 3,
            3, 2, 2, 6, 6, 3,
            3, 6, 6, 8, 8, 3,
            3, 8, 8, 9, 9, 3,
            4, 9, 9, 5, 5, 4,
            2, 4, 4,11,11, 2,
            6, 2, 2,10,10, 6,
            8, 6, 6, 7, 7, 8,
            9, 8, 8, 1, 1, 9,
    };
    // @formatter:on

}
