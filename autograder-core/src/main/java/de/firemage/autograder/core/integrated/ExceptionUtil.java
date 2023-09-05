package de.firemage.autograder.core.integrated;

import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

public final class ExceptionUtil {
    private static final Set<String> RUNTIME_EXCEPTIONS = Set.of(
        "java.lang.RuntimeException",
        "java.lang.IllegalArgumentException",
        "java.lang.NullPointerException",
        "java.util.NoSuchElementException",
        "java.lang.NumberFormatException",
        "java.lang.ArithmeticException",
        "java.lang.ArrayIndexOutOfBoundsException",
        "java.lang.SecurityException",
        "java.lang.NegativeArraySizeException",
        "java.lang.ClassCastException",
        "java.lang.ArrayStoreException",
        "java.lang.EnumConstantNotPresentException",
        "java.lang.IllegalStateException",
        "java.lang.UnsupportedOperationException",
        "java.lang.IndexOutOfBoundsException",
        "java.lang.StringIndexOutOfBoundsException"
    );

    private static final Set<String> ERRORS = Set.of(
        "java.lang.Error",
        "java.lang.AssertionError",
        "java.lang.OutOfMemoryError",
        "java.lang.StackOverflowError"
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
