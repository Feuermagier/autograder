package de.firemage.autograder.extra.errorprone;

import java.io.Serializable;

@FunctionalInterface
public interface SerializableSupplier<T extends Serializable> extends Serializable {
    T get() throws Exception;
}
