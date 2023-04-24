package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.STRING_IS_EMPTY_REIMPLEMENTED})
public class StringIsEmptyReimplementationCheck extends IntegratedCheck {
    private static LocalizedMessage formatExplanation(CtElement element) {
        return new LocalizedMessage("string-is-empty-exp-emptiness", Map.of("exp", element.toString()));
    }

    private static LocalizedMessage formatNegatedExplanation(CtElement element) {
        return new LocalizedMessage("string-is-empty-exp-non-emptiness", Map.of("exp", element.toString()));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> invocation) {
                if (invocation.getTarget() == null) {
                    return;
                }

                if (SpoonUtil.isString(invocation.getTarget().getType())) {
                    if (equalsWithEmptyString(invocation)) {
                        addLocalProblem(invocation, formatExplanation(invocation),
                            ProblemType.STRING_IS_EMPTY_REIMPLEMENTED);
                    } else if (lengthZeroCheck(invocation)) {
                        addLocalProblem(invocation.getParent(), formatExplanation(invocation.getParent()),
                            ProblemType.STRING_IS_EMPTY_REIMPLEMENTED);
                    } else if (lengthGreaterThanZeroCheck(invocation)) {
                        addLocalProblem(invocation.getParent(), formatNegatedExplanation(invocation.getParent()),
                            ProblemType.STRING_IS_EMPTY_REIMPLEMENTED);
                    }
                }
            }
        });
    }

    private boolean equalsWithEmptyString(CtInvocation<?> invocation) {
        CtExecutableReference<?> executable = invocation.getExecutable();
        return executable.getSignature().equals("equals(java.lang.Object)")
            && SpoonUtil.isStringLiteral(invocation.getArguments().get(0), "");
    }

    private boolean lengthZeroCheck(CtInvocation<?> invocation) {
        CtExecutableReference<?> executable = invocation.getExecutable();
        return executable.getSignature().equals("length()")
            && invocation.getParent() instanceof CtBinaryOperator<?> operator
            && operator.getKind().equals(BinaryOperatorKind.EQ)
            && (SpoonUtil.isIntegerLiteral(operator.getLeftHandOperand(), 0)
            || SpoonUtil.isIntegerLiteral(operator.getRightHandOperand(), 0));
    }

    private boolean lengthGreaterThanZeroCheck(CtInvocation<?> invocation) {
        CtExecutableReference<?> executable = invocation.getExecutable();
        if (!executable.getSignature().equals("length()")) {
            return false;
        }
        if (invocation.getParent() instanceof CtBinaryOperator<?> operator) {
            return switch (operator.getKind()) {
                case EQ -> SpoonUtil.isIntegerLiteral(operator.getLeftHandOperand(), 0)
                    || SpoonUtil.isIntegerLiteral(operator.getRightHandOperand(), 0);
                case GT -> SpoonUtil.isIntegerLiteral(operator.getRightHandOperand(), 0);
                case GE -> SpoonUtil.isIntegerLiteral(operator.getRightHandOperand(), 1);
                case LT -> SpoonUtil.isIntegerLiteral(operator.getLeftHandOperand(), 0);
                case LE -> SpoonUtil.isIntegerLiteral(operator.getLeftHandOperand(), 1);
                default -> false;
            };
        } else {
            return false;
        }
    }
}
