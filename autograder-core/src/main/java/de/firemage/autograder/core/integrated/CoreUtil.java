package de.firemage.autograder.core.integrated;

import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for functionality that does not fit in any other utility class.
 */
public final class CoreUtil {
    private static Optional<Boolean> AUTOGRADER_DEBUG_ENVIRONMENT = parseOptionalFlag(System.getenv("AUTOGRADER_DEBUG"));
    private static final boolean IS_IN_JUNIT_TEST = Arrays.stream(Thread.currentThread().getStackTrace())
        .anyMatch(element -> element.getClassName().startsWith("org.junit."));

    private CoreUtil() {
    }

    private static Optional<Boolean> parseOptionalFlag(String flag) {
        if (flag == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(flag) != 0);
        } catch (NumberFormatException exception) {
            return Optional.of(Boolean.parseBoolean(flag));
        }
    }

    /**
     * Enables debug mode for the autograder.
     * <br>
     * Note that this slows down the execution by a lot and should therefore only be used for debugging purposes.
     */
    public static void setDebugMode() {
        AUTOGRADER_DEBUG_ENVIRONMENT = Optional.of(true);
    }

    /**
     * Checks if the code is currently running in debug mode.
     * <br>
     * This is the case if the code is executed in a junit test or if the debug mode is explicitly enabled.
     *
     * @return {@code true} if the code is currently running in debug mode
     */
    public static boolean isInDebugMode() {
        return IS_IN_JUNIT_TEST || AUTOGRADER_DEBUG_ENVIRONMENT.orElse(false);
    }
}
