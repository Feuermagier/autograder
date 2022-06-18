package de.firemage.codelinter.core.check.api;

import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.integrated.SpoonUtil;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtExecutableReference;

public class StringIsEmptyReimplementationCheck extends IntegratedCheck {
    private static final String DESCRIPTION = "Use String#isEmpty instead of '.equals(\"\")' or '.length() == 0'";

    public StringIsEmptyReimplementationCheck() {
        super(DESCRIPTION);
    }

    private static String formatExplanation(CtInvocation<?> invocation) {
        return String.format("Use 'isEmpty()' instead of '%s'", invocation.toString());
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
                    CtExecutableReference<?> executable = invocation.getExecutable();
                    if (executable.getSignature().equals("equals(java.lang.Object)") &&
                        SpoonUtil.isStringLiteral(invocation.getArguments().get(0), "")) {
                        addLocalProblem(invocation, formatExplanation(invocation));
                    } else if (executable.getSignature().equals(".length()") &&
                        executable.getParent() instanceof CtBinaryOperator<?> operator && operator.getKind().equals(
                        BinaryOperatorKind.EQ) && (SpoonUtil.isIntegerLiteral(operator.getLeftHandOperand(), 0) ||
                        SpoonUtil.isIntegerLiteral(operator.getRightHandOperand(), 0))) {
                        addLocalProblem(invocation, formatExplanation(invocation));
                    }
                }
            }
        });
    }
}
