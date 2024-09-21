package de.firemage.autograder.core.check.api;


import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.IS_LETTER_OR_DIGIT })
public class IsLetterOrDigit extends IntegratedCheck {
    private static final Set<BinaryOperatorKind> SUPPORTED_OPERATORS = Set.of(
        BinaryOperatorKind.OR,
        BinaryOperatorKind.AND
    );

    private static boolean isLetterInvocation(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() != null
            && ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && TypeUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), Character.class)
            && MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), boolean.class, "isLetter", char.class);
    }

    private static boolean isDigitInvocation(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() != null
            && ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && TypeUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), Character.class)
            && MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), boolean.class, "isDigit", char.class);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<Boolean>>() {
            @Override
            public void process(CtBinaryOperator<Boolean> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit()
                    || !ctBinaryOperator.getPosition().isValidPosition()
                    || !SUPPORTED_OPERATORS.contains(ctBinaryOperator.getKind())) {
                    return;
                }

                CtInvocation<?> leftInvocation;
                CtInvocation<?> rightInvocation;
                boolean isNegated = false;

                // check for !left && !right
                if (ctBinaryOperator.getKind() == BinaryOperatorKind.AND
                    && ctBinaryOperator.getLeftHandOperand() instanceof CtUnaryOperator<?> leftUnaryOperator
                    && ctBinaryOperator.getRightHandOperand() instanceof CtUnaryOperator<?> rightUnaryOperator
                    && leftUnaryOperator.getKind() == UnaryOperatorKind.NOT
                    && rightUnaryOperator.getKind() == UnaryOperatorKind.NOT
                    && leftUnaryOperator.getOperand() instanceof CtInvocation<?> left
                    && rightUnaryOperator.getOperand() instanceof CtInvocation<?> right) {
                    leftInvocation = left;
                    rightInvocation = right;
                    isNegated = true;
                } else if (ctBinaryOperator.getKind() == BinaryOperatorKind.AND) {
                    return;
                } else if (ctBinaryOperator.getLeftHandOperand() instanceof CtInvocation<?> left
                    && ctBinaryOperator.getRightHandOperand() instanceof CtInvocation<?> right) {
                    leftInvocation = left;
                    rightInvocation = right;
                } else {
                    return;
                }

                if ((!(isLetterInvocation(leftInvocation) && isDigitInvocation(rightInvocation))
                    && !(isDigitInvocation(leftInvocation) && isLetterInvocation(rightInvocation)))
                    || !leftInvocation.getArguments().equals(rightInvocation.getArguments())) {
                    return;
                }

                String suggestion = "Character.isLetterOrDigit(%s)".formatted(leftInvocation.getArguments()
                    .stream()
                    .map(CtElement::toString)
                    .collect(Collectors.joining(", ")));

                if (isNegated) {
                    suggestion = "!" + suggestion;
                }

                addLocalProblem(
                    ctBinaryOperator,
                    new LocalizedMessage(
                        "common-reimplementation",
                        Map.of(
                            "suggestion", suggestion
                        )
                    ),
                    ProblemType.IS_LETTER_OR_DIGIT
                );
            }
        });
    }
}
