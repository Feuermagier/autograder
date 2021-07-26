package de.firemage.codelinter.linter.spoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProblemLogger {
    private final List<Problem> problems = new ArrayList<>();

    public void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    public List<Problem> getProblems() {
        return Collections.unmodifiableList(this.problems);
    }
}
