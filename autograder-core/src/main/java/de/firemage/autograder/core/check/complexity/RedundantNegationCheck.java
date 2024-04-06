package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import de.firemage.autograder.core.integrated.evaluator.fold.Fold;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_NEGATION })
public class RedundantNegationCheck extends IntegratedCheck {
    private record NegationFolder() implements Fold {
        @Override
        @SuppressWarnings("unchecked")
        public <T> CtExpression<T> foldCtUnaryOperator(CtUnaryOperator<T> ctUnaryOperator) {
            // only check negations !(operand)
            if (ctUnaryOperator.getKind() != UnaryOperatorKind.NOT) {
                return ctUnaryOperator;
            }

            CtExpression<?> operand = ctUnaryOperator.getOperand();

            // this negates the operand and optimizes it if possible
            return (CtExpression<T>) SpoonUtil.negate(operand);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtExpression<?>>() {
            @Override
            public void process(CtExpression<?> ctExpression) {
                if (ctExpression.isImplicit()
                    || !ctExpression.getPosition().isValidPosition()
                    || ctExpression.getParent(CtExpression.class) != null) {
                    return;
                }

                // this negates the operand and optimizes it if possible
                CtExpression<?> negated = new Evaluator(new NegationFolder()).evaluate(ctExpression);
                // if they are equal, the negation is not redundant
                if (ctExpression.equals(negated)) {
                    return;
                }

                addLocalProblem(
                    ctExpression,
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
