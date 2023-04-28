package de.firemage.autograder.core.check.general;

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
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtTypeReference;

import java.util.EnumSet;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.COMPARE_CHAR_VALUE })
public class CompareCharValue extends IntegratedCheck {
    private static final int MAX_ASCII_VALUE = 127;
    private static final Set<BinaryOperatorKind> COMPARISON_OPERATORS = EnumSet.of(
        BinaryOperatorKind.EQ,
        BinaryOperatorKind.NE,
        BinaryOperatorKind.GE,
        BinaryOperatorKind.GT,
        BinaryOperatorKind.LE,
        BinaryOperatorKind.LT
    );

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<?>>() {
            @Override
            public void process(CtBinaryOperator<?> ctBinaryOperator) {
                if (!COMPARISON_OPERATORS.contains(ctBinaryOperator.getKind())) return;

                CtExpression<?> lhs = SpoonUtil.resolveCtExpression(ctBinaryOperator.getLeftHandOperand());
                CtExpression<?> rhs = SpoonUtil.resolveCtExpression(ctBinaryOperator.getRightHandOperand());

                CtTypeReference<?> charType = lhs.getFactory().Type().createReference(char.class);

                if (lhs.getType().equals(charType) && rhs instanceof CtLiteral<?> ctLiteral && ctLiteral.getValue() instanceof Integer intValue) {
                    if (intValue > MAX_ASCII_VALUE) return;
                } else if (lhs instanceof CtLiteral<?> ctLiteral && ctLiteral.getValue() instanceof Integer intValue && rhs.getType().equals(charType)) {
                    if (intValue > MAX_ASCII_VALUE) return;
                } else {
                    return;
                }

                addLocalProblem(
                    ctBinaryOperator,
                    new LocalizedMessage("compare-char-value"),
                    ProblemType.COMPARE_CHAR_VALUE
                );
            }
        });
    }
}
