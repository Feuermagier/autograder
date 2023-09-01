package de.firemage.autograder.core.check.complexity;

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
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtVariableReference;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {ProblemType.REPEATED_MATH_OPERATION})
public class RepeatedMathOperationCheck extends IntegratedCheck {
    private static final Map<BinaryOperatorKind, Integer> OCCURRENCE_THRESHOLDS =
        Map.of(BinaryOperatorKind.PLUS, 2, BinaryOperatorKind.MUL, 3);

    private record Variable(CtVariableReference<?> ctVariableReference, CtExpression<?> target) {}

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<?>>() {
            @Override
            public void process(CtBinaryOperator<?> operator) {
                if (!OCCURRENCE_THRESHOLDS.containsKey(operator.getKind())) {
                    return;
                }

                // Only look at the top statement
                if (operator.getParent() instanceof CtBinaryOperator<?> parent &&
                    parent.getKind() == operator.getKind()) {
                    return;
                }

                var occurrences = countOccurrences(operator, operator.getKind());

                var optionalVariable = occurrences.entrySet().stream()
                    .filter(e -> e.getValue() >= OCCURRENCE_THRESHOLDS.get(operator.getKind()))
                    .max(Comparator.comparingInt(Map.Entry::getValue));

                optionalVariable.ifPresent(ctVariableReferenceIntegerEntry -> {
                    Variable variable = ctVariableReferenceIntegerEntry.getKey();
                    String variableName = "%s".formatted(variable.ctVariableReference().getSimpleName());
                    if (variable.target() != null) {
                        variableName = "%s.%s".formatted(
                            variable.target().prettyprint(),
                            variable.ctVariableReference().getSimpleName()
                        );
                    }

                    int count = ctVariableReferenceIntegerEntry.getValue();
                    String suggestion = "%s * %d".formatted(variableName, count);
                    if (operator.getKind() == BinaryOperatorKind.MUL) {
                        suggestion = "Math.pow(%s, %d)".formatted(variableName, count);
                    }

                    addLocalProblem(
                        operator,
                        new LocalizedMessage(
                            "common-reimplementation",
                            Map.of("suggestion", suggestion)
                        ),
                        ProblemType.REPEATED_MATH_OPERATION
                    );
                });
            }
        });
    }

    private Map<Variable, Integer> countOccurrences(CtExpression<?> expression, BinaryOperatorKind kind) {
        if (expression instanceof CtFieldRead<?> read) {
            return Map.of(new Variable(read.getVariable(), read.getTarget()), 1);
        } else if (expression instanceof CtVariableRead<?> read) {
            return Map.of(new Variable(read.getVariable(), null), 1);
        }

        if (expression instanceof CtBinaryOperator<?> operator && operator.getKind() == kind) {
            // '+' can also be used on Strings, but String operations are not associative
            if (SpoonUtil.isString(operator.getLeftHandOperand().getType()) ||
                SpoonUtil.isString(operator.getRightHandOperand().getType())) {
                return Map.of();
            }

            return mergeMaps(
                countOccurrences(operator.getLeftHandOperand(), kind),
                countOccurrences(operator.getRightHandOperand(), kind)
            );
        }

        return Map.of();
    }

    private <K> Map<K, Integer> mergeMaps(Map<? extends K, Integer> left, Map<? extends K, Integer> right) {
        return Stream.concat(
            left.entrySet().stream(),
            right.entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
    }
}
