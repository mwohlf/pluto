package net.wohlfart.pluto.entity.effects;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import net.wohlfart.pluto.entity.AbstractEntityCommand;
import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;

public class SmokeTrailBlueprint extends AbstractEntityCommand<SmokeTrailBlueprint> {

    private static final String TEXTURE_PATH = "texture/smoketrail.png";

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);
        final SmokeClouds element = new SmokeClouds();

        element.addCloud(0, 0, 0);
        element.addCloud(0, +2, 0);
        element.addCloud(0, +4, 0);
        element.addCloud(0, +6, 0);
        element.addCloud(0, +8, 0);
        element.addCloud(0, +10, 0);
        element.addCloud(0, +12, 0);

        entity.add(entityPool.createComponent(HasRenderables.class).withDelegate(element));

        entity.add(entityPool.createComponent(HasTransformMethod.class).withSetterTransformMethod(element.transform));

        entityPool.addEntity(entity);
        futureEntity.set(entity);
    }

    public static class SmokeClouds extends ModelInstance {

        private final MeshPart meshPart;

        private final Material material;

        public SmokeClouds() {
            super(new Model());
            meshPart = createMeshPart();
            material = createMaterial();
            nodes.clear();
        }

        public void addCloud(int x, int y, int z) {
            final NodePart nodePart = new NodePart(meshPart, material);
            final Node node = new Node();
            node.parts.clear();
            node.parts.add(nodePart);
            node.translation.set(x, y, z);
            node.rotation.idt();
            node.scale.set(1, 1, 1);
            //nodes.clear();
            nodes.add(node);
            calculateTransforms();
        }

        private MeshPart createMeshPart() {
            final Mesh mesh = new Mesh(false, 4, // vertices count
                    6, // index count
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                    new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));
            mesh.setVertices(SmokeClouds.QUAD_VERTICES);
            mesh.setIndices(SmokeClouds.QUAD_INDICES);

            final MeshPart result = new MeshPart();
            result.mesh = mesh;
            result.offset = 0;
            result.size = mesh.getNumIndices();
            result.primitiveType = GL20.GL_TRIANGLES;
            return result;
        }

        private Material createMaterial() {
            final Texture texture = new Texture(Gdx.files.internal(SmokeTrailBlueprint.TEXTURE_PATH));
            texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            final Material newMaterial = new Material(ColorAttribute.createDiffuse(Color.WHITE));
            newMaterial.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
            return newMaterial;
        }

        private static final float W = 1f;

        private static final float[] QUAD_VERTICES = {
                +SmokeClouds.W, +SmokeClouds.W, 0, 0f, 0f, 1f, 0, 1, // Color.toFloatBits(255, 0, 0, 255),
                -SmokeClouds.W, +SmokeClouds.W, 0, 0f, 0f, 1f, 1, 1, // Color.toFloatBits(255, 0, 255, 255),
                -SmokeClouds.W, -SmokeClouds.W, 0, 0f, 0f, 1f, 1, 0, // Color.toFloatBits(0, 255, 0, 255),
                +SmokeClouds.W, -SmokeClouds.W, 0, 0f, 0f, 1f, 0, 0, // Color.toFloatBits(0, 0, 255, 255),
        };

        private static final short[] QUAD_INDICES = { 0, 1, 2, 3, 0, 2, };

    }

}
