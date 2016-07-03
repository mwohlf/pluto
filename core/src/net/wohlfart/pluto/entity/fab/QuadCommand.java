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

import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.lang.EntityProperty;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;

@EntityElement(type = "Quad")
public class QuadCommand extends AbstractEntityCommand<QuadCommand> {

    private String textureFile;

    private float length;

    // calculated in async method, used in sync
    private float[] vertices;

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        resourceManager.load(this.textureFile, Texture.class);
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        vertices = new float[QUAD_VERTICES.length];
        System.arraycopy(QUAD_VERTICES, 0, vertices, 0, QUAD_VERTICES.length);
        for (int i = 0; i < QUAD_VERTICES.length; i += 8) {
            //noinspection PointlessArithmeticExpression
            vertices[i + 0] *= length / 2f;
            vertices[i + 1] *= length / 2f;
            vertices[i + 2] *= length / 2f;
        }
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);

        final Mesh mesh = new Mesh(true, QUAD_VERTICES.length / 8, QUAD_VERTICES.length, // vertices, indices
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));

        mesh.setVertices(vertices);
        mesh.setIndices(QUAD_INDICES);

        // TODO: use callback
        resourceManager.finishLoadingAsset(this.textureFile);
        final Texture texture = resourceManager.get(this.textureFile);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
        material.set(new TextureAttribute(TextureAttribute.Ambient, texture));
        material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));

        final MeshPart meshPart = new MeshPart(Long.toString(getUid(), Character.MAX_RADIX), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);

        final NodePart nodePart = new NodePart(meshPart, material);

        final Node node = new Node();
        node.parts.clear();
        node.parts.add(nodePart);
        node.calculateTransforms(true);

        final Model model = new Model();
        model.nodes.clear();
        model.nodes.add(node);
        model.manageDisposable(texture);

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
    public QuadCommand withLength(float length) {
        this.length = length;
        return this;
    }

    @EntityProperty(name = "texture", type = "String")
    public QuadCommand withTextureFile(String textureFile) {
        this.textureFile = textureFile;
        return this;
    }

    private static final float[] QUAD_VERTICES = {
            -1f, -1f, +0f, 0f, 0f, 1f, 0, 1, // bottom left
            +1f, +1f, +0f, 0f, 0f, 1f, 1, 0, // top right
            -1f, +1f, +0f, 0f, 0f, 1f, 0, 0, // top left
            +1f, -1f, +0f, 0f, 0f, 1f, 1, 1, // bottom right
    };

    private static final short[] QUAD_INDICES = { 0, 1, 2, 0, 3, 1 };

}
