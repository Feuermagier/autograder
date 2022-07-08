package de.firemage.autograder.core.check.debug;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssert;

public class AssertCheck extends IntegratedCheck {
    private static final String DESCRIPTION = """
            Assertions crash the entire program if they evaluate to false.
            Also they can be disabled, so never rely on them to e.g. check user input.
            They are great for testing purposes, but should not be part of your final solution.
            If you want to document an invariant, consider a comment.""";

    public AssertCheck() {
        super(DESCRIPTION);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssert<?>>() {
            @Override
            public void process(CtAssert<?> element) {
                addLocalProblem(element, "Assert used");
            }
        });
    }
}
