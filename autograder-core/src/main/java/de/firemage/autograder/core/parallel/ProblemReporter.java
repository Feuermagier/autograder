package de.firemage.autograder.core.parallel;

import de.firemage.autograder.core.Problem;

import java.util.Collection;

public interface ProblemReporter {
    void reportProblem(Problem problem);
    void reportProblems(Collection<Problem> problems);
}
