package de.firemage.autograder.core.check.api;


import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.USE_ARRAYS_COPY_OF })
public class UseArraysCopyOf extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        if (!staticAnalysis.hasJavaUtilImport()) {
            return;
        }

        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                if (ctInvocation.isImplicit()
                    || !ctInvocation.getPosition().isValidPosition()) {
                    return;
                }

                if (ctInvocation.getTarget() == null
                    || ctInvocation.getTarget().getType() == null
                    || !(ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess)
                    || !TypeUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), System.class)
                    || !MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), void.class, "arraycopy", Object.class, int.class, Object.class, int.class, int.class)) {
                    return;
                }
                // System.arraycopy(src, srcPos, dest, destPos, length)

                CtExpression<?> source = ctInvocation.getArguments().get(0);
                CtExpression<?> sourcePosition = ctInvocation.getArguments().get(1);
                CtExpression<?> destination = ctInvocation.getArguments().get(2);
                CtExpression<?> destinationPosition = ctInvocation.getArguments().get(3);
                CtExpression<?> length = ctInvocation.getArguments().get(4);

                if (ExpressionUtil.isIntegerLiteral(sourcePosition, 0) && ExpressionUtil.isIntegerLiteral(destinationPosition, 0)) {
                    addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage(
                            "common-reimplementation",
                            Map.of(
                                "suggestion", "%s = Arrays.copyOf(%s, %s)".formatted(
                                    destination,
                                    source,
                                    length
                                )
                            )
                        ),
                        ProblemType.USE_ARRAYS_COPY_OF
                    );
                }
            }
        });
    }
}
