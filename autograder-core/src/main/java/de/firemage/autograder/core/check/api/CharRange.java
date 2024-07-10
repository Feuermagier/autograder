package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.CtRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import org.apache.commons.lang3.Range;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.CHAR_RANGE })
public class CharRange extends IntegratedCheck {
    private static final Set<BinaryOperatorKind> RANGE_OPERATORS = Set.of(
        BinaryOperatorKind.LT,
        BinaryOperatorKind.LE,
        BinaryOperatorKind.GT,
        BinaryOperatorKind.GE
    );

    @FunctionalInterface
    private interface Suggester<T, R> {
        CtExpression<R> suggest(Factory factory, CtExpression<T> ctExpression, CtTypeReference<T> targetType);

        default CtExpression<R> suggest(CtExpression<T> ctExpression) {
            return this.suggest(ctExpression.getFactory(), ctExpression, ctExpression.getType());
        }
    }

    private static final Map<Range<Character>, Suggester<Character, Boolean>> MAPPING = Map.of(
        Range.of('a', 'z'), (factory, ctExpression, targetType) -> factory.createBinaryOperator(
            SpoonUtil.createStaticInvocation(
                targetType,
                "isAlphabetic",
                SpoonUtil.castExpression(int.class, ctExpression)
            ),
            SpoonUtil.createStaticInvocation(
                targetType,
                "isLowerCase",
                SpoonUtil.castExpression(char.class, ctExpression)
            ),
            BinaryOperatorKind.AND
        ),
        Range.of('A', 'Z'), (factory, ctExpression, targetType) -> factory.createBinaryOperator(
            SpoonUtil.createStaticInvocation(
                targetType,
                "isAlphabetic",
                SpoonUtil.castExpression(int.class, ctExpression)
            ),
            SpoonUtil.createStaticInvocation(
                targetType,
                "isUpperCase",
                SpoonUtil.castExpression(char.class, ctExpression)
            ),
            BinaryOperatorKind.AND
        ),
        Range.of('0', '9'), (factory, ctExpression, targetType) -> SpoonUtil.createStaticInvocation(
            targetType,
            "isDigit",
            SpoonUtil.castExpression(char.class, ctExpression)
        )
    );

    private static Optional<CtExpression<Boolean>> makeSuggestion(CtExpression<Character> ctExpression, Range<Character> range) {
        return Optional.ofNullable(MAPPING.get(range)).map(fn -> fn.suggest(
            ctExpression.getFactory(),
            ctExpression,
            ctExpression.getFactory().Type().characterType()
        ));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<Boolean>>() {
            @Override
            @SuppressWarnings("unchecked")
            public void process(CtBinaryOperator<Boolean> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit()
                    || !ctBinaryOperator.getPosition().isValidPosition()
                    || !TypeUtil.isTypeEqualTo(ctBinaryOperator.getType(), java.lang.Boolean.class, boolean.class)) {
                    return;
                }

                boolean isNegated;
                CtBinaryOperator<Boolean> operator = ctBinaryOperator;
                if (ctBinaryOperator.getKind() == BinaryOperatorKind.OR) {
                    isNegated = true;
                    operator = (CtBinaryOperator<Boolean>) SpoonUtil.negate(ctBinaryOperator);
                } else {
                    isNegated = false;
                    if (ctBinaryOperator.getKind() != BinaryOperatorKind.AND) {
                        return;
                    }
                }

                if (!(operator.getLeftHandOperand() instanceof CtBinaryOperator<?> left)
                    || !(operator.getRightHandOperand() instanceof CtBinaryOperator<?> right)
                    || !RANGE_OPERATORS.contains(left.getKind())
                    || !RANGE_OPERATORS.contains(right.getKind())
                ) {
                    return;
                }

                // structure must be one of:
                // - (<expr> <op> <literal>) && (<expr> <op> <literal>)
                // - (<literal> <op> <expr>) && (<literal> <op> <expr>)
                // or swapped

                CtRange<Character> leftRange = CtRange.ofCharRange((CtBinaryOperator<Boolean>) left).orElse(null);
                CtRange<Character> rightRange = CtRange.ofCharRange((CtBinaryOperator<Boolean>) right).orElse(null);

                if (leftRange == null || rightRange == null) {
                    return;
                }

                // skip them if they do not check the same variable
                if (!leftRange.ctExpression().equals(rightRange.ctExpression())) {
                    return;
                }

                Range<Character> intersection = leftRange.toRange().intersectionWith(rightRange.toRange());

                makeSuggestion(leftRange.ctExpression(), intersection).ifPresent(suggestion -> {
                    if (isNegated) {
                        suggestion = SpoonUtil.negate(suggestion);
                    }

                    addLocalProblem(
                        ctBinaryOperator,
                        new LocalizedMessage(
                            "common-reimplementation",
                            Map.of("suggestion", suggestion)
                        ),
                        ProblemType.CHAR_RANGE
                    );
                });
            }
        });
    }
}
