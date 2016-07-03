package net.wohlfart.pluto.shader;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.TextureData;
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

import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.lang.EntityElement;
import net.wohlfart.pluto.scene.lang.EntityProperty;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;
import net.wohlfart.pluto.util.Utils;

@EntityElement(type = "Skybox")
public class SkyboxCommand implements IEntityCommand {

    public static final String BLUE = "blue";

    public static final String SKY = "sky";

    public static final String VIOLENT = "violent";

    public static final String TEST = "test";

    protected String style;

    private long uid;

    @Override
    public long getUid() {
        //assert this.uid != IEntityCommand.NULL_UID : "uid is invalid";
        return uid;
    }

    @EntityProperty(name = "uid", type = "Long")
    public SkyboxCommand withUid(long uid) {
        this.uid = uid;
        return this;
    }

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert Utils.isRenderThread();
        assert style != null : "style is null";
        assert uid != 0;
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        assert !Utils.isRenderThread();
    }

    @Override
    public void runSync(ResourceManager resourceManager, final EntityPool entityPool, FutureEntity futureEntity) {
        assert Utils.isRenderThread();
        resourceManager.load(
                this.style,
                TextureData[].class,
                new CubeTexturesLoader.Parameters(ResourceManager.CUBEMAP_PATH + this.style,
                        (assetManager, fileName, type) -> {
                            // running on the render thread
                            futureEntity.set(
                                    registerInstance(entityPool, assetManager.get(this.style, TextureData[].class)));
                        }));
    }

    private Entity registerInstance(EntityPool entityPool, TextureData[] cubemap) {
        final Entity entity = entityPool.createEntity();
        final Model model = new Model();

        model.nodes.clear();
        final Node node = new Node();
        model.nodes.add(node);
        node.id = "skybox-node";
        node.parts.add(createNodePart(getUid(), new Cubemap(cubemap[0], cubemap[1], cubemap[2], cubemap[3], cubemap[4], cubemap[5])));

        final ModelInstance element = new ModelInstance(model);

        entity.add(entityPool.createComponent(HasRenderables.class).withDelegate(element));
        entity.add(entityPool.createComponent(HasTransformMethod.class).withRotationTransformMethod(element.transform));
        node.calculateTransforms(true);
        entityPool.addEntity(entity);
        return entity;
    }

    private NodePart createNodePart(long id, Cubemap cubemap) {
        final float[] vertices = new float[SkyboxCommand.CUBE_VERTICES.length];
        System.arraycopy(SkyboxCommand.CUBE_VERTICES, 0, vertices, 0, SkyboxCommand.CUBE_VERTICES.length);

        final Mesh mesh = new Mesh(true, // static
                vertices.length / 3, // max vertex count
                SkyboxCommand.CUBE_INDICES.length, // max index count
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE)); //"a_position"
        mesh.setVertices(vertices);
        mesh.setIndices(SkyboxCommand.CUBE_INDICES);

        final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        material.set(new SkyboxAttribute(cubemap));

        final MeshPart meshPart = new MeshPart(Long.toString(id, Character.MAX_RADIX), mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);

        return new NodePart(meshPart, material);
    }

    @EntityProperty(name = "style", type = "String", values = BLUE + "," + SKY + "," + VIOLENT + "," + TEST)
    public SkyboxCommand withStyle(String style) {
        this.style = style;
        return this;
    }

    @Override
    public String toString() {
        return "SkyboxCommand ["
                + "style=" + style
                + ", uid=" + uid
                + "]";
    }

    private static final float L = 1f;
    private static final float CUBE_VERTICES[] = {
            -SkyboxCommand.L, +SkyboxCommand.L, +SkyboxCommand.L, // 0
            -SkyboxCommand.L, -SkyboxCommand.L, +SkyboxCommand.L, // 1
            +SkyboxCommand.L, -SkyboxCommand.L, +SkyboxCommand.L, // 2
            +SkyboxCommand.L, +SkyboxCommand.L, +SkyboxCommand.L, // 3

            -SkyboxCommand.L, +SkyboxCommand.L, -SkyboxCommand.L, // 4
            -SkyboxCommand.L, -SkyboxCommand.L, -SkyboxCommand.L, // 5
            +SkyboxCommand.L, -SkyboxCommand.L, -SkyboxCommand.L, // 6
            +SkyboxCommand.L, +SkyboxCommand.L, -SkyboxCommand.L, // 7
    };

    // positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ
    private static final short CUBE_INDICES[] = {
            // positiveX - left
            3, 2, 6, 6, 7, 3,
            // negativeX - right
            4, 5, 1, 1, 0, 4,
            // positiveY - top
            4, 0, 3, 3, 7, 4,
            // negativeY - bottom
            2, 6, 5, 5, 1, 2,
            // positiveZ - back
            0, 1, 2, 2, 3, 0,
            // negativeZ - front
            7, 6, 5, 5, 4, 7, };

}
