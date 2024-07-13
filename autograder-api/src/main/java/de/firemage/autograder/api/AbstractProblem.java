package de.firemage.autograder.api;

public interface AbstractProblem {
    String getCheckName();
    Translatable getLinterName();
    Translatable getExplanation();
    String getDisplayLocation();
    AbstractCodePosition getPosition();
    String getType();
}
