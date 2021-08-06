package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.SpoonInCodeProblem;
import spoon.reflect.code.CtAssert;

public class AssertProcessor extends AbstractLoggingProcessor<CtAssert<?>> {
    private static final String DESCRIPTION = "Used 'assert'";
    private static final String EXPLANATION = """
            Assertions crash the entire program if they evaluate to false.
            Also they can be disabled, so never rely on them to e.g. check user input.
            They are great for testing purposes, but should not be part of your final solution.
            If you want to document an invariant, consider a comment.""";

    public AssertProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtAssert<?> element) {
        addProblem(element, DESCRIPTION, ProblemCategory.JAVA_FEATURE, EXPLANATION, ProblemPriority.FIX_RECOMMENDED);
    }
}
