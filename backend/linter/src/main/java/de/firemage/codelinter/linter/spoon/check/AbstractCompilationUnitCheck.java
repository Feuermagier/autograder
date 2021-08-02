package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.factory.Factory;

public abstract class AbstractCompilationUnitCheck implements Check {
    private final ProblemLogger logger;

    protected AbstractCompilationUnitCheck(ProblemLogger logger) {
        this.logger = logger;
    }

    protected void addProblem(Problem problem) {
        this.logger.addProblem(problem);
    }

    @Override
    public void check(CtModel model, Factory factory) {
        factory.CompilationUnit().getMap().values().forEach(this::checkCompilationUnit);
    }

    public abstract void checkCompilationUnit(CtCompilationUnit compilationUnit);
}
