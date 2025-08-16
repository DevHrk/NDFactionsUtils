package me.nd.factionsutils.utils.misc;

public class Vector3D {

    public static final Vector3D ZERO = new Vector3D(0, 0, 0);
    public static final Vector3D UNIT_X = new Vector3D(1, 0, 0);
    public static final Vector3D UNIT_Y = new Vector3D(0, 1, 0);
    public static final Vector3D UNIT_Z = new Vector3D(0, 0, 1);
    public static final Vector3D ONE = new Vector3D(1, 1, 1);

    protected final double x, y, z;

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vector3D(int x, int y, int z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }

    /**
     * Construct an instance.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public Vector3D(float x, float y, float z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }

    /**
     * Copy another vector.
     *
     * @param other the other vector
     */
    public Vector3D(Vector3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    /**
     * Construct a new instance with X and Z coordinates set to 0.
     *
     * <p>
     * One can also refer to a static {@link #ZERO}.</p>
     */
    public Vector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Get the X coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Get the X coordinate rounded.
     *
     * @return the x coordinate
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set the X coordinate.
     *
     * @param x the new X
     * @return a new vector
     */
    public Vector3D setX(double x) {
        return new Vector3D(x, y, z);
    }

    /**
     * Set the X coordinate.
     *
     * @param x the new X
     * @return a new vector
     */
    public Vector3D setX(int x) {
        return new Vector3D(x, y, z);
    }

    /**
     * Get the Y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Get the Y coordinate rounded.
     *
     * @return the y coordinate
     */
    public int getBlockY() {
        return (int) Math.round(y);
    }

    /**
     * Set the Y coordinate.
     *
     * @param y the new Y
     * @return a new vector
     */
    public Vector3D setY(double y) {
        return new Vector3D(x, y, z);
    }

    /**
     * Set the Y coordinate.
     *
     * @param y the new Y
     * @return a new vector
     */
    public Vector3D setY(int y) {
        return new Vector3D(x, y, z);
    }

    /**
     * Get the Z coordinate.
     *
     * @return the z coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Get the Z coordinate rounded.
     *
     * @return the z coordinate
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set the Z coordinate.
     *
     * @param z the new Z
     * @return a new vector
     */
    public Vector3D setZ(double z) {
        return new Vector3D(x, y, z);
    }

    /**
     * Set the Z coordinate.
     *
     * @param z the new Z
     * @return a new vector
     */
    public Vector3D setZ(int z) {
        return new Vector3D(x, y, z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3D add(Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param x the value to add
     * @param z the value to add
     * @return a new vector
     */
    public Vector3D add(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Add another vector to this vector and return the result as a new vector.
     *
     * @param x the value to add
     * @param z the value to add
     * @return a new vector
     */
    public Vector3D add(int x, int y, int z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Add a list of vectors to this vector and return the result as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector3D add(Vector3D... others) {
        double newX = x, newY = y, newZ = z;

        for (Vector3D other : others) {
            newX += other.x;
            newY += other.y;
            newZ += other.z;
        }

        return new Vector3D(newX, newY, newZ);
    }

    /**
     * Subtract another vector from this vector and return the result as a new vector.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Subtract another vector from this vector and return the result as a new vector.
     *
     * @param x the value to subtract
     * @param z the value to subtract
     * @return a new vector
     */
    public Vector3D subtract(double x, double y, double z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract another vector from this vector and return the result as a new vector.
     *
     * @param x the value to subtract
     * @param z the value to subtract
     * @return a new vector
     */
    public Vector3D subtract(int x, int y, int z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtract a list of vectors from this vector and return the result as a new vector.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector3D subtract(Vector3D... others) {
        double newX = x, newY = y, newZ = z;

        for (Vector3D other : others) {
            newX -= other.x;
            newY -= other.y;
            newZ -= other.z;
        }

        return new Vector3D(newX, newY, newZ);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3D multiply(Vector3D other) {
        return new Vector3D(x * other.x, x * other.y, z * other.z);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param x the value to multiply
     * @param z the value to multiply
     * @return a new vector
     */
    public Vector3D multiply(double x, double y, double z) {
        return new Vector3D(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply this vector by another vector on each component.
     *
     * @param x the value to multiply
     * @param z the value to multiply
     * @return a new vector
     */
    public Vector3D multiply(int x, int y, int z) {
        return new Vector3D(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Multiply this vector by zero or more vectors on each component.
     *
     * @param others an array of vectors
     * @return a new vector
     */
    public Vector3D multiply(Vector3D... others) {
        double newX = x, newY = y, newZ = z;

        for (Vector3D other : others) {
            newX *= other.x;
            newY *= other.y;
            newZ *= other.z;
        }

        return new Vector3D(newX, newY, newZ);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector3D multiply(double n) {
        return new Vector3D(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector3D multiply(float n) {
        return new Vector3D(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Perform scalar multiplication and return a new vector.
     *
     * @param n the value to multiply
     * @return a new vector
     */
    public Vector3D multiply(int n) {
        return new Vector3D(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector3D divide(Vector3D other) {
        return new Vector3D(x / other.x, this.y / other.y, z / other.z);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param x the value to divide by
     * @param z the value to divide by
     * @return a new vector
     */
    public Vector3D divide(double x, double y, double z) {
        return new Vector3D(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Divide this vector by another vector on each component.
     *
     * @param x the value to divide by
     * @param z the value to divide by
     * @return a new vector
     */
    public Vector3D divide(int x, int y, int z) {
        return new Vector3D(this.x / x, this.y / y, this.z / z);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector3D divide(int n) {
        return new Vector3D(x / n, y / n, z / n);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector3D divide(double n) {
        return new Vector3D(x / n, y / n, z / n);
    }

    /**
     * Perform scalar division and return a new vector.
     *
     * @param n the value to divide by
     * @return a new vector
     */
    public Vector3D divide(float n) {
        return new Vector3D(x / n, y / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Get the length, squared, of the vector.
     *
     * @return length, squared
     */
    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Get the distance between this vector and another vector.
     *
     * @param other the other vector
     * @return distance
     */
    public double distance(Vector3D other) {
        return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2) + Math.pow(other.z - z, 2));
    }

    /**
     * Get the distance between this vector and another vector, squared.
     *
     * @param other the other vector
     * @return distance
     */
    public double distanceSq(Vector3D other) {
        return Math.pow(other.x - x, 2)
            + Math.pow(other.y - y, 2)
            + Math.pow(other.z - z, 2);
    }

    /**
     * Get the normalized vector, which is the vector divided by its length, as a new vector.
     *
     * @return a new vector
     */
    public Vector3D normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other the other vector
     * @return the dot product of this and the other vector
     */
    public double dot(Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min the minimum point (X, Y, and Z are the lowest)
     * @param max the maximum point (X, Y, and Z are the lowest)
     * @return true if the vector is contained
     */
    public boolean containedWithin(Vector3D min, Vector3D max) {
        return x >= min.x && x <= max.x
            && y >= min.y && y <= max.y
            && z >= min.z && z <= max.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min the minimum point (X, Y, and Z are the lowest)
     * @param max the maximum point (X, Y, and Z are the lowest)
     * @return true if the vector is contained
     */
    public boolean containedWithinBlock(Vector3D min, Vector3D max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
            && getBlockY() >= min.getBlockY() && getBlockY() <= max.getBlockY()
            && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockZ();
    }

    /**
     * Floors the values of all components.
     *
     * @return a new vector
     */
    public Vector3D floor() {
        return new Vector3D(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return a new vector
     */
    public Vector3D ceil() {
        return new Vector3D(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.
     *
     * <p>
     * Components &lt; 0.5 are rounded down, otherwise up.</p>
     *
     * @return a new vector
     */
    public Vector3D round() {
        return new Vector3D(Math.floor(x + 0.5), Math.floor(x + 0.5), Math.floor(z + 0.5));
    }

    /**
     * Returns a vector with the absolute values of the components of this vector.
     *
     * @return a new vector
     */
    public Vector3D positive() {
        return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3D)) {
            return false;
        }

        Vector3D other = (Vector3D) obj;
        return other.x == this.x && other.z == this.z;

    }

    @Override
    public int hashCode() {
        return ((int) x << 16) ^ (int) z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return minimum
     */
    public static Vector3D getMinimum(Vector3D v1, Vector3D v2) {
        return new Vector3D(
            Math.min(v1.x, v2.x),
            Math.min(v1.y, v2.y),
            Math.min(v1.z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return maximum
     */
    public static Vector3D getMaximum(Vector3D v1, Vector3D v2) {
        return new Vector3D(
            Math.max(v1.x, v2.x),
            Math.max(v1.y, v2.y),
            Math.max(v1.z, v2.z)
        );
    }

}
