package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.eval.PartialEvaluator;
import spoon.reflect.factory.Factory;

public final class EvaluatePartialLiteralOperations implements Fold {
    private final PartialEvaluator evaluator;

    private EvaluatePartialLiteralOperations() {
        this.evaluator = new Evaluator(PromoteOperands.create(
            (operator, ctExpression) -> true,
            (operator, ctExpression) -> true
        ));
    }

    public static Fold create() {
        return new EvaluatePartialLiteralOperations();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CtExpression<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        CtBinaryOperator<T> promotedOperator = this.evaluator.evaluate(ctBinaryOperator);

        CtExpression<?> rightExpression = promotedOperator.getRightHandOperand();

        if (!(promotedOperator.getLeftHandOperand() instanceof CtLiteral<?>) && promotedOperator.getRightHandOperand() instanceof CtLiteral<?>) {
            promotedOperator = SpoonUtil.swapCtBinaryOperator(promotedOperator);
        }

        // ignore if both operands are not literals
        if (!(promotedOperator.getLeftHandOperand() instanceof CtLiteral<?> ctLiteral)) {
            System.out.println(promotedOperator);
            return ctBinaryOperator;
        }

        CtExpression<?> ctExpression = promotedOperator.getRightHandOperand();

        Factory factory = ctBinaryOperator.getFactory();
        boolean isLiteralValueTrue = Boolean.TRUE.equals(ctLiteral.getValue());

        CtLiteral<T> trueLiteral = (CtLiteral<T>) factory.createLiteral(true);
        CtLiteral<T> falseLiteral = (CtLiteral<T>) factory.createLiteral(false);

        return switch (promotedOperator.getKind()) {
            case AND -> {
                if (isLiteralValueTrue) {
                    // true && b -> b
                    yield (CtExpression<T>) ctExpression;
                } else {
                    // false && b -> false
                    yield falseLiteral;
                }
            }
            case OR -> {
                if (isLiteralValueTrue) {
                    // true || b -> true
                    yield trueLiteral;
                } else {
                    // false || b -> b
                    yield (CtExpression<T>) ctExpression;
                }
            }
            case EQ -> {
                // a == true -> a
                if (ctLiteral.getValue() instanceof Boolean && isLiteralValueTrue) {
                    yield (CtExpression<T>) ctExpression;
                }

                yield ctBinaryOperator;
            }
            case DIV -> {
                if (rightExpression instanceof CtLiteral<?> literal
                    && literal.getValue() instanceof Number number
                    && number.doubleValue() == 1.0) {
                    yield (CtExpression<T>) ctExpression;
                }

                yield ctBinaryOperator;
            }
            case PLUS -> {
                if (ctLiteral.getValue() instanceof Number number && number.doubleValue() == 0.0) {
                    yield (CtExpression<T>) ctExpression;
                }

                yield ctBinaryOperator;
            }
            case MUL -> {
                if (ctLiteral.getValue() instanceof Number number && number.doubleValue() == 0.0) {
                    yield (CtExpression<T>) ctLiteral;
                }

                if (ctLiteral.getValue() instanceof Number number && number.doubleValue() == 1.0) {
                    yield (CtExpression<T>) ctExpression;
                }

                yield ctBinaryOperator;
            }
            case MINUS -> {
                if (rightExpression instanceof CtLiteral<?> literal
                    && literal.getValue() instanceof Number number
                    && number.doubleValue() == 0.0) {
                    yield (CtExpression<T>) ctExpression;
                }

                yield ctBinaryOperator;
            }
            default -> ctBinaryOperator;
        };
    }
}
