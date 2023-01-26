package de.firemage.autograder.core.integrated.scope.value;

/**
 * A value that can be used in for example a HashMap.
 */
public interface IndexValue extends Value {
    boolean isEqual(IndexValue other);

    int hashValue();
}
