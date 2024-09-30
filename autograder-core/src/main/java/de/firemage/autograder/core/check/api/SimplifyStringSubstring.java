package de.firemage.autograder.core.check.api;


import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.SIMPLIFY_STRING_SUBSTRING })
public class SimplifyStringSubstring extends IntegratedCheck {
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
                    || !TypeUtil.isTypeEqualTo(ctInvocation.getTarget().getType(), String.class)
                    || !MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), String.class, "substring", int.class, int.class)) {
                    return;
                }

                CtExpression<?> start = ctInvocation.getArguments().get(0);
                CtExpression<?> end = ctInvocation.getArguments().get(1);
                // ensure that the end is the length of the string
                if (!(end instanceof CtInvocation<?> endInvocation)
                    || !MethodUtil.isSignatureEqualTo(endInvocation.getExecutable(), int.class, "length")
                    || endInvocation.getTarget() == null
                    || !(endInvocation.getTarget().equals(ctInvocation.getTarget()))) {
                    return;
                }

                addLocalProblem(
                    ctInvocation,
                    new LocalizedMessage(
                        "common-reimplementation",
                        Map.of(
                            "suggestion", "%s.substring(%s)".formatted(
                                ctInvocation.getTarget(),
                                start
                            )
                        )
                    ),
                    ProblemType.SIMPLIFY_STRING_SUBSTRING
                );
            }
        });
    }
}
