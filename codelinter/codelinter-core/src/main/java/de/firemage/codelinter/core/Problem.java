package de.firemage.codelinter.core;

public interface Problem {
    String getDescription();

    ProblemCategory getCategory();

    String getExplanation();

    ProblemPriority getPriority();

    String getDisplayLocation();
}
