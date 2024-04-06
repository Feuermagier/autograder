package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_NEGATION })
public class RedundantNegationCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtUnaryOperator<?>>() {
            @Override
            public void process(CtUnaryOperator<?> ctUnaryOperator) {
                if (ctUnaryOperator.isImplicit() || !ctUnaryOperator.getPosition().isValidPosition()) {
                    return;
                }

                // only check negations !(operand)
                if (ctUnaryOperator.getKind() != UnaryOperatorKind.NOT) {
                    return;
                }

                CtExpression<?> operand = ctUnaryOperator.getOperand();

                // this negates the operand and optimizes it if possible
                CtExpression<?> negated = SpoonUtil.negate(operand);
                // if they are equal, the negation is not redundant
                if (ctUnaryOperator.equals(negated)) {
                    return;
                }

                // TODO: do the same as in RepeatedMathOperationCheck

                addLocalProblem(
                    ctUnaryOperator,
                    new LocalizedMessage(
                        "common-reimplementation",
                        Map.of("suggestion", negated.prettyprint())
                    ),
                    ProblemType.REDUNDANT_NEGATION
                );
            }
        });
    }
}
