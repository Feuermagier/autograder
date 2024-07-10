package de.firemage.autograder.core.integrated;

import java.util.Arrays;

// TODO: rename?
public final class CoreUtil {
    private static final boolean IS_IN_JUNIT_TEST = Arrays.stream(Thread.currentThread().getStackTrace())
        .anyMatch(element -> element.getClassName().startsWith("org.junit."));

    private CoreUtil() {
    }

    public static boolean isInJunitTest() {
        return IS_IN_JUNIT_TEST;
    }
}
