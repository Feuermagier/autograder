package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.GlobalProblem;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import spoon.reflect.declaration.CtElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class IntegratedCheck implements Check {

    private final LocalizedMessage description;

    private final List<Problem> problems = new ArrayList<>();
    private Path root;

    protected IntegratedCheck(LocalizedMessage description) {
        this.description = description;
    }

    protected void addGlobalProblem(LocalizedMessage explanation, ProblemType problemType) {
        this.problems.add(new GlobalProblem(this, explanation, problemType));
    }

    protected void addLocalProblem(CtElement element, LocalizedMessage explanation, ProblemType problemType) {
        this.problems.add(new IntegratedInCodeProblem(this, element, explanation, problemType, this.root));
    }

    public List<Problem> run(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis, Path root) {
        this.problems.clear();
        this.root = root;
        this.check(staticAnalysis, dynamicAnalysis);
        return this.problems;
    }

    protected abstract void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis);

    @Override
    public LocalizedMessage getLinter() {
        return new LocalizedMessage("linter-integrated");
    }

    @Override
    public LocalizedMessage getDescription() {
        return description;
    }

    protected Path getRoot() {
        return root;
    }
}
