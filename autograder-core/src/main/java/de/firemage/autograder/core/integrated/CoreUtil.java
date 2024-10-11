package de.firemage.autograder.core.integrated;

import org.apache.commons.io.FilenameUtils;
import spoon.reflect.CtModel;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.CompoundSourcePosition;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

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

    public static void visitCtCompilationUnit(CtModel ctModel, Consumer<? super CtCompilationUnit> lambda) {
        // it is not possible to visit CtCompilationUnit through the processor API.
        //
        // in https://github.com/INRIA/spoon/issues/5168 the below code is mentioned as a workaround:
        ctModel
            .getAllTypes()
            .stream()
            .map(CtType::getPosition)
            .filter(SourcePosition::isValidPosition)
            .map(SourcePosition::getCompilationUnit)
            // visit each compilation unit only once
            .distinct()
            .forEach(lambda);
    }

    /**
     * Converts the provided source position into a human-readable string.
     *
     * @param sourcePosition the source position as given by spoon
     * @return a human-readable string representation of the source position
     */
    public static String formatSourcePosition(SourcePosition sourcePosition) {
        return String.format("%s:L%d", getBaseName(sourcePosition.getFile().getName()), sourcePosition.getLine());
    }

    public static String getBaseName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return FilenameUtils.removeExtension(new File(fileName).getName());
    }

    public static String truncatedSuggestion(CtElement ctElement) {
        StringJoiner result = new StringJoiner(System.lineSeparator());

        for (String line : ctElement.toString().split("\\r?\\n")) {
            int newLineLength = 0;

            // this ensures that the truncation is the same on linux and windows
            if (!result.toString().contains("\r\n")) {
                newLineLength += (int) result.toString().chars().filter(ch -> ch == '\n').count();
            }

            if (result.length() + newLineLength > 150) {
                if (line.startsWith(" ")) {
                    result.add("...".indent(line.length() - line.stripIndent().length()).stripTrailing());
                } else {
                    result.add("...");
                }

                if (result.toString().startsWith("{")) {
                    result.add("}");
                }

                break;
            }

            result.add(line);
        }

        return result.toString();
    }

    public static SourcePosition getNamePosition(CtNamedElement ctNamedElement) {
        SourcePosition position = ctNamedElement.getPosition();

        if (position instanceof CompoundSourcePosition compoundSourcePosition) {
            return ctNamedElement.getFactory().createSourcePosition(
                position.getCompilationUnit(),
                compoundSourcePosition.getNameStart(),
                compoundSourcePosition.getNameEnd(),
                position.getCompilationUnit().getLineSeparatorPositions()
            );
        }

        return position;
    }

    public static boolean isInstanceOfAny(Object object, Class<?>... classes) {
        return isInstanceOfAny(object, Arrays.asList(classes));
    }

    public static boolean isInstanceOfAny(Object object, Iterable<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(object)) {
                return true;
            }
        }

        return false;
    }
}
