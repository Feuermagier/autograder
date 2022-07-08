package de.firemage.autograder.core.spoon;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.check.Check;
import spoon.reflect.declaration.CtElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProblemLogger {
    private final List<Problem> problems = new ArrayList<>();
    private final Check check;

    public ProblemLogger(Check check) {
        this.check = check;
    }

    public void addInCodeProblem(CtElement element, String explanation) {
        this.problems.add(new SpoonInCodeProblem(this.check, element, explanation));
    }

    public void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    public List<Problem> getProblems() {
        return Collections.unmodifiableList(this.problems);
    }
}
