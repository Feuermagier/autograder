package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExecutableCheck(reportedProblems = {ProblemType.COMPARE_CHAR_VALUE})
public class CompareCharValue extends IntegratedCheck {
    private static final int MIN_ASCII_VALUE = 1;
    private static final int MAX_ASCII_VALUE = 127;

    private static final Set<BinaryOperatorKind> COMPARISON_OPERATORS = EnumSet.of(
        BinaryOperatorKind.EQ,
        BinaryOperatorKind.NE,
        BinaryOperatorKind.GE,
        BinaryOperatorKind.GT,
        BinaryOperatorKind.LE,
        BinaryOperatorKind.LT
    );

    private static Optional<Integer> getComparedIntegerValue(CtExpression<?> left, CtExpression<?> right) {
        if (!TypeUtil.isTypeEqualTo(left.getType(), char.class)
            || !(SpoonUtil.resolveConstant(right) instanceof CtLiteral<?> literal && literal.getValue() instanceof Integer value)) {
            return Optional.empty();
        }

        if (value < MIN_ASCII_VALUE || value > MAX_ASCII_VALUE) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<?>>() {
            @Override
            public void process(CtBinaryOperator<?> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit()
                    || !ctBinaryOperator.getPosition().isValidPosition()
                    || !COMPARISON_OPERATORS.contains(ctBinaryOperator.getKind())) {
                    return;
                }

                CtExpression<?> left = ctBinaryOperator.getLeftHandOperand();
                CtExpression<?> right = ctBinaryOperator.getRightHandOperand();

                CtExpression<?> charExpression = left;
                Optional<Integer> number = getComparedIntegerValue(left, right);

                if (number.isEmpty()) {
                    charExpression = right;
                    number = getComparedIntegerValue(right, left);
                }

                if (number.isEmpty()) {
                    return;
                }

                int asciiValue = number.get();

                addLocalProblem(
                    ctBinaryOperator,
                    new LocalizedMessage(
                        "compare-char-value",
                        Map.of(
                            "expression", charExpression,
                            "intValue", asciiValue,
                            "charValue", "" + (char) asciiValue
                        )
                    ),
                    ProblemType.COMPARE_CHAR_VALUE
                );
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(3);
    }
}
