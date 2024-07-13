package de.firemage.autograder.api;

public interface Problem {
    String getCheckName();
    Translatable getLinterName();
    Translatable getExplanation();

    String getDisplayLocation();

    AbstractCodePosition getPosition();

    ProblemType getProblemType();
}
