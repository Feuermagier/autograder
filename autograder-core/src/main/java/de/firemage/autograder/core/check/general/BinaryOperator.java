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
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.BINARY_OPERATOR_ON_BOOLEAN })
public class BinaryOperator extends IntegratedCheck {
    private static final Set<BinaryOperatorKind> BINARY_OPERATORS = Set.of(
        BinaryOperatorKind.BITAND,
        BinaryOperatorKind.BITOR
    );

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<Boolean>>() {
            @Override
            public void process(CtBinaryOperator<Boolean> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit()
                    || !ctBinaryOperator.getPosition().isValidPosition()
                    || ctBinaryOperator.getParent(CtBinaryOperator.class) != null) {
                    return;
                }

                boolean hasBinaryOperator = BINARY_OPERATORS.contains(ctBinaryOperator.getKind())
                    && SpoonUtil.isTypeEqualTo(ctBinaryOperator.getType(), boolean.class, Boolean.class);

                if (!hasBinaryOperator) {
                    hasBinaryOperator = ctBinaryOperator.getElements(new TypeFilter<>(CtBinaryOperator.class)).stream()
                        .anyMatch(operator -> SpoonUtil.isTypeEqualTo(operator.getType(), boolean.class, Boolean.class)
                            && BINARY_OPERATORS.contains(operator.getKind()));
                }

                if (hasBinaryOperator) {
                    addLocalProblem(
                        ctBinaryOperator,
                        new LocalizedMessage("binary-operator-on-boolean"),
                        ProblemType.BINARY_OPERATOR_ON_BOOLEAN
                    );
                }
            }
        });
    }
}
