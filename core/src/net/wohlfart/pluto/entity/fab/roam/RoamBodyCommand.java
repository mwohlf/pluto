package net.wohlfart.pluto.entity.fab.roam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "TODO: work in progress")
@EntityElement(type = "RoamBody")
public class RoamBodyCommand extends AbstractEntityCommand<RoamBodyCommand> {

    public static final String CUBEMAP_PATH = "texture/cubemap/";

    private final Vector3 tmpVector = new Vector3();

    private final Pool<RoamTriangle> nodePool = new RoamTrianglePool();
    // used in the body
    private List<RoamTriangle> nodeList = new ArrayList<>();

    private FloatArray verticesBuffer = new FloatArray();
    private ShortArray indicesBuffer = new ShortArray();
    private float radius = Float.NaN;
    private IHeightFunction heightFunction = new IHeightFunction.Const(1);
    private int details = 2;

    // exactly one allowed (xor):
    private Color color;
    private String textureFile;
    private String cubemapFile;
    private IColorFunction colorFunction;

    private Pixmap[] pixmapData;

    private final Array<VertexAttribute> attributes = new Array<>(new VertexAttribute[] {
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE) });

    private static class RoamTrianglePool extends Pool<RoamTriangle> {
        @Override
        protected RoamTriangle newObject() {
            return new RoamTriangle();
        }
    }

    @Override
    public void runAsync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {

        if (colorFunction != null) {
            pixmapData = loadPixmanpData(colorFunction);
        } else if (cubemapFile != null) {
            pixmapData = loadPixmapData(cubemapFile);
        }

        // setup the initial body
        final RoamBodyBootstrap bootstrap = new RoamBodyBootstrap();
        this.nodeList = new ArrayList<>(bootstrap.create(this.nodePool));
        for (final RoamTriangle triangle : this.nodeList) {
            for (int i = 0; i < 3; i++) {
                triangle.vertices[i].nor();
            }
        }

        // refine the triangles
        for (int i = 0; i < this.details; i++) {
            splitCurrentTriangleList(this.nodeList);
            bootstrap.validate(this.nodeList);
        }

    }

    private Pixmap[] loadPixmapData(String subdir) {
        final String[] sides = Utils.getPaths(RoamBodyCommand.CUBEMAP_PATH + subdir);
        return new Pixmap[] {
                new Pixmap(Gdx.files.internal(sides[0])),
                new Pixmap(Gdx.files.internal(sides[1])),
                new Pixmap(Gdx.files.internal(sides[2])),
                new Pixmap(Gdx.files.internal(sides[3])),
                new Pixmap(Gdx.files.internal(sides[4])),
                new Pixmap(Gdx.files.internal(sides[5])),
        };
    }

    @SuppressWarnings("MagicNumber")
    private Pixmap[] loadPixmanpData(IColorFunction function) {
        return new Pixmap[] {
                // yaw: the rotation around the y axis in degrees
                // pitch: the rotation around the x axis in degrees
                // roll: the rotation around the z axis degrees
                new PixmapData(new Quaternion().setEulerAngles(90, 0, 180), function),
                new PixmapData(new Quaternion().setEulerAngles(270, 0, 180), function),
                new PixmapData(new Quaternion().setEulerAngles(0, 270, 180), function),
                new PixmapData(new Quaternion().setEulerAngles(0, 90, 180), function),
                new PixmapData(new Quaternion().setEulerAngles(0, 0, 180), function),
                new PixmapData(new Quaternion().setEulerAngles(180, 0, 180), function),
        };
    }

    public static class PixmapData extends Pixmap {
        private static final int SIZE = 512;

        public PixmapData(Quaternion quat, IColorFunction colorFunction) {
            super(PixmapData.SIZE, PixmapData.SIZE, Pixmap.Format.RGBA8888);
            final Vector3 vector = new Vector3();
            for (int y = 0; y < PixmapData.SIZE; y++) {
                for (int x = 0; x < PixmapData.SIZE; x++) {
                    vector.set((PixmapData.SIZE - x) - PixmapData.SIZE / 2f, y - PixmapData.SIZE / 2f, PixmapData.SIZE / 2f).mul(quat).nor();
                    drawPixel(x, y, Color.rgba8888(colorFunction.calculate(vector)));
                }
            }
        }
    }

    @Override
    public void runSync(ResourceManager resourceManager, EntityPool entityPool, FutureEntity futureEntity) {
        final Entity entity = create(entityPool);

        final NodePart nodePart = new NodePart(
                createMeshPart(),
                createMaterial(this.textureFile, this.pixmapData));

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
        this.verticesBuffer = new FloatArray();
        this.indicesBuffer = new ShortArray();

        for (final RoamTriangle node : nodeList) {
            appendTriangle(node);
        }

        int vertSize = 0;
        for (final VertexAttribute attribute : attributes) {
            vertSize += attribute.numComponents;
        }

        final Mesh mesh = new Mesh(true, // static
                this.verticesBuffer.shrink().length / vertSize, // vertices count
                this.indicesBuffer.shrink().length, // index count
                attributes.toArray());

        // Set our vertices up in a Counter Clock Wise order
        mesh.setVertices(this.verticesBuffer.shrink());
        mesh.setIndices(this.indicesBuffer.shrink());

        final MeshPart meshPart = new MeshPart();
        meshPart.mesh = mesh;
        meshPart.offset = 0;
        meshPart.size = mesh.getNumIndices();
        meshPart.primitiveType = GL20.GL_TRIANGLES;
        //return new MeshConverter().toWireFrame(meshPart);
        return meshPart;
    }

    private Material createMaterial(String textureFile, Pixmap[] pixmapData) {
        final Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        if (textureFile != null) {
            final Texture texture = new Texture(Gdx.files.internal(textureFile));
            texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            material.set(new TextureAttribute(TextureAttribute.Diffuse, texture));
        }
        if (pixmapData != null) {
            final Cubemap cubemap = new Cubemap(
                    pixmapData[0],
                    pixmapData[1],
                    pixmapData[2],
                    pixmapData[3],
                    pixmapData[4],
                    pixmapData[5],
                    true);
            material.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
        }
        return material;

    }

    private RoamBodyCommand withAttribute(VertexAttribute vertexAttribute) {
        this.attributes.add(vertexAttribute);
        return this;
    }

    public RoamBodyCommand withNormalAttribute() {
        return withAttribute(new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));
    }

    public RoamBodyCommand withColorAttribute() {
        return withAttribute(new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
    }

    @EntityProperty(name = "radius", type = "Float")
    public RoamBodyCommand withRadius(float radius) {
        this.radius = radius;
        return this;
    }

    @EntityProperty(name = "heightFunction", type = "HeightFunction")
    public RoamBodyCommand withHeightFunction(IHeightFunction heightFunction) {
        this.heightFunction = heightFunction;
        return this;
    }

    @EntityProperty(name = "colorFunction", type = "ColorFunction")
    public RoamBodyCommand withColorFunction(IColorFunction colorFunction) {
        if (this.textureFile != null || this.cubemapFile != null || this.color != null) {
            throw new IllegalStateException("can't use color and texture/cubemap/color in the same entity");
        }
        this.withNormalAttribute();
        this.colorFunction = colorFunction;
        return this;
    }

    @EntityProperty(name = "color", type = "Color")
    public RoamBodyCommand withColor(Color color) {
        if (this.textureFile != null || this.cubemapFile != null || this.colorFunction != null) {
            throw new IllegalStateException("can't use color and texture/cubemap/colorFunction in the same entity");
        }
        this.color = new Color(color);
        return withAttribute(new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
    }

    @EntityProperty(name = "texture", type = "String")
    public RoamBodyCommand withTexture(String textureFile) {
        if (this.color != null || this.cubemapFile != null || this.colorFunction != null) {
            throw new IllegalStateException("can't use texture and color/cubemap/colorFunction in the same entity");
        }
        this.textureFile = textureFile;
        return withAttribute(new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
    }

    @EntityProperty(name = "cubemap", type = "String")
    public RoamBodyCommand withCubemap(String cubemapFile) {
        if (this.color != null || this.textureFile != null || this.colorFunction != null) {
            throw new IllegalStateException("can't use cubemap and color/texture/colorFunction in the same entity");
        }
        this.withNormalAttribute();
        this.cubemapFile = cubemapFile;
        return this;
    }

    @EntityProperty(name = "details", type = "Integer")
    public RoamBodyCommand withDetails(int details) {
        this.details = details;
        return this;
    }

    private void splitCurrentTriangleList(List<RoamTriangle> nodeList) {
        final Collection<RoamTriangle> tmpList = new ArrayList<>(nodeList);
        for (final RoamTriangle triangle : tmpList) {
            splitTriangle(triangle, nodeList);
        }
    }

    private void splitTriangle(RoamTriangle triangle, List<RoamTriangle> nodeList) {
        nodeList.addAll(Arrays.asList(triangle.getOrCreateChildren(this.nodePool)));
        removeEmptyTriangles(nodeList);
    }

    private void removeEmptyTriangles(Iterable<RoamTriangle> nodeList) {
        final Iterator<RoamTriangle> iter = nodeList.iterator();
        while (iter.hasNext()) {
            if (iter.next() == RoamTriangle.NULL_TRIANGLE) {
                iter.remove();
            }
        }
    }

    // draw the shape
    private void appendTriangle(RoamTriangle node) {
        if (!node.hasChildren()) {
            appendTriangle(node.vertices[0], node.vertices[1], node.vertices[2]);
        }
    }

    private void appendTriangle(Vector3 v0, Vector3 v1, Vector3 v2) {
        appendVertex(v0, 1.0f, 1.0f); // 0
        appendVertex(v1, 0.5f, 0.0f); // 1
        appendVertex(v2, 0.0f, 1.0f); // 2
    }

    private void appendVertex(Vector3 vertex, float x, float y) {
        for (final VertexAttribute attribute : attributes) {
            switch (attribute.usage) {
                case Usage.Position:
                    tmpVector.set(vertex); // .nor();
                    final float l = this.heightFunction.calculate(tmpVector);
                    tmpVector.scl(this.radius + l);
                    this.verticesBuffer.add(tmpVector.x);
                    this.verticesBuffer.add(tmpVector.y);
                    this.verticesBuffer.add(tmpVector.z);
                    break;
                case Usage.ColorUnpacked:
                    if (color == null) {
                        color = new Color(Color.BLACK);
                    }
                    this.verticesBuffer.add(color.r);
                    this.verticesBuffer.add(color.g);
                    this.verticesBuffer.add(color.b);
                    this.verticesBuffer.add(color.a);
                    break;
                case Usage.Normal:
                    tmpVector.set(vertex).nor();
                    this.verticesBuffer.add(tmpVector.x);
                    this.verticesBuffer.add(tmpVector.y);
                    this.verticesBuffer.add(tmpVector.z);
                    break;
                case Usage.TextureCoordinates:
                    this.verticesBuffer.add(x);
                    this.verticesBuffer.add(y);
                    break;
                default:
                    Logging.ROOT.error("<appendVertex> unsupported usage " + attribute.usage);
            }
        }
        this.indicesBuffer.add(this.indicesBuffer.size);
    }

}
