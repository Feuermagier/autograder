package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;

import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_BOOLEAN_EQUAL })
public class RedundantBooleanEqual extends IntegratedCheck {
    private static final Set<BinaryOperatorKind> OPERATORS = Set.of(
        BinaryOperatorKind.EQ,
        BinaryOperatorKind.NE
    );

    private void reportProblem(CtBinaryOperator<?> ctBinaryOperator, boolean literal, CtExpression<?> otherSide) {
        boolean realLiteral = literal;
        if (ctBinaryOperator.getKind() == BinaryOperatorKind.NE) {
            realLiteral = !literal;
        }

        CtExpression<?> suggestion = otherSide;
        if (!realLiteral) {
            suggestion = SpoonUtil.negate(otherSide);
        }

        addLocalProblem(
            ctBinaryOperator,
            new LocalizedMessage(
                "redundant-boolean-equal",
                Map.of(
                    "suggestion", suggestion
                )
            ),
            ProblemType.REDUNDANT_BOOLEAN_EQUAL
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<Boolean>>() {

            @Override
            public void process(CtBinaryOperator<Boolean> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit()
                    || !ctBinaryOperator.getPosition().isValidPosition()
                    || !OPERATORS.contains(ctBinaryOperator.getKind())
                    || !SpoonUtil.isBoolean(ctBinaryOperator.getLeftHandOperand())
                    || !SpoonUtil.isBoolean(ctBinaryOperator.getRightHandOperand())) {
                    return;
                }

                CtExpression<?> left = ctBinaryOperator.getLeftHandOperand();
                CtExpression<?> right = ctBinaryOperator.getRightHandOperand();

                // the lhs resolves to a literal boolean
                SpoonUtil.tryGetBooleanLiteral(left)
                    .ifPresentOrElse(
                        literal -> reportProblem(ctBinaryOperator, literal, right),
                        // if the lhs is not a literal boolean, check if the rhs is
                        () -> SpoonUtil.tryGetBooleanLiteral(right)
                            .ifPresent(literal -> reportProblem(ctBinaryOperator, literal, left))
                    );
            }
        });
    }
}
