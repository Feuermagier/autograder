package de.firemage.autograder.core.check.debug;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssert;

@ExecutableCheck(reportedProblems = {ProblemType.ASSERT})
public class AssertCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssert<?>>() {
            @Override
            public void process(CtAssert<?> element) {
                addLocalProblem(element, new LocalizedMessage("assert-used-exp"), ProblemType.ASSERT);
            }
        });
    }
}
