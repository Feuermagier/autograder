package de.firemage.codelinter.linter;

public interface Problem {
    String getDescription();
    ProblemCategory getCategory();
    String getExplanation();
    ProblemPriority getPriority();
}
