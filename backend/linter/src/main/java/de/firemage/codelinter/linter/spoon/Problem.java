package de.firemage.codelinter.linter.spoon;

public interface Problem {
    String getDescription();
    ProblemCategory getCategory();
}
