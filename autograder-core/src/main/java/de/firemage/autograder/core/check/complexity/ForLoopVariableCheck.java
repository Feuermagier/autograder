package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFor;

@ExecutableCheck(reportedProblems = {ProblemType.FOR_WITH_MULTIPLE_VARIABLES})
public class ForLoopVariableCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.getForInit().size() > 1) {
                    addLocalProblem(
                        ctFor.getForInit().get(0),
                        new LocalizedMessage("for-loop-var"),
                        ProblemType.FOR_WITH_MULTIPLE_VARIABLES
                    );
                }
            }
        });
    }
}
