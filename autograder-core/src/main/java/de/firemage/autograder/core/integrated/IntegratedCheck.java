package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.SourceInfo;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;

public abstract class IntegratedCheck implements Check {
    private final List<Problem> problems = new ArrayList<>();
    private SourceInfo sourceInfo;

    protected IntegratedCheck() {}

    protected void addLocalProblem(CtElement element, Translatable explanation, ProblemType problemType) {
        this.problems.add(new IntegratedInCodeProblem(this, element, explanation, problemType, this.sourceInfo));
    }

    protected void addLocalProblem(CodePosition position, Translatable explanation, ProblemType problemType) {
        this.problems.add(new Problem(this, position, explanation, problemType) {});
    }

    public List<Problem> run(StaticAnalysis staticAnalysis, SourceInfo sourceInfo) {
        this.problems.clear();
        this.sourceInfo = sourceInfo;
        this.check(staticAnalysis);
        return this.problems;
    }

    protected abstract void check(StaticAnalysis staticAnalysis);

    @Override
    public Translatable getLinter() {
        return new LocalizedMessage("linter-integrated");
    }

    protected SourceInfo getRoot() {
        return this.sourceInfo;
    }
}
