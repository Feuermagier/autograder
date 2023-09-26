package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtParameter;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.REASSIGNED_PARAMETER})
public class ReassignedParameterCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtParameter<?>>() {
            @Override
            public void process(CtParameter<?> ctParameter) {
                if (ctParameter.isImplicit() || !ctParameter.getPosition().isValidPosition()) {
                    return;
                }

                if (!SpoonUtil.isEffectivelyFinal(ctParameter)) {
                    addLocalProblem(
                        ctParameter,
                        new LocalizedMessage(
                            "reassigned-parameter",
                            Map.of("name", ctParameter.getSimpleName())
                        ),
                        ProblemType.REASSIGNED_PARAMETER
                    );
                }
            }
        });
    }
}
