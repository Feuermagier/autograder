package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_NEGATION})
public class RedundantNegationCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtUnaryOperator<?>>() {
            @Override
            public void process(CtUnaryOperator<?> op) {
                if (op.getKind() != UnaryOperatorKind.NOT) {
                    return;
                }

                if (op.getOperand() instanceof CtBinaryOperator<?> inner && inner.getKind() == BinaryOperatorKind.EQ) {
                    var fixed = staticAnalysis.getFactory().createBinaryOperator(
                        inner.getLeftHandOperand(),
                        inner.getRightHandOperand(),
                        BinaryOperatorKind.NE);

                    addLocalProblem(op,
                        new LocalizedMessage("redundant-neg-exp", Map.of("original", op.toString(), "fixed", fixed)),
                        ProblemType.REDUNDANT_NEGATION);
                }
            }
        });
    }
}
