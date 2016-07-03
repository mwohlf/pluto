package net.wohlfart.pluto.entity.fab;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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

@EntityElement(type = "Box")
public class CubeCommand extends AbstractEntityCommand<CubeCommand> {

    Color color;
    String textureFile;
    float length = Float.NaN;

    // calculated in async method, used in sync
    private float[] vertices;

    private final Array<VertexAttribute> attributes = new Array<>(
            new VertexAttribute[] {
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"
                    new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"
            });

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert (color == null ^ textureFile == null) : "can't use texture and color on cube";
        if (textureFile != null) {
            resourceManager.load(this.textureFile, Texture.class);
        }
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert (!Float.isNaN(this.length)) : "need to set length on cube";
        final int vertexSize = vertexSize();
        final int vertexCount = CubeCommand.BOX_VERTICES.length / 8;
        vertices = new float[vertexCount * vertexSize];
        for (int i = 0; i < vertexCount; i += 1) {
            int o = 0;
            for (final VertexAttribute attribute : attributes) {
                switch (attribute.usage) {
                    case Usage.Position:
                        //noinspection PointlessArithmeticExpression
                        vertices[i * vertexSize + o + 0] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 0] * this.length;
                        vertices[i * vertexSize + o + 1] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 1] * this.length;
                        vertices[i * vertexSize + o + 2] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 2] * this.length;
                        break;
                    case Usage.Normal:
                        //noinspection PointlessArithmeticExpression
                        vertices[i * vertexSize + o + 0] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 0];
                        vertices[i * vertexSize + o + 1] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 1];
                        vertices[i * vertexSize + o + 2] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 2];
                        break;
                    case Usage.TextureCoordinates:
                        //noinspection PointlessArithmeticExpression
                        vertices[i * vertexSize + o + 0] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 0];
                        vertices[i * vertexSize + o + 1] = CubeCommand.BOX_VERTICES[i * vertexSize + o + 1];
                        break;
                    case Usage.ColorPacked:
                        //noinspection PointlessArithmeticExpression
                        vertices[i * vertexSize + o + 0] = color.r;
                        vertices[i * vertexSize + o + 1] = color.g;
                        vertices[i * vertexSize + o + 2] = color.b;
                        vertices[i * vertexSize + o + 3] = color.a;
                        break;
                    case Usage.ColorUnpacked:
                    default:
                        Logging.ROOT.error("<createScaledVertices> unknown attribute usage: '" + attribute.usage + "' ignoring");
                }
                o += attribute.numComponents;
            }
        }
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);
        final int vertexSize = vertexSize();
        final Mesh mesh = new Mesh(true, CubeCommand.BOX_VERTICES.length / vertexSize, CubeCommand.BOX_INDICES.length, attributes.toArray());

        mesh.setVertices(this.vertices);
        mesh.setIndices(CubeCommand.BOX_INDICES);

        final Model model = new Model();
        final Material material = new Material();

        // TODO: use callback
        if (this.textureFile != null) {
            resourceManager.finishLoadingAsset(this.textureFile);
            final Texture texture = resourceManager.get(this.textureFile);
            texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            model.manageDisposable(texture);

            material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
            material.set(new TextureAttribute(TextureAttribute.Ambient, texture));
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
        }
        if (this.color != null) {
            material.set(new ColorAttribute(TextureAttribute.Diffuse, color));
        }

        final MeshPart meshPart = new MeshPart(Long.toString(getUid(), Character.MAX_RADIX), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);

        final NodePart nodePart = new NodePart(meshPart, material);

        final Node node = new Node();
        node.parts.add(nodePart);
        node.calculateTransforms(true);

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

    private int vertexSize() {
        int result = 0;
        for (final VertexAttribute attribute : attributes) {
            result += attribute.numComponents;
        }
        return result;
    }

    @EntityProperty(name = "length", type = "Float")
    public CubeCommand withLength(float length) {
        this.length = length;
        return this;
    }

    @EntityProperty(name = "texture", type = "String")
    public CubeCommand withTextureFile(String textureFile) {
        this.textureFile = textureFile;
        attributes.add(new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"
        return this;
    }

    @EntityProperty(name = "color", type = "Color")
    public CubeCommand withColor(Color color) {
        this.color = new Color().set(color);
        attributes.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        return this;
    }

    @Override
    public String toString() {
        return CubeCommand.class.getSimpleName();
    }

    private static final float BOX_VERTICES[] = {
            // Front face
            -1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, +0.0f, +1.0f,
            +1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +1.0f, +1.0f, +0.0f,
            -1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +1.0f, +0.0f, +0.0f,
            // Right face
            +1.0f, -1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +0.0f, +1.0f,
            +1.0f, -1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, +0.0f,
            +1.0f, +1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +0.0f, +0.0f,
            // Back face
            +1.0f, -1.0f, -1.0f, +0.0f, +0.0f, -1.0f, +0.0f, +1.0f,
            -1.0f, -1.0f, -1.0f, +0.0f, +0.0f, -1.0f, +1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f, +0.0f, +0.0f, -1.0f, +1.0f, +0.0f,
            +1.0f, +1.0f, -1.0f, +0.0f, +0.0f, -1.0f, +0.0f, +0.0f,
            // Left face
            -1.0f, -1.0f, -1.0f, -1.0f, +0.0f, +0.0f, +0.0f, +1.0f,
            -1.0f, -1.0f, +1.0f, -1.0f, +0.0f, +0.0f, +1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f, -1.0f, +0.0f, +0.0f, +1.0f, +0.0f,
            -1.0f, +1.0f, -1.0f, -1.0f, +0.0f, +0.0f, +0.0f, +0.0f,
            // Top Face
            -1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, +0.0f, +1.0f,
            +1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, +1.0f, +0.0f,
            -1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, +0.0f, +0.0f,
            // Bottom Face
            +1.0f, -1.0f, +1.0f, +0.0f, -1.0f, +0.0f, +0.0f, +1.0f,
            -1.0f, -1.0f, +1.0f, +0.0f, -1.0f, +0.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, -1.0f, +0.0f, -1.0f, +0.0f, +1.0f, +0.0f,
            +1.0f, -1.0f, -1.0f, +0.0f, -1.0f, +0.0f, +0.0f, +0.0f,
    };

    // positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ
    private static final short BOX_INDICES[] = {
            // Font face
            0, 1, 2, 2, 3, 0,
            // Right face
            4, 5, 6, 6, 7, 4,
            // Back face
            9, 10, 11, 11, 8, 9,
            // Left face
            13, 14, 15, 15, 12, 13,
            // Top Face
            17, 18, 19, 19, 16, 17,
            // Bottom Face
            21, 22, 23, 23, 20, 21, };

}
