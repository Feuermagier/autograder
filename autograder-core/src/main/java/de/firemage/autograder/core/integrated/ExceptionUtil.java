package de.firemage.autograder.core.integrated;

import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

public final class ExceptionUtil {
    private static final Set<String> RUNTIME_EXCEPTIONS = Set.of(
        "java.lang.IllegalArgumentException",
        "java.lang.NullPointerException",
        "java.util.NoSuchElementException"
    );
    
    private static final Set<String> ERRORS = Set.of(
        "java.lang.Error",
        "java.lang.AssertionError"
    );

    private ExceptionUtil() {

    }

    public static boolean isRuntimeException(CtTypeReference<?> type) {
        return RUNTIME_EXCEPTIONS.contains(type.getQualifiedName());
    }
    
    public static boolean isError(CtTypeReference<?> type) {
        return ERRORS.contains(type.getQualifiedName());
    }
}
