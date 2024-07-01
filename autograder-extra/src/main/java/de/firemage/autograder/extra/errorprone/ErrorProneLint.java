package de.firemage.autograder.extra.errorprone;

import com.google.errorprone.BugPattern;
import org.reflections.Reflections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public final class ErrorProneLint implements Serializable {
    private static final Collection<String> VALID_LINTS;

    static {
        Reflections reflections = new Reflections("com.google.errorprone.bugpatterns");

        // query all lints in error-prone (they are annotated with BugPattern)
        VALID_LINTS = reflections.getTypesAnnotatedWith(BugPattern.class)
                .stream()
                .flatMap(ctClass -> {
                    BugPattern annotation = ctClass.getAnnotation(BugPattern.class);
                    Collection<String> names = new ArrayList<>(Arrays.asList(annotation.altNames()));
                    if (annotation.name().isEmpty()) {
                        names.add(ctClass.getSimpleName());
                    } else {
                        names.add(annotation.name());
                    }

                    return names.stream();
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    private final String lint;

    private ErrorProneLint(String lint) {
        this.lint = lint;
    }

    public static ErrorProneLint fromString(String string) {
        if (VALID_LINTS.contains(string)) {
            return new ErrorProneLint(string);
        }

        throw new IllegalArgumentException("Unknown lint '%s'".formatted(string));
    }

    @Override
    public String toString() {
        return this.lint;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ErrorProneLint that)) return false;

        return this.lint.equals(that.lint);
    }

    @Override
    public int hashCode() {
        return this.lint.hashCode();
    }
}
