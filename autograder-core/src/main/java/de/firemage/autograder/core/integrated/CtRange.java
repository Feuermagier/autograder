package de.firemage.autograder.core.integrated;

import org.apache.commons.lang3.Range;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;

import java.util.Optional;
import java.util.function.IntUnaryOperator;

public record CtRange<T extends Comparable<T>>(
    CtExpression<T> ctExpression,
    BinaryOperatorKind operator,
    CtLiteral<T> ctLiteral) {

    private static BinaryOperatorKind swapOperator(BinaryOperatorKind operator) {
        return switch (operator) {
            // a < b => b > a
            case LT -> BinaryOperatorKind.GT;
            // a <= b => b >= a
            case LE -> BinaryOperatorKind.GE;
            // a >= b => b <= a
            case GE -> BinaryOperatorKind.LE;
            // a > b => b < a
            case GT -> BinaryOperatorKind.LT;
            default -> operator;
        };
    }

    public static Optional<CtRange<Character>> of(CtBinaryOperator<Boolean> ctBinaryOperator) {
        return of(ctBinaryOperator, Character.class, char.class);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> Optional<CtRange<T>> of(CtBinaryOperator<Boolean> ctBinaryOperator, Class<?>... expectedTypes) {
        // <expr> <op> <literal>
        CtExpression<T> expression;
        BinaryOperatorKind operator;
        CtLiteral<T> literal;

        // <expr> <op> <literal>
        if (SpoonUtil.resolveCtExpression(ctBinaryOperator.getRightHandOperand()) instanceof CtLiteral<?> ctLiteral
            && SpoonUtil.isTypeEqualTo(ctLiteral.getType(), expectedTypes)) {
            literal = (CtLiteral<T>) ctLiteral;
            operator = ctBinaryOperator.getKind();
            expression = (CtExpression<T>) ctBinaryOperator.getLeftHandOperand();
            // <literal> <op> <expr>
        } else if (SpoonUtil.resolveCtExpression(ctBinaryOperator.getLeftHandOperand()) instanceof CtLiteral<?> ctLiteral
            && SpoonUtil.isTypeEqualTo(ctLiteral.getType(), expectedTypes)) {
            literal = (CtLiteral<T>) ctLiteral;
            expression = (CtExpression<T>) ctBinaryOperator.getRightHandOperand();
            // must swap literal and expression, to do that, the operator must be inverted:
            // a <= b => b >= a
            // a < b => b > a
            // a >= b => b <= a
            // a > b => b < a
            operator = swapOperator(ctBinaryOperator.getKind());
        } else {
            return Optional.empty();
        }

        // adjust the literal if the operator is < or >
        if (operator == BinaryOperatorKind.LT) {
            // <expr> < <literal> => <expr> <= <literal> - 1
            literal = update(literal, i -> i - 1);
            operator = BinaryOperatorKind.LE;
        } else if (ctBinaryOperator.getKind() == BinaryOperatorKind.GT) {
            // <expr> > <literal> => <expr> >= <literal> + 1
            literal = update(literal, i -> i + 1);
            operator = BinaryOperatorKind.GE;
        }

        return Optional.of(new CtRange<>(expression, operator, literal));
    }

    private static <T> CtLiteral<T> update(CtLiteral<T> ctLiteral, IntUnaryOperator function) {
        CtLiteral result = ctLiteral.clone();

        if (ctLiteral.getValue() instanceof Character character) {
            result.setValue((char) function.applyAsInt(character));
        } else if (ctLiteral.getValue() instanceof Integer integer) {
            result.setValue(function.applyAsInt(integer));
        } else {
            throw new IllegalStateException("Unsupported literal type: " + ctLiteral.getValue().getClass().getSimpleName());
        }

        return result;
    }

    private static <T> CtLiteral<T> minValue(CtLiteral<T> ctLiteral) {
        CtLiteral result = ctLiteral.getFactory().createLiteral();
        result.setBase(ctLiteral.getBase());

        if (ctLiteral.getValue() instanceof Integer) {
            result.setValue(Integer.MIN_VALUE);
        } else if (ctLiteral.getValue() instanceof Character) {
            result.setValue(Character.MIN_VALUE);
        } else if (ctLiteral.getValue() instanceof Long) {
            result.setValue(Long.MIN_VALUE);
        }

        return result;
    }

    private static <T> CtLiteral<T> maxValue(CtLiteral<T> ctLiteral) {
        CtLiteral result = ctLiteral.getFactory().createLiteral();
        result.setBase(ctLiteral.getBase());

        if (ctLiteral.getValue() instanceof Integer) {
            result.setValue(Integer.MAX_VALUE);
        } else if (ctLiteral.getValue() instanceof Character) {
            result.setValue(Character.MAX_VALUE);
        } else if (ctLiteral.getValue() instanceof Long) {
            result.setValue(Long.MAX_VALUE);
        }

        return result;
    }

    public Range<T> toRange() {
        // <expr> <op> <literal>
        if (this.operator == BinaryOperatorKind.LE) {
            if (SpoonUtil.resolveCtExpression(this.ctExpression) instanceof CtLiteral<T> exprLiteral) {
                return Range.between(exprLiteral.getValue(), this.ctLiteral.getValue());
            }
            return Range.between(minValue(this.ctLiteral).getValue(), this.ctLiteral.getValue());
        } else if (this.operator == BinaryOperatorKind.GE) {
            // <expr> >= <literal>
            return Range.between(this.ctLiteral.getValue(), maxValue(this.ctLiteral).getValue());
        } else {
            throw new IllegalStateException("Unsupported operator: " + this.operator);
        }
    }
}
