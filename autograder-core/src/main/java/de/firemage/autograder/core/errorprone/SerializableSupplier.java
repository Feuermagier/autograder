package de.firemage.autograder.core.errorprone;

import java.io.Serializable;

@FunctionalInterface
public interface SerializableSupplier<T extends Serializable> extends Serializable {
    T get() throws Exception;
}
