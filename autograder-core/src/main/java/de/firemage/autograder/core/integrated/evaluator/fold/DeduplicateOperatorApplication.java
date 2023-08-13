package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.eval.PartialEvaluator;

/**
 * Removes operators that cancel each other out like {@code -(-x)} or {@code !(!x)} that would be {@code x}.
 * <p>
 * Note that this fold might add casts to ensure that the type of the expression is preserved.
 */
public final class DeduplicateOperatorApplication implements Fold {
    private final PartialEvaluator evaluator;
    private DeduplicateOperatorApplication() {
        this.evaluator = new Evaluator(ApplyOperatorPromotion.create(
            (operator, ctExpression) -> true,
            (operator, ctExpression) -> true
        ));
    }

    public static Fold create() {
        return new DeduplicateOperatorApplication();
    }

    @Override
    public <T> CtExpression<T> foldCtUnaryOperator(CtUnaryOperator<T> ctUnaryOperator) {
        // the promoted result is only used if the operator can be optimized
        CtUnaryOperator<T> promotedResult = this.evaluator.evaluate(ctUnaryOperator);
        CtExpression<T> operand = promotedResult.getOperand();

        return switch (ctUnaryOperator.getKind()) {
            // -(-x) -> x
            case NEG -> {
                if (operand instanceof CtUnaryOperator<T> unaryOperand && (unaryOperand.getKind() == UnaryOperatorKind.NEG)) {
                    yield unaryOperand.getOperand();
                }

                yield ctUnaryOperator;
            }
            // !(!x) -> x
            case NOT -> {
                if (operand instanceof CtUnaryOperator<T> unaryOperand && (unaryOperand.getKind() == UnaryOperatorKind.NOT)) {
                    yield unaryOperand.getOperand();
                }

                yield ctUnaryOperator;
            }
            default -> ctUnaryOperator;
        };
    }
}
