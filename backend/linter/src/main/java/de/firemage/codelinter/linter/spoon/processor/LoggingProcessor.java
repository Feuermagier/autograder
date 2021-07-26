package de.firemage.codelinter.linter.spoon.processor;

import de.firemage.codelinter.linter.spoon.Problem;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

public abstract class LoggingProcessor<E extends CtElement> extends AbstractProcessor<E> {
    private final ProblemLogger logger;

    public LoggingProcessor(ProblemLogger logger) {
        this.logger = logger;
    }

    protected void addProblem(Problem problem) {
        this.logger.addProblem(problem);
    }
}
