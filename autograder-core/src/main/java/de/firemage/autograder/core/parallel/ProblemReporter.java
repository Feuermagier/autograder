package de.firemage.autograder.core.parallel;

import de.firemage.autograder.api.Problem;
import de.firemage.autograder.core.ProblemImpl;

import java.util.Collection;

public interface ProblemReporter {
    void reportProblem(ProblemImpl problem);
    void reportProblems(Collection<ProblemImpl> problems);
}
