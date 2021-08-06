package de.firemage.codelinter.linter.spoon;

import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import spoon.reflect.declaration.CtElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProblemLogger {
    private final List<Problem> problems = new ArrayList<>();
    private final File root;

    public ProblemLogger(File root) {
        this.root = root;
    }

    public void addInCodeProblem(CtElement element, String description, ProblemCategory category, String explanation, ProblemPriority priority) {
        this.problems.add(new SpoonInCodeProblem(element, description, category, explanation, priority, this.root));
    }

    public void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    public List<Problem> getProblems() {
        return Collections.unmodifiableList(this.problems);
    }
}
