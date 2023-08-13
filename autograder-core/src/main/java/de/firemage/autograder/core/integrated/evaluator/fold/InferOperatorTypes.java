package de.firemage.autograder.core.integrated.evaluator.fold;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;

/**
 * Infers the type of operators if they are not set.
 * <p>
 * This can happen if the operator is created through a factory method like
 * {@link spoon.reflect.factory.Factory#createBinaryOperator(CtExpression, CtExpression, BinaryOperatorKind)}
 * which does not set the type.
 */
public final class InferOperatorTypes implements Fold {
    private InferOperatorTypes() {
    }

    public static Fold create() {
        return new InferOperatorTypes();
    }

    @Override
    public <T> CtBinaryOperator<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        if (ctBinaryOperator.getType() == null) {
            ctBinaryOperator.setType(FoldUtils.inferType(ctBinaryOperator));
        }

        return ctBinaryOperator;
    }

    @Override
    public <T> CtUnaryOperator<T> foldCtUnaryOperator(CtUnaryOperator<T> ctUnaryOperator) {
        if (ctUnaryOperator.getType() == null) {
            ctUnaryOperator.setType(FoldUtils.inferType(ctUnaryOperator));
        }

        return ctUnaryOperator;
    }
}
