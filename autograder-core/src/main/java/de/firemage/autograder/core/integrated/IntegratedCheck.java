package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.GlobalProblem;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import lombok.Getter;
import spoon.reflect.declaration.CtElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class IntegratedCheck implements Check {

    @Getter
    private final String description;

    private final List<Problem> problems = new ArrayList<>();
    private Path root;

    protected IntegratedCheck(String description) {
        this.description = description;
    }

    protected void addGlobalProblem(String explanation) {
        this.problems.add(new GlobalProblem(this, explanation));
    }

    protected void addLocalProblem(CtElement element, String explanation) {
        this.problems.add(new IntegratedInCodeProblem(this, element, explanation, this.root));
    }

    public List<Problem> run(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis, Path root) {
        this.problems.clear();
        this.root = root;
        this.check(staticAnalysis, dynamicAnalysis);
        return this.problems;
    }

    protected abstract void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis);

    @Override
    public String getLinter() {
        return "Integrated";
    }
}
