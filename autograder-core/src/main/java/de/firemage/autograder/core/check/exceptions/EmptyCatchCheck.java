package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;

@ExecutableCheck(reportedProblems = {ProblemType.EMPTY_CATCH})
public class EmptyCatchCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtCatch>() {
            @Override
            public void process(CtCatch ctCatch) {
                if (SpoonUtil.getEffectiveStatements(ctCatch.getBody()).isEmpty()) {
                    addLocalProblem(
                        ctCatch,
                        new LocalizedMessage("empty-catch-exp"),
                        ProblemType.EMPTY_CATCH
                    );
                }
            }
        });
    }
}
