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
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import net.wohlfart.pluto.entity.EntityPool;
import net.wohlfart.pluto.entity.IEntityCommand;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRenderables;

public class LaserBeamCommand implements IEntityCommand {

    protected static final String TEXTURE_BEAM_PATH = "texture/laser.png";

    private long uid = IEntityCommand.NULL_UID;

    private Entity main;

    private Entity beginEntity;

    private Entity endEntity;

    @Override
    public long getUid() {
        assert this.uid != IEntityCommand.NULL_UID : "uid is invalid";
        return uid;
    }

    public LaserBeamCommand withUid(long uid) {
        assert this.uid == IEntityCommand.NULL_UID : "uid can't be changed";
        this.uid = uid;
        return this;
    }

    public LaserBeamCommand withBegin(Entity position) {
        beginEntity = position;
        return this;
    }

    public LaserBeamCommand withEnd(Entity position) {
        endEntity = position;
        return this;
    }

    @Override
    public void runNow(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        // do nothing
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        // do nothing
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        this.main = entityPool.createEntity();

        final Beam element = new Beam(beginEntity, endEntity);
        main.add(entityPool.createComponent(HasRenderables.class).withDelegate(element));

        entityPool.addEntity(main);
        futureEntity.set(main);
    }

    private static final float W = 1f;

    private static final float[] QUAD_VERTICES = {
            +LaserBeamCommand.W, +LaserBeamCommand.W, 0, /* 0f, 0f, 1f,*/ 1, 1, // Color.toFloatBits(255, 0, 0, 255),
            -LaserBeamCommand.W, +LaserBeamCommand.W, 0, /* 0f, 0f, 1f,*/ 1, 0, // Color.toFloatBits(255, 0, 255, 255),
            -LaserBeamCommand.W, -LaserBeamCommand.W, 0, /* 0f, 0f, 1f,*/ 0, 0, // Color.toFloatBits(0, 255, 0, 255),
            +LaserBeamCommand.W, -LaserBeamCommand.W, 0, /* 0f, 0f, 1f,*/ 0, 1, // Color.toFloatBits(0, 0, 255, 255),
    };

    private static final short[] QUAD_INDICES = { 0, 1, 2, 3, 0, 2, };

    // TODO: removing this entity from the graph also needs to remove the contained entities
    public static class Beam implements RenderableProvider {

        private final Entity begin;
        private final Entity end;
        private final ModelInstance beam;

        private final Vector3 tmpVector1 = new Vector3();
        private final Vector3 tmpVector2 = new Vector3();

        public Beam(Entity beginEntity, Entity endEntity) {
            this.begin = beginEntity;
            this.end = endEntity;
            this.beam = beam();
        }

        private ModelInstance beam() {
            final Mesh mesh = new Mesh(false,
                    4, // vertices count
                    6, // index count
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                    //  new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));
            mesh.setVertices(LaserBeamCommand.QUAD_VERTICES);
            mesh.setIndices(LaserBeamCommand.QUAD_INDICES);

            final MeshPart meshPart = new MeshPart("beam", mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);
            final Material material = createMaterial();

            final NodePart nodePart = new NodePart(meshPart, material);

            final Node node = new Node();
            node.parts.clear();
            node.parts.add(nodePart);
            node.calculateTransforms(true);

            final Model model = new Model();
            model.nodes.clear();
            model.nodes.add(node);
            //model.manageDisposable(texture);

            return new ModelInstance(model);
        }

        private Material createMaterial() {
            final Texture texture = new Texture(Gdx.files.internal(LaserBeamCommand.TEXTURE_BEAM_PATH));
            texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            final Material material = new Material();
            material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
            material.set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA/*, 1f*/));
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, false)); // check depth buffer but don't write
            return material;
        }

        @Override
        public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
            fixBeam();
            beam.getRenderables(renderables, pool);
        }

        // TODO: reduce gc here
        // see: http://stackoverflow.com/questions/32802811/rendering-a-laser-beam-how-to-make-it-face-camera
        private void fixBeam() {
            final Vector3 beginVector = begin.getComponent(HasPosition.class).getPosition().get(new Vector3());
            final Vector3 endVector = end.getComponent(HasPosition.class).getPosition().get(new Vector3());
            // the axis direction
            final Vector3 yAxis = new Vector3().set(beginVector).sub(endVector);
            // beam length
            final float len = yAxis.len();
            // center of the beam
            final Vector3 center = new Vector3().set(beginVector).lerp(endVector, 0.5f);

            // the up vector is orthogonal to the axis and to the center vector
            final Vector3 xAxis = new Vector3().set(center.scl(-1)).crs(yAxis).nor();
            final Vector3 zAxis = new Vector3().set(yAxis).nor().crs(xAxis).nor();

            yAxis.nor();
            xAxis.scl(-1);

            beam.transform.set(xAxis, yAxis, zAxis, tmpVector1.set(0, 0, 0)).tra();

            begin.getComponent(HasPosition.class).getPosition().get(tmpVector1);
            end.getComponent(HasPosition.class).getPosition().get(tmpVector2);
            tmpVector1.lerp(tmpVector2, 0.5f); // center location of the beam
            tmpVector1.traMul(beam.transform);
            beam.transform.translate(tmpVector1); //
            beam.transform.scale(1, len / 2f, 1);
        }
    }

}
