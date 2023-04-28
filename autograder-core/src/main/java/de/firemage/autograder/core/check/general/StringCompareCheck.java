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

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.STRING_COMPARE_BY_REFERENCE })
public class StringCompareCheck extends IntegratedCheck {
    private static boolean isStringComparison(CtExpression<?> lhs, CtExpression<?> rhs) {
        return SpoonUtil.isString(lhs.getType()) && !SpoonUtil.isNullLiteral(rhs)
               || SpoonUtil.isString(rhs.getType()) && !SpoonUtil.isNullLiteral(lhs);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<?>>() {
            @Override
            public void process(CtBinaryOperator<?> operator) {
                if (operator.getKind() != BinaryOperatorKind.EQ && operator.getKind() != BinaryOperatorKind.NE) {
                    return;
                }

                CtExpression<?> lhs = operator.getLeftHandOperand();
                CtExpression<?> rhs = operator.getRightHandOperand();

                if (isStringComparison(lhs, rhs)) {
                    addLocalProblem(operator, new LocalizedMessage(
                            "string-cmp-exp",
                            Map.of("lhs", lhs, "rhs", rhs)
                        ),
                        ProblemType.STRING_COMPARE_BY_REFERENCE
                    );
                }
            }
        });
    }
}
