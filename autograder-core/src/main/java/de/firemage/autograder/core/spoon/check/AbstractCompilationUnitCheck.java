package de.firemage.autograder.core.spoon.check;

import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.spoon.ProblemLogger;
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
