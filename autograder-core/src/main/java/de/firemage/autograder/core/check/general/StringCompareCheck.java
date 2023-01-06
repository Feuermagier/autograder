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

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.STRING_COMPARE_BY_REFERENCE})
public class StringCompareCheck extends IntegratedCheck {
    public StringCompareCheck() {
        super(new LocalizedMessage("string-cmp-desc"));
    }
    
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<?>>() {
            @Override
            public void process(CtBinaryOperator<?> operator) {
                if (operator.getKind() != BinaryOperatorKind.EQ) {
                    return;
                }

                if (SpoonUtil.isString(operator.getLeftHandOperand().getType())) {
                    addLocalProblem(operator, new LocalizedMessage("string-cmp-exp", Map.of("lhs", operator.getLeftHandOperand(), "rhs", operator.getRightHandOperand())),
                        ProblemType.STRING_COMPARE_BY_REFERENCE);
                }
            }
        });
    }
}
