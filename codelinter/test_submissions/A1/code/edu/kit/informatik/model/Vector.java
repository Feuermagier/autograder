package edu.kit.informatik.model;

import edu.kit.informatik.StringConstants;

import java.util.Objects;

/**
 * Represents an immutable two-dimensional vector. The origin is on the bottom left edge with the positive x-axis going
 * to the right and the positive y-axis going upwards
 *
 * @author ukidf
 * @version 1.0
 */
public class Vector {
    /**
     * Represents the vector (0, 1)T
     */
    public static final Vector UP = new Vector(0, 1);
    /**
     * Represents the vector (1, 0)T
     */
    public static final Vector RIGHT = new Vector(1, 0);
    /**
     * Represents the vector (0, -1)T
     */
    public static final Vector DOWN = new Vector(0, -1);
    /**
     * Represents the vector (-1, 0)T
     */
    public static final Vector LEFT = new Vector(-1, 0);

    private final int x;
    private final int y;

    /**
     * Creates a new vector
     *
     * @param x The x coordinate of this vector
     * @param y The y coordinate of this vector
     */
    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate of this vector
     *
     * @return The x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate of this vector
     *
     * @return The y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Subtracts the given vector from this vector
     *
     * @param other The vector that should be subtracted
     * @return The result
     */
    public Vector subtract(Vector other) {
        return new Vector(x - other.x, y - other.y);
    }

    /**
     * Adds the given vector to this vector
     *
     * @param other The vector that should be added
     * @return The result
     */
    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y);
    }

    /**
     * Normalizes this vector to the length of 1
     *
     * @return The normalized vector
     */
    public Vector normalize() {
        return new Vector(x / length(), y / length());
    }

    /**
     * Inverts this vector by changing the sign of ti's coordinates
     *
     * @return The inverted vector
     */
    public Vector inverted() {
        return multiply(-1);
    }

    /**
     * Multiplies this vector with the given scalar
     *
     * @param value The scalar
     * @return The resized Vector
     */
    public Vector multiply(int value) {
        return new Vector(x * value, y * value);
    }

    /**
     * Calculates the length of this vector
     *
     * @return The length of this vector
     */
    public int length() {
        return (int) Math.sqrt(((long) x) * x + ((long) y) * y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return x == vector.x && y == vector.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return StringConstants.LEFT_BRACKET + x + StringConstants.COMMA + y + StringConstants.RIGHT_BRACKET;
    }
}
