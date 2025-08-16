package me.nd.factionsutils.utils.misc;

public class Position {

    protected double x, y, z;
    protected float yaw, pitch;

    // No-args constructor
    public Position() {
    }

    // All-args constructor
    public Position(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    // Constructor with x, y, z
    public Position(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    // Setters
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    // Equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 &&
               Double.compare(position.y, y) == 0 &&
               Double.compare(position.z, z) == 0 &&
               Float.compare(position.yaw, yaw) == 0 &&
               Float.compare(position.pitch, pitch) == 0;
    }

    // HashCode
    @Override
    public int hashCode() {
        int result = 17;
        long xBits = Double.doubleToLongBits(x);
        result = 31 * result + (int) (xBits ^ (xBits >>> 32));
        long yBits = Double.doubleToLongBits(y);
        result = 31 * result + (int) (yBits ^ (yBits >>> 32));
        long zBits = Double.doubleToLongBits(z);
        result = 31 * result + (int) (zBits ^ (zBits >>> 32));
        result = 31 * result + Float.floatToIntBits(yaw);
        result = 31 * result + Float.floatToIntBits(pitch);
        return result;
    }

    @Override
    public Position clone() {
        return new Position(
                x, y, z,
                yaw, pitch
        );
    }
}
