package net.wohlfart.pluto;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.FloatArray;

public class MeshConverterUtil {

    private final FloatArray verticesBuffer = new FloatArray();

    public MeshPart toWireFrame(MeshPart input) {

        final MeshPart meshPart = new MeshPart();

        switch (input.primitiveType) {
            case GL20.GL_TRIANGLES:
                spoolTriangleMesh(input.mesh);
                break;
            default:
                Logging.ROOT.debug("<toWireFrame>" + " unable to convert primitiveType " + input.primitiveType);
        }

        final Mesh mesh = new Mesh(true, verticesBuffer.shrink().length / 3, // vertices count
                0, // no indexes
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        mesh.setVertices(verticesBuffer.shrink());

        meshPart.mesh = mesh;
        meshPart.offset = 0;
        meshPart.size = mesh.getNumIndices();
        meshPart.primitiveType = GL20.GL_LINES;
        return meshPart;
    }

    private void spoolTriangleMesh(Mesh mesh) {
        final int vertexSize = mesh.getVertexSize() / 4;
        final VertexAttribute posAttribute = mesh.getVertexAttribute(Usage.Position);
        final int offset = posAttribute.offset / 4;
        final FloatBuffer meshVertBuffer = mesh.getVerticesBuffer();

        final ShortBuffer indicesBuffer = mesh.getIndicesBuffer().duplicate();
        if (indicesBuffer.capacity() > 0) {
            for (int i = 0; i < indicesBuffer.capacity(); i += 3) {
                addTriangle(pickPosition(indicesBuffer.get() * vertexSize + offset, meshVertBuffer),
                            pickPosition(indicesBuffer.get() * vertexSize + offset, meshVertBuffer),
                            pickPosition(indicesBuffer.get() * vertexSize + offset, meshVertBuffer));
            }
        } else {
            Logging.ROOT.debug("<spoolTriangleMesh> need to implement non-indexed spooling ");
        }
    }

    private float[] pickPosition(int index, FloatBuffer verticesBuffer) {
        //noinspection PointlessArithmeticExpression
        return new float[] { verticesBuffer.get(index + 0), verticesBuffer.get(index + 1), verticesBuffer.get(index + 2), };
    }

    private void addTriangle(float[] a, float b[], float c[]) {
        addLine(a[0], a[1], a[2], b[0], b[1], b[2]);
        addLine(b[0], b[1], b[2], c[0], c[1], c[2]);
        addLine(c[0], c[1], c[2], a[0], a[1], a[2]);
    }

    public void addLine(float ax, float ay, float az, float bx, float by, float bz) {

        verticesBuffer.add(ax);
        verticesBuffer.add(ay);
        verticesBuffer.add(az);

        verticesBuffer.add(bx);
        verticesBuffer.add(by);
        verticesBuffer.add(bz);
    }

}
