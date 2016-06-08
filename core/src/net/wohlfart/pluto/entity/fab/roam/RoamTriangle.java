package net.wohlfart.pluto.entity.fab.roam;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// see: http://archive.gamedev.net/archive/reference/programming/features/procplanet1/
@SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "TODO: work in progress")
public class RoamTriangle implements Poolable {

    // vertices in order top, left, right
    protected final Vector3[] vertices = { NULL_VERTEX, NULL_VERTEX, NULL_VERTEX };
    // neighbor triangles in order left, bottom, right
    protected final RoamTriangle[] neighbors = { NULL_TRIANGLE, NULL_TRIANGLE, NULL_TRIANGLE };
    // parent, might be null
    protected RoamTriangle parent;
    // inner children or this triangle, this is their parent, they are in the order top, left, right, center
    protected final RoamTriangle[] children = { NULL_TRIANGLE, NULL_TRIANGLE, NULL_TRIANGLE, NULL_TRIANGLE };
    // to avoid looking for the adjacent triangle this is this triangles index in the neighbor
    protected final int[] ownIndexInNeighbor = { -1, -1, -1 };

    static final RoamTriangle NULL_TRIANGLE = new RoamTriangle() {
        @Override
        public String toString() {
            return "NULL_TRIANGLE";
        }
    };

    @SuppressWarnings("serial")
    static final Vector3 NULL_VERTEX = new Vector3(Float.NaN, Float.NaN, Float.NaN) {
        @Override
        public String toString() {
            return "NULL_VERTEX";
        }
    };

    @Override
    public void reset() {
        parent = NULL_TRIANGLE;
        vertices[0] = vertices[1] = vertices[2] = NULL_VERTEX;
        neighbors[0] = neighbors[1] = neighbors[2] = NULL_TRIANGLE;
        children[0] = children[1] = children[2] = children[3] = NULL_TRIANGLE;
    }

    boolean hasChildren() {
        return children[0] != NULL_TRIANGLE
                && children[1] != NULL_TRIANGLE
                && children[2] != NULL_TRIANGLE;
    }

    // now comes the tricky part, creating 3 children
    RoamTriangle[] getOrCreateChildren(Pool<RoamTriangle> nodePool) {
        if (!hasChildren()) {
            final Vector3 m[] = {
                    reuseNeighborMidpoint(0),
                    reuseNeighborMidpoint(1),
                    reuseNeighborMidpoint(2),
            };

            // create the children, setting parents and vertices
            children[0] = nodePool.obtain().withParent(this).withVertices(vertices[0], m[0], m[2]);
            children[1] = nodePool.obtain().withParent(this).withVertices(m[0], vertices[1], m[1]);
            children[2] = nodePool.obtain().withParent(this).withVertices(m[2], m[1], vertices[2]);
            children[3] = nodePool.obtain().withParent(this).withVertices(m[1], m[2], m[0]);

            for (int i = 0; i < 3; i++) {
                if (neighbors[i].hasChildren()) {
                    link(neighbors[i], (ownIndexInNeighbor[i] + 1) % 3, this, i);
                    link(neighbors[i], ownIndexInNeighbor[i], this, (i + 1) % 3);
                }
            }

            link(this, 0, this, 3);
            link(this, 1, this, 3);
            link(this, 2, this, 3);
        }

        return children;
    }

    private void link(RoamTriangle parent0, int index0, RoamTriangle parent1, int index1) {
        final RoamTriangle child0 = parent0.children[index0];
        final RoamTriangle child1 = parent1.children[index1];
        //
        for (int a = 0; a < 3; a++) {
            for (int b = 0; b < 3; b++) {
                if (isTriangleMatch(child0, child1, a, b)) {
                    child0.neighbors[a] = child1;
                    child1.neighbors[b] = child0;
                    child0.ownIndexInNeighbor[a] = b;
                    child1.ownIndexInNeighbor[b] = a;
                    child0.vertices[a] = child1.vertices[(b + 1) % 3];
                    child0.vertices[(a + 1) % 3] = child1.vertices[b];
                    return;
                }
            }
        }
        throw new IllegalStateException("no match found between: " + child0 + " and: " + child1);
    }

    private boolean isTriangleMatch(RoamTriangle child0, RoamTriangle child1, int s0, int s1) {
        return equal(child0.vertices[s0], child1.vertices[(s1 + 1) % 3])
                && equal(child0.vertices[(s0 + 1) % 3], child1.vertices[s1]);
    }

    private boolean equal(Vector3 v0, Vector3 v1) {
        return (Math.abs(v0.x - v1.x) < 0.0001f
                && Math.abs(v0.y - v1.y) < 0.0001f
                && Math.abs(v0.z - v1.z) < 0.0001f);
    }

    // check neighbor for midpoint or create a new one
    private Vector3 reuseNeighborMidpoint(int i) {
        if (neighbors[i] != NULL_TRIANGLE && neighbors[i].hasChildren()) {
            final RoamTriangle neighbor = neighbors[i];
            assert (neighbor.neighbors[ownIndexInNeighbor[i]] == this) : "wrong neighbor connection " + neighbor + " should be " + this;
            switch (ownIndexInNeighbor[i]) {
                case 0:
                    assert neighbor.children[0].vertices[1] == neighbor.children[1].vertices[0];
                    return neighbor.children[0].vertices[1];
                case 1:
                    assert neighbor.children[1].vertices[2] == neighbor.children[2].vertices[1];
                    return neighbor.children[1].vertices[2];
                case 2:
                    assert neighbor.children[2].vertices[0] == neighbor.children[0].vertices[2];
                    return neighbor.children[2].vertices[0];
                default:
                    throw new IllegalStateException("undefined edge index: " + i);
            }
        } else {
            return center(vertices[i], vertices[(i + 1) % 3]).nor();
        }
    }

    private RoamTriangle withParent(RoamTriangle parent) {
        this.parent = parent;
        return this;
    }

    private RoamTriangle withVertices(Vector3 v0, Vector3 v1, Vector3 v2) {
        vertices[0] = v0;
        vertices[1] = v1;
        vertices[2] = v2;
        return this;
    }

    private Vector3 center(Vector3 a, Vector3 b) {
        return new Vector3(
                (a.x + b.x) / 2f,
                (a.y + b.y) / 2f,
                (a.z + b.z) / 2f);
    }

    @Override
    public String toString() {
        return "id: " + System.identityHashCode(this) + "\n"
                + " hasChildren: " + hasChildren() + "\n"
                + "   v0: " + vertices[0] + "\n"
                + "   v1: " + vertices[1] + "\n"
                + "   v2: " + vertices[2] + "\n"
                + "   parent: "
                + System.identityHashCode(parent) + "\n"
                + "   children: "
                + System.identityHashCode(children[0]) + ", "
                + System.identityHashCode(children[1]) + ", "
                + System.identityHashCode(children[2]) + ", "
                + System.identityHashCode(children[3]) + "\n"
                + "   neighbors: "
                + System.identityHashCode(neighbors[0]) + ", "
                + System.identityHashCode(neighbors[1]) + ", "
                + System.identityHashCode(neighbors[2]) + "\n"

        ;
    }

}
