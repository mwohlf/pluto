package net.wohlfart.pluto.entity.fab.roam;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;
import java.util.List;

public class RoamBodyBootstrap {

    public List<RoamTriangle> create(Pool<RoamTriangle> nodePool) {
        final List<Vector3> verticesList = new ArrayList<>();

        final List<RoamTriangle> nodeList = new ArrayList<>();

        for (int i = 0; i < RoamBodyBootstrap.INITIAL_VERTICES.length; i += 3) {
            //noinspection PointlessArithmeticExpression
            verticesList.add(new Vector3(
                    RoamBodyBootstrap.INITIAL_VERTICES[i + 0],
                    RoamBodyBootstrap.INITIAL_VERTICES[i + 1],
                    RoamBodyBootstrap.INITIAL_VERTICES[i + 2]));
        }

        for (int i = 0; i < RoamBodyBootstrap.INITIAL_INDICES.length; i += 3) {
            final RoamTriangle triangle = nodePool.obtain();
            //noinspection PointlessArithmeticExpression
            triangle.vertices[0] = verticesList.get(RoamBodyBootstrap.INITIAL_INDICES[i + 0]);
            triangle.vertices[1] = verticesList.get(RoamBodyBootstrap.INITIAL_INDICES[i + 1]);
            triangle.vertices[2] = verticesList.get(RoamBodyBootstrap.INITIAL_INDICES[i + 2]);
            nodeList.add(triangle);
        }

        for (int x = 0; x < nodeList.size(); x++) {
            final RoamTriangle t1 = nodeList.get(x);
            for (final RoamTriangle t2 : nodeList) {
                for (int i = 0; i < 3; i++) {
                    final int index = findIndexInNeighbor(t1, i, t2);
                    if (index != -1) {
                        t1.neighbors[i] = t2;
                        t2.neighbors[index] = t1;
                        t1.ownIndexInNeighbor[i] = index;
                    }
                }
            }
        }

        validate(nodeList);

        return nodeList;
    }

    void validate(Iterable<RoamTriangle> nodeList) {
        for (final RoamTriangle triangle : nodeList) {
            if (triangle.hasChildren()) {
                return;
            }
            assert triangle != RoamTriangle.NULL_TRIANGLE : "found null triangle";
            for (int n = 0; n < 3; n++) {
                final RoamTriangle neighbor = triangle.neighbors[n];
                assert neighbor != RoamTriangle.NULL_TRIANGLE : "found null triangle as neighbor " + n;
                final int i = triangle.ownIndexInNeighbor[n];
                assert neighbor.neighbors[i] == triangle : "found unmatched neighbor";
            }
        }
    }

    private int findIndexInNeighbor(RoamTriangle current, int i, RoamTriangle neighbor) {
        //noinspection PointlessArithmeticExpression
        if (current.vertices[(i + 0) % 3].equals(neighbor.vertices[(i + 0) % 3])
                && current.vertices[(i + 1) % 3].equals(neighbor.vertices[(i + 2) % 3])) {
            //noinspection PointlessArithmeticExpression
            current.vertices[(i + 0) % 3] = neighbor.vertices[(i + 0) % 3];
            current.vertices[(i + 1) % 3] = neighbor.vertices[(i + 2) % 3];
            return (i + 2) % 3;
        }
        //noinspection PointlessArithmeticExpression
        if (current.vertices[(i + 0) % 3].equals(neighbor.vertices[(i + 1) % 3])
                && current.vertices[(i + 1) % 3].equals(neighbor.vertices[(i + 3) % 3])) {
            //noinspection PointlessArithmeticExpression
            current.vertices[(i + 0) % 3] = neighbor.vertices[(i + 1) % 3];
            current.vertices[(i + 1) % 3] = neighbor.vertices[(i + 3) % 3];
            return (i + 3) % 3;
        }
        //noinspection PointlessArithmeticExpression
        if (current.vertices[(i + 0) % 3].equals(neighbor.vertices[(i + 2) % 3])
                && current.vertices[(i + 1) % 3].equals(neighbor.vertices[(i + 4) % 3])) {
            //noinspection PointlessArithmeticExpression
            current.vertices[(i + 0) % 3] = neighbor.vertices[(i + 2) % 3];
            current.vertices[(i + 1) % 3] = neighbor.vertices[(i + 4) % 3];
            return (i + 4) % 3;
        }
        return -1;
    }

    // @formatter:off
    private static final float t = (1.0f + (float)Math.sqrt(5.0d)) / 2.0f;
    private static final float[] INITIAL_VERTICES = {
        -1f, RoamBodyBootstrap.t, 0,
        +1f, RoamBodyBootstrap.t, 0,
        -1f, -RoamBodyBootstrap.t, 0,
        +1f, -RoamBodyBootstrap.t, 0,

        0f, -1, RoamBodyBootstrap.t,
        0f, 1, RoamBodyBootstrap.t,
        0f, -1, -RoamBodyBootstrap.t,
        0f, 1, -RoamBodyBootstrap.t,

        RoamBodyBootstrap.t, 0, -1,
        RoamBodyBootstrap.t, 0, 1,
        -RoamBodyBootstrap.t, 0, -1,
        -RoamBodyBootstrap.t, 0, 1,
    };

    private static final short[] INITIAL_INDICES = {
        11, 5, 0,
        5, 1, 0,
        1, 7, 0,
        7,10, 0,
        10,11, 0,
        5, 9, 1,
        11, 4, 5,
        10, 2,11,
        7, 6,10,
        1, 8, 7,
        9, 4, 3,
        4, 2, 3,
        2, 6, 3,
        6, 8, 3,
        8, 9, 3,
        9, 5, 4,
        4,11, 2,
        2,10, 6,
        6, 7, 8,
        8, 1, 9,
    };

    // @formatter:on
}
