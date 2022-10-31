package de.firemage.autograder.core.check.debug;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssert;

public class AssertCheck extends IntegratedCheck {
    public AssertCheck() {
        super(new LocalizedMessage("assert-used"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssert<?>>() {
            @Override
            public void process(CtAssert<?> element) {
                addLocalProblem(element, new LocalizedMessage("assert-used-exp"), ProblemType.ASSERT);
            }
        });
    }
}
