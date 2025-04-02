package de.firemage.autograder.core.integrated.evaluator.algebra;

import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.FactoryUtil;
import de.firemage.autograder.core.integrated.evaluator.fold.Fold;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;

public final class ApplyAbsorptionLaw implements Fold {
    private ApplyAbsorptionLaw() {
    }

    public static Fold create() {
        return new ApplyAbsorptionLaw();
    }

    @Override
    public <T> CtExpression<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        // Applies the following simplifications:
        // - a && (a || b) = a
        // - a || (a && b) = a
        // - a || (!a && c) = a || c
        CtExpression<?> left = ctBinaryOperator.getLeftHandOperand();
        CtExpression<?> right = ctBinaryOperator.getRightHandOperand();

        if (!ExpressionUtil.isBoolean(left) || !ExpressionUtil.isBoolean(right)) {
            return ctBinaryOperator;
        }

        CtExpression<T> result = internalFold(left, right, ctBinaryOperator.getKind(), false);
        if (result == null) {
            result = internalFold(right, left, ctBinaryOperator.getKind(), true);
        }

        if (result != null) {
            return result;
        }

        return ctBinaryOperator;
    }

    @SuppressWarnings("unchecked")
    private static <T> CtExpression<T> internalFold(CtExpression<?> left, CtExpression<?> right, BinaryOperatorKind kind, boolean isSwapped) {
        switch (kind) {
            case AND -> {
                // a && (a || b)
                // = a && a || a && b
                // = a || a && b
                // = a

                // The same would happen for (a || b) && a
                if (right instanceof CtBinaryOperator<?> rightBinaryOperator
                        && rightBinaryOperator.getKind() == BinaryOperatorKind.OR
                        && AlgebraUtils.isEqualTo(left, rightBinaryOperator.getLeftHandOperand())) {
                    return (CtExpression<T>) left;
                }

                // a && (b || a)
                // The option (b || a) && a is not considered, because the evaluation order would be different
                if (!isSwapped && right instanceof CtBinaryOperator<?> rightBinaryOperator
                        && rightBinaryOperator.getKind() == BinaryOperatorKind.OR
                        && AlgebraUtils.isEqualTo(left, rightBinaryOperator.getRightHandOperand())) {
                    return (CtExpression<T>) left;
                }
            }
            case OR -> {
                // check for a || (a && b) or a || (b && a) which would simplify to a
                if (right instanceof CtBinaryOperator<?> rightBinaryOperator
                        && rightBinaryOperator.getKind() == BinaryOperatorKind.AND) {
                    // a || (a && b) / (a && b) || a
                    if (AlgebraUtils.isEqualTo(left, rightBinaryOperator.getLeftHandOperand())) {
                        return (CtExpression<T>) left;
                    }

                    // a || (b && a)
                    // The option (b && a) || a is not considered, because the evaluation order would be different
                    if (!isSwapped && AlgebraUtils.isEqualTo(left, rightBinaryOperator.getRightHandOperand())) {
                        return (CtExpression<T>) left;
                    }
                }

                // a || (!a && c) => a || c
                if (right instanceof CtBinaryOperator<?> rightBinaryOperator
                        && rightBinaryOperator.getKind() == BinaryOperatorKind.AND) {
                    // a || (!a && c) => a || c
                    if (AlgebraUtils.isNegatedEqualTo(left, rightBinaryOperator.getLeftHandOperand())) {
                        return FactoryUtil.createBinaryOperator(left, rightBinaryOperator.getRightHandOperand(), BinaryOperatorKind.OR);
                    }

                    // a || (c && !a) => a || c
                    if (!isSwapped && AlgebraUtils.isNegatedEqualTo(left, rightBinaryOperator.getRightHandOperand())) {
                        return FactoryUtil.createBinaryOperator(left, rightBinaryOperator.getLeftHandOperand(), BinaryOperatorKind.OR);
                    }
                }
            }
            default -> {
                // ignore the unknown variants
            }
        }

        return null;
    }
}
