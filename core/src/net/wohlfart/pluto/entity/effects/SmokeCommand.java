package net.wohlfart.pluto.entity.effects;

import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.UpdateMethod;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.scene.properties.HasUpdateMethod;

@EntityElement(type = "Smoke")
public class SmokeCommand extends AbstractEntityCommand<SmokeCommand> {

    static final AtomicInteger COUNT = new AtomicInteger();

    protected static final String TEXTURE_PATH = "texture/flame.png";
    private static final int GRID_X_ELEMENTS = 2;
    private static final int GRID_Y_ELEMENTS = 2;

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);
        final Flame element = new Flame();

        entity.add(entityPool.createComponent(HasRenderables.class)
                .withDelegate(element));

        entity.add(entityPool.createComponent(HasTransformMethod.class)
                .withSetterTransformMethod(element.transform));
        //        .withRotationTransformMethod(element.transform));

        entity.add(entityPool.createComponent(HasUpdateMethod.class)
                .withUpdateMethod(element));

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    public static class Flame extends ModelInstance implements UpdateMethod {

        private float localTime = Float.NaN;

        private static final float DEATH_TIME = 4000;

        public Flame() {
            super(new Model());
            final MeshPart[] meshParts = createMeshParts(SmokeCommand.GRID_X_ELEMENTS, SmokeCommand.GRID_Y_ELEMENTS);
            final Material material = createMaterial();
            materials.add(material);
            nodes.clear();
            //noinspection AssignmentToSuperclassField
            userData = "count: " + SmokeCommand.COUNT.getAndIncrement();
            createFlame(meshParts, material);
        }

        @Override
        public void update(float delta) {
            final int partIndex = ((int) (localTime * 1f)) % (SmokeCommand.GRID_X_ELEMENTS * SmokeCommand.GRID_Y_ELEMENTS);

            final Array<NodePart> parts = nodes.get(0).parts;
            for (int i = 0; i < parts.size; i++) {
                final NodePart part = parts.get(i);
                part.enabled = (i == partIndex);
            }
            calculateTransforms();
            localTime += delta;
            if (localTime >= Flame.DEATH_TIME) {
                localTime = Flame.DEATH_TIME;
            }
        }

        private void createFlame(MeshPart[] meshParts, Material material) {
            final Node node = new Node();
            node.parts.clear();
            for (final MeshPart meshPart : meshParts) {
                final NodePart nodePart = new NodePart(meshPart, material);
                nodePart.enabled = false;
                node.parts.add(nodePart);
            }
            node.translation.set(0, 0, 0);
            //node.rotation.set(Vector3.X, -75f);
            node.scale.set(1, 1, 1);
            //nodes.clear();
            nodes.add(node);
            calculateTransforms();
            localTime = 0f;
        }

        private MeshPart[] createMeshParts(int a, int b) {
            final int partCount = a * b;
            final MeshPart[] result = new MeshPart[partCount];

            final Mesh mesh = new Mesh(false, 4 * partCount, // vertices count
                    6 * partCount, // index count
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                    new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));
            model.meshes.add(mesh);

            final float[] vertices = new float[partCount * Flame.QUAD_VERTICES.length];
            final short[] indices = new short[partCount * Flame.QUAD_INDICES.length];

            int partIndex = 0;
            for (int x = 0; x < a; x++) {
                for (int y = 0; y < b; y++) {
                    final int vertOffset = partIndex * Flame.QUAD_VERTICES.length;
                    final int idxOffset = partIndex * Flame.QUAD_INDICES.length;
                    System.arraycopy(Flame.QUAD_VERTICES, 0, vertices, vertOffset, Flame.QUAD_VERTICES.length);
                    System.arraycopy(Flame.QUAD_INDICES, 0, indices, idxOffset, Flame.QUAD_INDICES.length);
                    // adjust the texture positions
                    vertices[vertOffset + 6] = (1f / a) * x; // 0
                    vertices[vertOffset + 7] = (1f / b) + (1f / b) * y; // 1
                    vertices[vertOffset + 14] = (1f / a) + (1f / a) * x; // 1
                    vertices[vertOffset + 15] = (1f / b) + (1f / b) * y; // 1
                    vertices[vertOffset + 22] = (1f / a) + (1f / a) * x; // 1
                    vertices[vertOffset + 23] = (1f / b) * y; // 0
                    vertices[vertOffset + 30] = (1f / a) * x; // 0
                    vertices[vertOffset + 31] = (1f / b) * y; // 0
                    for (int z = idxOffset; z < idxOffset + Flame.QUAD_INDICES.length; z++) {
                        indices[z] += (partIndex * 4);
                    }
                    final MeshPart meshPart = new MeshPart();
                    meshPart.id = Integer.toString(partIndex);
                    meshPart.mesh = mesh;
                    meshPart.offset = idxOffset; // might be byte?
                    meshPart.size = 6; // index count, total vertices to be drawn in this part
                    meshPart.primitiveType = GL20.GL_TRIANGLES;
                    result[partIndex] = meshPart;
                    partIndex++;
                }
            }
            mesh.setVertices(vertices);
            mesh.setIndices(indices);
            return result;
        }

        private Material createMaterial() {
            final Texture texture = new Texture(Gdx.files.internal(SmokeCommand.TEXTURE_PATH));
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            final Material material = new Material();
            material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
            material.set(new ColorAttribute(ColorAttribute.Diffuse, Color.BLUE));
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA/*, 1f*/));
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, false)); // check depth buffer but don't write
            return material;
        }

        private static final float W = 15f;
        private static final float[] QUAD_VERTICES = {
                +Flame.W, +Flame.W, 0, 0f, 0f, 1f, 0, 1, // Color.toFloatBits(255, 0, 0, 255),
                -Flame.W, +Flame.W, 0, 0f, 0f, 1f, 1, 1, // Color.toFloatBits(255, 0, 255, 255),
                -Flame.W, -Flame.W, 0, 0f, 0f, 1f, 1, 0, // Color.toFloatBits(0, 255, 0, 255),
                +Flame.W, -Flame.W, 0, 0f, 0f, 1f, 0, 0, // Color.toFloatBits(0, 0, 255, 255),
        };

        private static final short[] QUAD_INDICES = { 0, 1, 2, 3, 0, 2, };

    }

}
