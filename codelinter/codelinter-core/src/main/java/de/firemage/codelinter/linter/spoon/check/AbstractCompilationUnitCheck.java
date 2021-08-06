package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.Problem;
import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

public abstract class AbstractCompilationUnitCheck implements Check {
    private final ProblemLogger logger;

    protected AbstractCompilationUnitCheck(ProblemLogger logger) {
        this.logger = logger;
    }

    protected void addProblem(CtElement element, String description, ProblemCategory category, String explanation, ProblemPriority priority) {
        this.logger.addInCodeProblem(element, description, category, explanation, priority);
    }

    @Override
    public void check(CtModel model, Factory factory) {
        factory.CompilationUnit().getMap().values().forEach(this::checkCompilationUnit);
    }

    public abstract void checkCompilationUnit(CtCompilationUnit compilationUnit);
}
