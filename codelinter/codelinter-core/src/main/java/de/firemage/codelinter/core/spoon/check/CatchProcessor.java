package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtCatch;

public class CatchProcessor extends AbstractLoggingProcessor<CtCatch> {
    private static final String EXPLANATION = "Empty catch block";

    public CatchProcessor(Check check) {
        super(check);
    }

    @Override
    public void process(CtCatch element) {
        if (element.getBody().getStatements().isEmpty()) {
            addProblem(element, EXPLANATION);
        }
    }
}
