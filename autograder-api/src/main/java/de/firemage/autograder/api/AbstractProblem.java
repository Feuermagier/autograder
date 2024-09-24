package de.firemage.autograder.api;

import java.util.Optional;

public interface AbstractProblem {
    String getCheckName();
    Translatable getLinterName();
    Translatable getExplanation();
    String getDisplayLocation();
    AbstractCodePosition getPosition();
    String getType();
    default Optional<Integer> getMaximumProblemsForCheck() {
        return Optional.empty();
    }
}
