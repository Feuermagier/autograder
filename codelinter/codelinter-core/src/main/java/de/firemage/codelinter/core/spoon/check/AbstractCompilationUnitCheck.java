package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import java.util.List;

public abstract class AbstractCompilationUnitCheck implements CodeProcessor {
    private final ProblemLogger logger;
    private final Check check;

    protected AbstractCompilationUnitCheck(Check check) {
        this.check = check;
        this.logger = new ProblemLogger(check);
    }

    protected void addProblem(CtElement element, String explanation) {
        this.logger.addInCodeProblem(element, explanation);
    }

    @Override
    public List<Problem> check(CtModel model, Factory factory) {
        factory.CompilationUnit().getMap().values().forEach(this::checkCompilationUnit);
        return this.logger.getProblems();
    }

    public abstract void checkCompilationUnit(CtCompilationUnit compilationUnit);
}
