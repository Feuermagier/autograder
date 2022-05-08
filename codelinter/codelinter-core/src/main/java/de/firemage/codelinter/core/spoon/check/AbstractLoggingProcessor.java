package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.GlobalProblem;
import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import java.util.List;

public abstract class AbstractLoggingProcessor<E extends CtElement> extends AbstractProcessor<E> implements CodeProcessor {
    private final ProblemLogger logger;
    private final Check check;

    protected AbstractLoggingProcessor(Check check) {
        this.check = check;
        this.logger = new ProblemLogger(check);
    }

    protected void addProblem(CtElement element, String explanation) {
        this.logger.addInCodeProblem(element, explanation);
    }

    protected void addProblem(String explanation) {
        this.logger.addProblem(new GlobalProblem(this.check, explanation));
    }

    @Override
    public List<Problem> check(CtModel model, Factory factory) {
        model.processWith(this);
        processingFinished();
        return this.logger.getProblems();
    }

    protected void processingFinished() {

    }
}
