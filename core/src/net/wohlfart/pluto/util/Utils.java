package net.wohlfart.pluto.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.wohlfart.pluto.Logging;

public final class Utils {

    private static final Vector3 tmpVector = new Vector3();

    private static float tmpScale;

    private static final Random random = new Random();

    static volatile String renderThreadName;

    private static long tickOffset = Long.MIN_VALUE;

    public static final List<String> CUBE_PARTS = Collections.unmodifiableList(Arrays.asList("/right.png",
            "/left.png",
            "/top.png",
            "/bottom.png",
            "/front.png",
            "/back.png"));

    private Utils() {
    }

    public static String[] getPaths(String basePath) {
        final String[] result = new String[Utils.CUBE_PARTS.size()];
        for (int i = 0; i < Utils.CUBE_PARTS.size(); i++) {
            result[i] = basePath + Utils.CUBE_PARTS.get(i);
        }
        return result;
    }

    /**
     * some code needs to run on the render thread
     */
    public static void initRenderThreadName(String threadName) {
        Utils.renderThreadName = threadName;
    }

    public static void initTickCount() {
        Utils.tickOffset = System.currentTimeMillis();
    }

    public static long currentTickCount() {
        assert Utils.tickOffset != Long.MIN_VALUE;
        return System.currentTimeMillis() - Utils.tickOffset;
    }

    public static Vector3 randomVector() {
        return new Vector3(Utils.random.nextFloat(), Utils.random.nextFloat(), Utils.random.nextFloat()).nor();
    }

    public static Quaternion nor(Quaternion q) {
        float len = q.len2();
        if (MathUtils.isEqual(len, 0f)) {
            q.idt();
        } else if (!MathUtils.isEqual(len, 1f)) {
            len = (float) Math.sqrt(len);
            q.w /= len;
            q.x /= len;
            q.y /= len;
            q.z /= len;
        }
        return q;
    }

    public static Quaternion abs(Quaternion q) {
        final float angle = q.getAxisAngle(Utils.tmpVector);
        if (angle < 0) {
            q.set(Utils.tmpVector.scl(-1), -angle);
        }
        return q;
    }

    public static void dump(Model model) {
        final StringBuilder sb = new StringBuilder();
        Utils.dump(sb, model);
        Logging.ROOT.info(sb.toString());
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void dump(StringBuilder sb, Model model) {
        sb.append(" ========== model =============== \n");
        if (model.materials != null) {
            sb.append(" -- material -- \n");
            for (final Material material : model.materials) {
                sb.append("  id: " + material.id + "\n");
                Utils.dumpa(sb, material.iterator());
                sb.append("  mask: " + material.getMask() + "\n");
                sb.append("  size: " + material.size() + "\n");
                sb.append("\n");
            }
        }
        if (model.meshes != null) {
            sb.append(" -- mesh -- \n");
            for (final Mesh mesh : model.meshes) {
                sb.append("  indices: " + mesh.getNumIndices());
                sb.append("  vertices: " + mesh.getNumVertices());
                sb.append("\n");
            }
        }
        if (model.meshParts != null) {
            sb.append(" -- meshpart -- \n");
            for (final MeshPart meshPart : model.meshParts) {
                sb.append("  id: " + meshPart.id + "\n");
                sb.append("  offset: " + meshPart.offset + "\n");
                sb.append("  size: " + meshPart.size + "\n");
                sb.append("  primitiveType: " + meshPart.primitiveType + "\n");
                Utils.dump(sb, meshPart.mesh);
                sb.append("\n");
            }
        }
        if (model.nodes != null) {
            sb.append(" -- nodes -- \n");
            for (final Node node : model.nodes) {
                sb.append("  id: " + node.id);
                sb.append("  parts: " + node.parts);
                sb.append("\n");
            }
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void dump(StringBuilder sb, Mesh mesh) {
        sb.append(" -- mesh -- \n");
        sb.append("     verticesBuffer: " + mesh.getVerticesBuffer() + "\n");
        sb.append("     indicesBuffer: " + mesh.getIndicesBuffer() + "\n");
        Utils.dumpva(sb, mesh.getVertexAttributes().iterator());
    }

    @SuppressWarnings({ "SpellCheckingInspection", "StringConcatenationInsideStringBufferAppend" })
    public static void dumpa(StringBuilder sb, Iterator<Attribute> iterator) {
        while (iterator.hasNext()) {
            sb.append("     attribute: " + iterator.next());
            sb.append("\n");
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void dumpva(StringBuilder sb, Iterator<VertexAttribute> iterator) {
        while (iterator.hasNext()) {
            sb.append("     attribute: " + iterator.next().alias);
            sb.append("\n");
        }
    }

    // scale might cause a rollover
    public static Quaternion scale(Quaternion quaternion, float scale) {
        Utils.tmpScale = quaternion.getAxisAngle(Utils.tmpVector);
        if (MathUtils.isEqual(Utils.tmpScale, 0, 0.0001f)) {
            Utils.tmpScale = scale * 360;
        } else {
            Utils.tmpScale = scale * Utils.tmpScale;
        }
        assert Utils.tmpScale <= 360 && Utils.tmpScale > 0 : "scale is not in range: " + Utils.tmpScale;
        return quaternion.set(Utils.tmpVector, Utils.tmpScale);
    }

    public static boolean isRenderThread() {
        assert Utils.renderThreadName != null : "thread name is null, need to call initRenderThreadName()";
        return Thread.currentThread().getName().equals(Utils.renderThreadName);
    }

    public static boolean isClose(float delta, Vector3 pos1, Vector3 pos2) {
        return Math.abs(pos1.x - pos2.x) < delta && Math.abs(pos1.y - pos2.y) < delta && Math.abs(pos1.z - pos2.z) < delta;
    }

    public static Quaternion createQuaternion(Vector3 axis, float angle) {
        if (Math.abs(angle) >= 360) {
            throw new GdxRuntimeException("the value for |angle| must be < 360, current value is '" + angle + "'");
        } else if (angle > 0) {
            return new Quaternion(axis.nor().scl(+1), angle * +1);
        } else if (angle < 0) {
            return new Quaternion(axis.nor().scl(-1), angle * -1);
        } else {
            return new Quaternion().idt();
        }
    }

    public static String printClasspath() {
        final StringBuilder stringBuilder = new StringBuilder();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        final URL[] urls = ((URLClassLoader) classLoader).getURLs();
        for (final URL url : urls) {
            stringBuilder.append(url.getFile());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public static String print(Vector3 v) {
        return "v("
                + Math.round(v.x * 1000) / 1000f + ","
                + Math.round(v.y * 1000) / 1000f + ","
                + Math.round(v.z * 1000) / 1000f + ")";
    }
}
