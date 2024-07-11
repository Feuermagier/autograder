package de.firemage.autograder.api;

public interface Problem {
    String getCheckName();
    Translatable getLinterName();
    Translatable getExplanation();

    String getDisplayLocation();

    CodePosition getPosition();

    ProblemType getProblemType();
}
