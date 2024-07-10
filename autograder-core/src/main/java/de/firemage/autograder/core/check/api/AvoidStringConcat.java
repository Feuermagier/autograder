package de.firemage.autograder.core.check.api;


import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.AVOID_STRING_CONCAT })
public class AvoidStringConcat extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                if (ctInvocation.isImplicit()
                    || !ctInvocation.getPosition().isValidPosition()) {
                    return;
                }

                if (ctInvocation.getTarget() == null
                    || ctInvocation.getTarget().getType() == null
                    || !MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), String.class, "concat", String.class)) {
                    return;
                }

                addLocalProblem(
                    ctInvocation,
                    new LocalizedMessage(
                        "common-reimplementation",
                        Map.of(
                            "suggestion", "%s + %s".formatted(
                                ctInvocation.getTarget(),
                                ctInvocation.getArguments().get(0)
                            )
                        )
                    ),
                    ProblemType.AVOID_STRING_CONCAT
                );
            }
        });
    }
}
