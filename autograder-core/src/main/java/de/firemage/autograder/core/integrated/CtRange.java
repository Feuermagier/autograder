package de.firemage.autograder.core.integrated;

import org.apache.commons.lang3.Range;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;

import java.util.Optional;
import java.util.function.Predicate;

public record CtRange<T extends Comparable<T>>(
    CtExpression<T> ctExpression,
    BinaryOperatorKind operator,
    CtLiteral<T> ctLiteral) {
    public static Optional<CtRange<Character>> ofCharRange(CtBinaryOperator<Boolean> ctBinaryOperator) {
        return of(ctBinaryOperator, Character.class, char.class);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> Optional<CtRange<T>> of(CtBinaryOperator<Boolean> ctBinaryOperator, Class<?>... expectedTypes) {
        Predicate<? super CtExpression<?>> isLiteral = expr -> expr instanceof CtLiteral<?> ctLiteral
            && SpoonUtil.isTypeEqualTo(ctLiteral.getType(), expectedTypes);

        // swap operator if necessary, so that the literal is on the right side:
        // <expr> <op> <literal>
        CtBinaryOperator<Boolean> result = SpoonUtil.normalizeBy(
            (left, right) -> isLiteral.test(left) && !isLiteral.test(right),
            ctBinaryOperator
        );

        // check if neither side is a literal
        if (!isLiteral.test(result.getRightHandOperand())) {
            return Optional.empty();
        }

        return Optional.of(new CtRange<>(
            (CtExpression<T>) result.getLeftHandOperand(),
            result.getKind(),
            (CtLiteral<T>) result.getRightHandOperand()
        ));
    }

    public Range<T> toRange() {
        // <expr> <op> <literal>
        if (this.operator == BinaryOperatorKind.LE) {
            T lowerBound = SpoonUtil.minimumValue(this.ctLiteral).getValue();

            if (SpoonUtil.resolveCtExpression(this.ctExpression) instanceof CtLiteral<T> exprLiteral) {
                lowerBound = exprLiteral.getValue();
            }

            return Range.between(lowerBound, this.ctLiteral.getValue());
        } else if (this.operator == BinaryOperatorKind.GE) {
            // <expr> >= <literal>
            T upperBound = SpoonUtil.maximumValue(this.ctLiteral).getValue();
            if (SpoonUtil.resolveCtExpression(this.ctExpression) instanceof CtLiteral<T> exprLiteral) {
                upperBound = exprLiteral.getValue();
            }

            return Range.between(this.ctLiteral.getValue(), upperBound);
        } else {
            throw new IllegalStateException("Unsupported operator: " + this.operator);
        }
    }
}
