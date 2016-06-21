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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
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
import net.wohlfart.pluto.entity.MovementSystem;
import net.wohlfart.pluto.resource.ResourceManager;
import net.wohlfart.pluto.scene.FutureEntity;
import net.wohlfart.pluto.scene.Position;
import net.wohlfart.pluto.scene.properties.HasPosition;
import net.wohlfart.pluto.scene.properties.HasRenderables;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.scene.properties.HasTransformMethod;

public class LinkDecalCommand implements IEntityCommand {

    protected static final String TEXTURE_PATH = "texture/flame.png";
    protected static final String TEXTURE_BEAM_PATH = "texture/laser.jpg";

    private long uid = IEntityCommand.NULL_UID;

    private Entity main;

    private final Position beginPosition = new Position();

    private final Position endPosition = new Position();

    @Override
    public long getUid() {
        assert this.uid != IEntityCommand.NULL_UID : "uid is invalid";
        return uid;
    }

    public LinkDecalCommand withUid(long uid) {
        assert this.uid == IEntityCommand.NULL_UID : "uid can't be changed";
        this.uid = uid;
        return this;
    }

    public LinkDecalCommand withBegin(float x, float y, float z) {
        beginPosition.set(x, y, z);
        return this;
    }

    public LinkDecalCommand withBegin(Vector3 begin) {
        return withBegin(begin.x, begin.y, begin.z);
    }

    public LinkDecalCommand withEnd(float x, float y, float z) {
        endPosition.set(x, y, z);
        return this;
    }

    public LinkDecalCommand withEnd(Vector3 end) {
        return withEnd(end.x, end.y, end.z);
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

        final Entity begin = entityPool.createEntity();
        final ModelInstance beginInstance = quad();
        begin.add(entityPool.createComponent(HasTransformMethod.class).withTranslation(beginInstance.transform));
        begin.add(entityPool.createComponent(HasRotation.class).withRotation(MovementSystem.NULL_ROTATION));
        begin.add(entityPool.createComponent(HasPosition.class).withPosition(this.beginPosition));

        final Entity end = entityPool.createEntity();
        final ModelInstance endInstance = quad();
        end.add(entityPool.createComponent(HasTransformMethod.class).withTranslation(endInstance.transform));
        end.add(entityPool.createComponent(HasRotation.class).withRotation(MovementSystem.NULL_ROTATION));
        end.add(entityPool.createComponent(HasPosition.class).withPosition(this.endPosition));

        final Beam element = new Beam(beginInstance, endInstance);
        main.add(entityPool.createComponent(HasRenderables.class).withDelegate(element));

        entityPool.addEntity(begin);
        entityPool.addEntity(end);
        entityPool.addEntity(main);
        futureEntity.set(main);
    }

    private ModelInstance quad() {
        final Mesh mesh = new Mesh(false, 4, // vertices count
                6, // index count
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));
        mesh.setVertices(LinkDecalCommand.QUAD_VERTICES);
        mesh.setIndices(LinkDecalCommand.QUAD_INDICES);

        final MeshPart meshPart = new MeshPart("beam", mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);
        final Texture texture = new Texture(Gdx.files.internal(LinkDecalCommand.TEXTURE_PATH));
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));

        final NodePart nodePart = new NodePart(meshPart, material);

        final Node node = new Node();
        node.parts.clear();
        node.parts.add(nodePart);
        node.calculateTransforms(true);

        final Model model = new Model();
        model.nodes.clear();
        model.nodes.add(node);
        model.manageDisposable(texture);

        return new ModelInstance(model);
    }

    private static final float W = 1f;

    private static final float[] QUAD_VERTICES = {
            +LinkDecalCommand.W, +LinkDecalCommand.W, 0, 0f, 0f, 1f, 1, 1, // Color.toFloatBits(255, 0, 0, 255),
            -LinkDecalCommand.W, +LinkDecalCommand.W, 0, 0f, 0f, 1f, 1, 0, // Color.toFloatBits(255, 0, 255, 255),
            -LinkDecalCommand.W, -LinkDecalCommand.W, 0, 0f, 0f, 1f, 0, 0, // Color.toFloatBits(0, 255, 0, 255),
            +LinkDecalCommand.W, -LinkDecalCommand.W, 0, 0f, 0f, 1f, 0, 1, // Color.toFloatBits(0, 0, 255, 255),
    };

    private static final short[] QUAD_INDICES = { 0, 1, 2, 3, 0, 2, };

    // TODO: removing this entity from the graph also needs to remove the contained entities
    public static class Beam implements RenderableProvider {

        private final ModelInstance begin;
        private final ModelInstance end;
        private final ModelInstance beam;

        private final Vector3 tmpVector1 = new Vector3();
        private final Vector3 tmpVector2 = new Vector3();

        public Beam(ModelInstance begin, ModelInstance end) {
            this.begin = begin;
            this.end = end;
            this.beam = beam();
        }

        private ModelInstance beam() {
            final Mesh mesh = new Mesh(false, 4, // vertices count
                    6, // index count
                    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE), //"a_position"),
                    new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE), //"a_normal"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")); //"a_texCoord0"));
            mesh.setVertices(LinkDecalCommand.QUAD_VERTICES);
            mesh.setIndices(LinkDecalCommand.QUAD_INDICES);

            final MeshPart meshPart = new MeshPart("beam", mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES);
            final Texture texture = new Texture(Gdx.files.internal(LinkDecalCommand.TEXTURE_BEAM_PATH));
            texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
            material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));

            final NodePart nodePart = new NodePart(meshPart, material);

            final Node node = new Node();
            node.parts.clear();
            node.parts.add(nodePart);
            node.calculateTransforms(true);

            final Model model = new Model();
            model.nodes.clear();
            model.nodes.add(node);
            model.manageDisposable(texture);

            return new ModelInstance(model);
        }

        @Override
        public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
            fixBeam();
            begin.getRenderables(renderables, pool);
            end.getRenderables(renderables, pool);
            beam.getRenderables(renderables, pool);
        }

        // TODO: reduce gc here
        // see: http://stackoverflow.com/questions/32802811/rendering-a-laser-beam-how-to-make-it-face-camera
        private void fixBeam() {
            final Vector3 beginVector = begin.transform.getTranslation(new Vector3());
            final Vector3 endVector = end.transform.getTranslation(new Vector3());
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

            begin.transform.getTranslation(tmpVector1);
            end.transform.getTranslation(tmpVector2);
            tmpVector1.lerp(tmpVector2, 0.5f); // center location of the beam
            tmpVector1.traMul(beam.transform);
            beam.transform.translate(tmpVector1); //
            beam.transform.scale(1, len / 2f, 1);
        }
    }

}
