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
import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.UpdateMethod;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.scene.properties.HasUpdateMethod;

public class ShockWaveBlueprint extends AbstractEntityCommand<ShockWaveBlueprint> {

    static final AtomicInteger COUNT = new AtomicInteger();

    protected static final String TEXTURE_PATH = "texture/shockwave.png";

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);
        final ShockWave element = new ShockWave();

        entity.add(entityPool.createComponent(HasRenderables.class).withDelegate(element));

        entity.add(entityPool.createComponent(HasTransformMethod.class).withSetterTransformMethod(element.transform));

        entity.add(entityPool.createComponent(HasUpdateMethod.class).withUpdateMethod(element));

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    public static class ShockWave extends ModelInstance implements UpdateMethod {

        private float localTime = Float.NaN;

        private static final float DEATH_TIME = 4;

        public ShockWave() {
            super(new Model());
            final MeshPart meshPart = createMeshPart();
            final Material material = createMaterial();
            materials.add(material);
            nodes.clear();
            //noinspection AssignmentToSuperclassField
            userData = "count: " + ShockWaveBlueprint.COUNT.getAndIncrement();
            addWave(meshPart, material);
        }

        @Override
        public void update(float delta) {
            final Node quad = nodes.get(0);
            final Material material = materials.get(0);
            final BlendingAttribute blending = (BlendingAttribute) material.get(BlendingAttribute.Type);

            final float currentScale = localTime;
            blending.opacity = 1f - (localTime / ShockWave.DEATH_TIME);
            quad.scale.set(currentScale, currentScale, 1);

            calculateTransforms();
            localTime += delta;
            if (localTime >= ShockWave.DEATH_TIME) {
                localTime = ShockWave.DEATH_TIME;
            }
        }

        private void addWave(MeshPart meshPart, Material material) {
            final NodePart nodePart = new NodePart(meshPart, material);
            final Node node = new Node();
            node.parts.clear();
            node.parts.add(nodePart);
            node.translation.set(0, 0, 0);
            //node.rotation.set(Vector3.X, -75f);
            node.scale.set(1, 1, 1);
            //nodes.clear();
            nodes.add(node);
            calculateTransforms();
            localTime = 0;
        }

        private MeshPart createMeshPart() {
            final Mesh mesh = new Mesh(false, 4, // vertices count
                    6, // index count
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                    new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));

            mesh.setVertices(ShockWave.QUAD_VERTICES);
            mesh.setIndices(ShockWave.QUAD_INDICES);

            final MeshPart meshPart = new MeshPart();
            meshPart.id = "0";
            meshPart.mesh = mesh;
            meshPart.offset = 0;
            meshPart.size = mesh.getNumIndices();
            meshPart.primitiveType = GL20.GL_TRIANGLES;
            return meshPart;
        }

        private Material createMaterial() {
            final Texture texture = new Texture(Gdx.files.internal(ShockWaveBlueprint.TEXTURE_PATH));
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            final Material material = new Material();
            material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
            material.set(new ColorAttribute(ColorAttribute.Diffuse, Color.BLUE));
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA/*, 1f*/));
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, false)); // check depth buffer but don't write
            return material;
        }

        private static final float W = 5f;

        private static final float[] QUAD_VERTICES = { +ShockWave.W, +ShockWave.W, 0, 0f, 0f, 1f, 0, 1, // Color.toFloatBits(255, 0, 0, 255),
                -ShockWave.W, +ShockWave.W, 0, 0f, 0f, 1f, 1, 1, // Color.toFloatBits(255, 0, 255, 255),
                -ShockWave.W, -ShockWave.W, 0, 0f, 0f, 1f, 1, 0, // Color.toFloatBits(0, 255, 0, 255),
                +ShockWave.W, -ShockWave.W, 0, 0f, 0f, 1f, 0, 0, // Color.toFloatBits(0, 0, 255, 255),
        };

        private static final short[] QUAD_INDICES = { 0, 1, 2, 3, 0, 2, };

    }

}
