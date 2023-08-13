
package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.evaluator.OperatorHelper;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.reference.CtTypeReference;

public final class FoldUtils {
    private FoldUtils() {
    }

    /**
     * Copy-pasted from {@link spoon.support.reflect.eval.VisitorPartialEvaluator}.
     *
     * @param type the type of the number
     * @param number some number that should be converted to a value of the given type
     * @return the converted number
     */
    public static Number convert(CtTypeReference<?> type, Number number) {
        CtTypeReference<?> targetType = type.unbox();

        if (targetType.getActualClass() == int.class) {
            return number.intValue();
        }

        if (targetType.getActualClass() == byte.class) {
            return number.byteValue();
        }

        if (targetType.getActualClass() == long.class) {
            return number.longValue();
        }

        if (targetType.getActualClass() == float.class) {
            return number.floatValue();
        }

        if (targetType.getActualClass() == short.class) {
            return number.shortValue();
        }

        if (targetType.getActualClass() == double.class) {
            return number.doubleValue();
        }

        return number;
    }

    private static <T> CtExpression<T> inferTypeIfNeeded(CtExpression<T> ctExpression) {
        CtExpression<T> result = ctExpression.clone();

        if (result instanceof CtBinaryOperator<T> ctBinaryOperator && ctBinaryOperator.getType() == null) {
            ctBinaryOperator.setType(inferType(ctBinaryOperator));
        } else if (result instanceof CtUnaryOperator<T> ctUnaryOperator && ctUnaryOperator.getType() == null) {
            ctUnaryOperator.setType(inferType(ctUnaryOperator));
        }

        return result;
    }

    public static CtTypeReference<?> inferType(CtBinaryOperator<?> ctBinaryOperator) {
        return switch (ctBinaryOperator.getKind()) {
            case AND, OR, INSTANCEOF, EQ, NE, LT, LE, GT, GE -> ctBinaryOperator.getFactory().Type().BOOLEAN_PRIMITIVE;
            case SL, SR, USR, MUL, DIV, MOD, MINUS, PLUS, BITAND, BITXOR, BITOR -> OperatorHelper.getPromotedType(
                ctBinaryOperator.getKind(),
                inferTypeIfNeeded(ctBinaryOperator.getLeftHandOperand()),
                inferTypeIfNeeded(ctBinaryOperator.getRightHandOperand())
            ).orElseThrow();
        };
    }

    public static CtTypeReference<?> inferType(CtUnaryOperator<?> ctUnaryOperator) {
        return OperatorHelper.getPromotedType(
            ctUnaryOperator.getKind(),
            inferTypeIfNeeded(ctUnaryOperator.getOperand())
        ).orElseThrow();
    }
}
