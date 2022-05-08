package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtAssert;

public class AssertProcessor extends AbstractLoggingProcessor<CtAssert<?>> {
    private static final String EXPLANATION = "Used 'assert'";

    public AssertProcessor(Check check) {
        super(check);
    }

    @Override
    public void process(CtAssert<?> element) {
        addProblem(element, EXPLANATION);
    }
}
