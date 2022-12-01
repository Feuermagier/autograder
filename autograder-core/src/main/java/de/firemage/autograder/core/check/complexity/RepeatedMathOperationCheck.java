package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtVariableReference;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepeatedMathOperationCheck extends IntegratedCheck {
    private static final Map<BinaryOperatorKind, Integer> OCCURRENCE_THRESHOLDS =
        Map.of(BinaryOperatorKind.PLUS, 2, BinaryOperatorKind.MUL, 3);

    public RepeatedMathOperationCheck() {
        super(new LocalizedMessage("repeated-math-operation"));
    }

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

                var variable = occurrences.entrySet().stream()
                    .filter(e -> e.getValue() >= OCCURRENCE_THRESHOLDS.get(operator.getKind()))
                    .max(Comparator.comparingInt(Map.Entry::getValue));

                variable.ifPresent(ctVariableReferenceIntegerEntry -> addLocalProblem(operator,
                    new LocalizedMessage("repeated-math-operation-" + operator.getKind().toString().toLowerCase(),
                        Map.of("var", ctVariableReferenceIntegerEntry.getKey().getSimpleName(), "count",
                            ctVariableReferenceIntegerEntry.getValue())),
                    ProblemType.REPEATED_MATH_OPERATION));
            }
        });
    }

    private Map<CtVariableReference<?>, Integer> countOccurrences(CtExpression<?> expression, BinaryOperatorKind kind) {
        if (expression instanceof CtVariableRead<?> read) {
            return Map.of(read.getVariable(), 1);
        } else if (expression instanceof CtBinaryOperator<?> operator && operator.getKind() == kind) {
            return mergeOccurrenceMaps(countOccurrences(operator.getLeftHandOperand(), kind),
                countOccurrences(operator.getRightHandOperand(), kind));
        } else {
            return Map.of();
        }
    }

    private Map<CtVariableReference<?>, Integer> mergeOccurrenceMaps(Map<CtVariableReference<?>, Integer> map1,
                                                                     Map<CtVariableReference<?>, Integer> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
    }
}
