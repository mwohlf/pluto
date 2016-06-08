package net.wohlfart.pluto.scene;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Position {

    public static final float SCALE2FLOAT = 1f / 4f;

    public double x;
    public double y;
    public double z;

    public final Vector3 logVector = new Vector3();
    public final Matrix3 tmpMatrix = new Matrix3();

    public Position() {
        this(0, 0, 0);
    }

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(Position position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public void idt() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Position get(Position v) {
        v.x = this.x;
        v.y = this.y;
        v.z = this.z;
        return v;
    }

    // TODO: remove this method
    public Vector3 get(Vector3 v) {
        v.x = (float) this.x;
        v.y = (float) this.y;
        v.z = (float) this.z;
        return v;
    }

    public float floatDist2() {
        return (float) (0
                + ((this.x * this.x) * Position.SCALE2FLOAT)
                + ((this.y * this.y) * Position.SCALE2FLOAT)
                + ((this.z * this.z) * Position.SCALE2FLOAT));
    }

    public void move(Vector3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public Position add(Position v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Position sub(Position v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public Position scl(float f) {
        this.x *= f;
        this.y *= f;
        this.z *= f;
        return this;
    }

    public void move(Position p) {
        this.x += p.x;
        this.y += p.y;
        this.z += p.z;
    }

    public Position set(Vector3 t) {
        this.x = t.x;
        this.y = t.y;
        this.z = t.z;
        return this;
    }

    public Position set(Position position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        return this;
    }

    public Position set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Position set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Position withTranslation(Matrix4 tmpMatrix) {
        this.x = tmpMatrix.val[Matrix4.M03];
        this.y = tmpMatrix.val[Matrix4.M13];
        this.z = tmpMatrix.val[Matrix4.M23];
        return this;
    }

    public Position prj(final Matrix4 matrix) {
        final float l_mat[] = matrix.val;
        final double l_w = 1f / (x * l_mat[Matrix4.M30] + y * l_mat[Matrix4.M31] + z * l_mat[Matrix4.M32] + l_mat[Matrix4.M33]);

        return this.set(
                (x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03]) * l_w,
                (x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13]) * l_w,
                (x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]) * l_w);
    }

    public Position mul(Quaternion q) {
        tmpMatrix.set(new float[] {
                1 - 2 * (q.y * q.y + q.z * q.z), 2 * (q.x * q.y - q.z * q.w), 2 * (q.x * q.z + q.y * q.w),
                2 * (q.x * q.y + q.z * q.w), 1 - 2 * (q.x * q.x + q.z * q.z), 2 * (q.y * q.z - q.x * q.w),
                2 * (q.x * q.z - q.y * q.w), 2 * (q.y * q.z + q.x * q.w), 1 - 2 * (q.x * q.x + q.y * q.y),
        });
        return mul(tmpMatrix.transpose());
    }

    public Position mul(Matrix3 matrix) {
        final float l_mat[] = matrix.val;
        return set(
                x * l_mat[Matrix3.M00] + y * l_mat[Matrix3.M01] + z * l_mat[Matrix3.M02],
                x * l_mat[Matrix3.M10] + y * l_mat[Matrix3.M11] + z * l_mat[Matrix3.M12],
                x * l_mat[Matrix3.M20] + y * l_mat[Matrix3.M21] + z * l_mat[Matrix3.M22]);
    }

    public Vector3 logVector3() {
        // TODO Auto-generated method stub
        //return logVector.set((float) this.x / 1.5f, (float) this.y / 1.5f, (float) this.z / 1.5f);
        return logVector.set(scale(this.x), scale(this.y), scale(this.z));
    }

    private float scale(double d) {
        return (float) d;
        //return (float) Math.log(Math.abs(d - 1)) * (float) Math.signum(d - 1);
    }

    public double len2() {
        return x * x + y * y + z * z;
    }

    public double len() {
        return Math.sqrt(len2());
    }

    public Position nor() {
        final double len = len();
        x /= len;
        y /= len;
        z /= len;
        return this;
    }

    @Override
    public String toString() {
        return "Position ["
                + "x=" + x + ", "
                + "y=" + y + ", "
                + "z=" + z + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Position other = (Position) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

}
