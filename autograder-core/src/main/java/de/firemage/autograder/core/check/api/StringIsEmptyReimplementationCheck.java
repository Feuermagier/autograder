package de.firemage.autograder.core.check.api;

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

public class StringIsEmptyReimplementationCheck extends IntegratedCheck {
    private static final String DESCRIPTION = "Use String#isEmpty instead of '.equals(\"\")' or '.length() == 0' (or the negation when checking if the String is not empty)";

    public StringIsEmptyReimplementationCheck() {
        super(DESCRIPTION);
    }

    private static String formatExplanation(CtElement element) {
        return String.format("Use 'isEmpty()' instead of '%s' to check for emptiness", element.toString());
    }

    private static String formatNegatedExplanation(CtElement element) {
        return String.format("Use '!isEmpty()' instead of '%s' to check for non-emptiness", element.toString());
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
                        addLocalProblem(invocation, formatExplanation(invocation));
                    } else if (lengthZeroCheck(invocation)) {
                        addLocalProblem(invocation.getParent(), formatExplanation(invocation.getParent()));
                    } else if (lengthGreaterThanZeroCheck(invocation)) {
                        addLocalProblem(invocation.getParent(), formatNegatedExplanation(invocation.getParent()));
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
            return switch(operator.getKind()) {
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
