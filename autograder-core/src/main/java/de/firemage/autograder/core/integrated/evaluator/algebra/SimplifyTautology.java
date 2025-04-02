package de.firemage.autograder.core.integrated.evaluator.algebra;

import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.FactoryUtil;
import de.firemage.autograder.core.integrated.evaluator.fold.Fold;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;

/**
 * A tautology is a boolean expression that is always true, like {@code a || !a}.
 * <p>
 * This class simplifies tautologies by replacing them with {@code true} or {@code false}.
 * <p>
 * Warning: Figuring out if an expression is a tautology, is NP-complete, therefore this class is really slow.
 *          The code has been implemented, but is not in use, because it increases the evaluation time by a factor of 20.
 */
public final class SimplifyTautology implements Fold {
    private SimplifyTautology() {
    }

    public static Fold create() {
        return new SimplifyTautology();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CtExpression<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        if (!ExpressionUtil.isBoolean(ctBinaryOperator.getLeftHandOperand()) || !ExpressionUtil.isBoolean(ctBinaryOperator.getRightHandOperand())) {
            return ctBinaryOperator;
        }

        var booleanType = ctBinaryOperator.getFactory().Type().booleanPrimitiveType();
        CtExpression<Boolean> trueExpression = FactoryUtil.makeLiteral(booleanType, true);
        CtExpression<Boolean> falseExpression = FactoryUtil.makeLiteral(booleanType, false);

        if (AlgebraUtils.isEqualTo(ctBinaryOperator, trueExpression)) {
            return (CtExpression<T>) trueExpression;
        }

        if (AlgebraUtils.isEqualTo(ctBinaryOperator, falseExpression)) {
            return (CtExpression<T>) falseExpression;
        }
        return ctBinaryOperator;
    }
}
