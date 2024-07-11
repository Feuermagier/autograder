package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import de.firemage.autograder.core.integrated.evaluator.fold.Fold;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {ProblemType.REPEATED_MATH_OPERATION})
public class RepeatedMathOperationCheck extends IntegratedCheck {
    private static final Map<BinaryOperatorKind, Integer> OCCURRENCE_THRESHOLDS =
        Map.of(BinaryOperatorKind.PLUS, 2, BinaryOperatorKind.MUL, 3);

    private record Variable(CtVariableReference<?> ctVariableReference, CtExpression<?> target) {
    }


    private static List<CtExpression<?>> splitOperator(CtBinaryOperator<?> ctBinaryOperator, BinaryOperatorKind kind) {
        List<CtExpression<?>> result = new ArrayList<>();

        if (ctBinaryOperator.getKind() != kind) {
            return new ArrayList<>(List.of(ctBinaryOperator));
        }

        CtExpression<?> left = ctBinaryOperator.getLeftHandOperand();
        CtExpression<?> right = ctBinaryOperator.getRightHandOperand();

        // The right hand side can also be a binary operator, e.g. a + (b + c)
        if (right instanceof CtBinaryOperator<?> rightOperator) {
            List<CtExpression<?>> rightOperands = splitOperator(rightOperator, kind);
            // the right operands have to be added in reverse order
            Collections.reverse(rightOperands);
            result.addAll(rightOperands);
        } else {
            result.add(right);
        }

        while (left instanceof CtBinaryOperator<?> lhs && lhs.getKind() == kind) {
            result.add(lhs.getRightHandOperand());
            left = lhs.getLeftHandOperand();
        }

        result.add(left);

        Collections.reverse(result);

        return result;
    }

    /**
     * This class optimizes repeated operations like `a + a + a + a` to `a * 4`.
     */
    private record OperatorFolder(BinaryOperatorKind kind, int threshold, BiFunction<CtExpression<?>, Integer, CtExpression<?>> function) implements Fold {
        @Override
        public CtElement enter(CtElement ctElement) {
            return this.fold(ctElement);
        }

        @Override
        public CtElement exit(CtElement ctElement) {
            return ctElement;
        }


        @Override
        @SuppressWarnings("unchecked")
        public <T> CtExpression<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
            // skip if the operator is not supported
            if (!OCCURRENCE_THRESHOLDS.containsKey(ctBinaryOperator.getKind()) ||
                !SpoonUtil.isPrimitiveNumeric(ctBinaryOperator.getType())) {
                return ctBinaryOperator;
            }

            List<CtExpression<?>> operands = splitOperator(ctBinaryOperator, this.kind);

            Map<CtExpression<?>, Integer> occurrences = operands.stream()
                .collect(Collectors.toMap(o -> o, o -> 1, Integer::sum, LinkedHashMap::new));

            // reconstruct the binary operator (note: this will destroy the original parentheses)
            return (CtExpression<T>) occurrences.entrySet()
                .stream()
                .map(entry -> {
                    var expression = entry.getKey();
                    int count = entry.getValue();

                    if (count < this.threshold) {
                        return repeatExpression(this.kind, expression, count);
                    } else {
                        return this.function.apply(expression, count);
                    }
                })
                .reduce((left, right) -> SpoonUtil.createBinaryOperator(left, right, this.kind))
                .orElseThrow();
        }
    }


    public static CtExpression<?> repeatExpression(BinaryOperatorKind kind, CtExpression<?> expression, int count) {
        CtExpression<?>[] array = new CtExpression<?>[count - 1];
        Arrays.fill(array, expression);
        return joinExpressions(kind, expression, array);
    }

    public static CtExpression<?> joinExpressions(BinaryOperatorKind kind, CtExpression<?> first, CtExpression<?>... others) {
        return Arrays.stream(others).reduce(first, (left, right) -> SpoonUtil.createBinaryOperator(left, right, kind));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtExpression<?>>() {
            @Override
            public void process(CtExpression<?> ctExpression) {
                if (ctExpression.isImplicit()
                    || !ctExpression.getPosition().isValidPosition()
                    // we only want to look at top level expressions:
                    || ctExpression.getParent(CtExpression.class) != null) {
                    return;
                }

                AtomicInteger plusOptimizations = new AtomicInteger();
                AtomicInteger mulOptimizations = new AtomicInteger();

                Fold plusFolder = new OperatorFolder(
                    BinaryOperatorKind.PLUS,
                    OCCURRENCE_THRESHOLDS.get(BinaryOperatorKind.PLUS),
                    (expression, count) -> {
                        plusOptimizations.addAndGet(1);
                        return SpoonUtil.createBinaryOperator(
                            expression,
                            SpoonUtil.makeLiteralNumber(expression.getType(), count),
                            BinaryOperatorKind.MUL
                        );
                    }
                );

                Fold mulFolder = new OperatorFolder(
                    BinaryOperatorKind.MUL,
                    OCCURRENCE_THRESHOLDS.get(BinaryOperatorKind.MUL),
                    (expression, count) -> {
                        TypeFactory typeFactory = expression.getFactory().Type();
                        mulOptimizations.addAndGet(1);
                        return SpoonUtil.createStaticInvocation(
                            typeFactory.get(java.lang.Math.class).getReference(),
                            "pow",
                            expression,
                            SpoonUtil.makeLiteralNumber(typeFactory.integerPrimitiveType(), count)
                        );
                    }
                );

                CtExpression<?> suggestion = new Evaluator(plusFolder).evaluate(ctExpression);
                suggestion = new Evaluator(mulFolder).evaluate(suggestion);

                if (plusOptimizations.get() > 0 || mulOptimizations.get() > 0) {
                    addLocalProblem(
                        ctExpression,
                        new LocalizedMessage(
                            "common-reimplementation",
                            Map.of("suggestion", suggestion)
                        ),
                        ProblemType.REPEATED_MATH_OPERATION
                    );
                }
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
