package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;

@ExecutableCheck(reportedProblems = { ProblemType.AVOID_LABELS })
public class AvoidLabels extends IntegratedCheck {
    public AvoidLabels() {
        super(new LocalizedMessage("avoid-labels"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtStatement>() {
            @Override
            public void process(CtStatement ctStatement) {
                if (ctStatement.getLabel() != null) {
                    addLocalProblem(
                        ctStatement,
                        new LocalizedMessage("avoid-labels"),
                        ProblemType.AVOID_LABELS
                    );
                }
            }
        });
    }
}
