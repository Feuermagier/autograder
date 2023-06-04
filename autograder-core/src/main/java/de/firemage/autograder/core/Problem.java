package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;

public interface Problem {
    Check getCheck();

    Translatable getExplanation();

    String getDisplayLocation();

    ProblemType getProblemType();
}
