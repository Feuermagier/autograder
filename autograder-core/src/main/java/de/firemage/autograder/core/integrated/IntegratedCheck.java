package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.Translatable;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import spoon.reflect.declaration.CtElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class IntegratedCheck implements Check {
    private final List<Problem> problems = new ArrayList<>();
    private Path root;

    protected IntegratedCheck() {}

    protected void addLocalProblem(CtElement element, Translatable explanation, ProblemType problemType) {
        this.problems.add(new IntegratedInCodeProblem(this, element, explanation, problemType, this.root));
    }

    protected void addLocalProblem(CodePosition position, Translatable explanation, ProblemType problemType) {
        this.problems.add(new ProblemImpl(this, position, explanation, problemType) {});
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

    protected Path getRoot() {
        return root;
    }
}
