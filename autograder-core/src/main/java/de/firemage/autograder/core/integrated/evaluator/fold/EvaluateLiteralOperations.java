package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.FactoryUtil;
import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import de.firemage.autograder.core.integrated.evaluator.OperatorHelper;
import spoon.SpoonException;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.eval.PartialEvaluator;
import spoon.reflect.reference.CtTypeReference;

/**
 * Evaluates literal operations like {@code 1 + 2} that can be evaluated to a constant value.
 */
public final class EvaluateLiteralOperations implements Fold {
    private final PartialEvaluator evaluator;

    private EvaluateLiteralOperations() {
        this.evaluator = new Evaluator(PromoteOperands.create());
    }

    public static Fold create() {
        return new EvaluateLiteralOperations();
    }

    private boolean isFloatingType(CtTypeReference<?> type) {
        if (type == null) {
            return false;
        }
        return type.equals(type.getFactory().Type().doublePrimitiveType())
            || type.equals(type.getFactory().Type().floatPrimitiveType())
            || type.equals(type.getFactory().Type().doubleType())
            || type.equals(type.getFactory().Type().floatType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CtExpression<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        CtBinaryOperator<T> promotedOperator = this.evaluator.evaluate(ctBinaryOperator);

        if (!(promotedOperator.getLeftHandOperand() instanceof CtLiteral<?> leftLiteral) ||
            !(promotedOperator.getRightHandOperand() instanceof CtLiteral<?> rightLiteral)) {
            return ctBinaryOperator;
        }

        Object leftObject = leftLiteral.getValue();
        Object rightObject = rightLiteral.getValue();

        CtTypeReference<T> operatorType = (CtTypeReference<T>) ExpressionUtil.getExpressionType(ctBinaryOperator);

        Object value = switch (ctBinaryOperator.getKind()) {
            case AND -> (Boolean) leftObject && (Boolean) rightObject;
            case OR -> (Boolean) leftObject || (Boolean) rightObject;
            case EQ -> {
                if (leftObject == null) {
                    yield leftObject == rightObject;
                } else {
                    yield leftObject.equals(rightObject);
                }
            }
            case NE -> {
                if (leftObject == null) {
                    yield leftObject != rightObject;
                } else {
                    yield !leftObject.equals(rightObject);
                }
            }
            case GE -> ((Number) leftObject).doubleValue() >= ((Number) rightObject).doubleValue();
            case LE -> ((Number) leftObject).doubleValue() <= ((Number) rightObject).doubleValue();
            case GT -> ((Number) leftObject).doubleValue() > ((Number) rightObject).doubleValue();
            case LT -> ((Number) leftObject).doubleValue() < ((Number) rightObject).doubleValue();
            case MINUS -> FoldUtils.convert(
                operatorType,
                ((Number) leftObject).doubleValue() - ((Number) rightObject).doubleValue()
            );
            case MUL -> FoldUtils.convert(
                operatorType,
                ((Number) leftObject).doubleValue() * ((Number) rightObject).doubleValue()
            );
            case DIV -> {
                try {
                    // handle floating point division differently than integer division, because
                    // dividing by 0 is not an error for floating point numbers.
                    if (this.isFloatingType(operatorType)) {
                        yield FoldUtils.convert(operatorType,
                            ((Number) leftObject).doubleValue() / ((Number) rightObject).doubleValue());
                    } else {
                        yield FoldUtils.convert(operatorType,
                            ((Number) leftObject).longValue() / ((Number) rightObject).longValue());
                    }
                } catch (ArithmeticException exception) {
                    // division by 0
                    throw new SpoonException(
                        String.format(
                            "Expression '%s' evaluates to '%s %s %s' which can not be evaluated",
                            ctBinaryOperator,
                            leftObject,
                            OperatorHelper.getOperatorText(ctBinaryOperator.getKind()),
                            rightObject
                        ),
                        exception
                    );
                }
            }
            case PLUS -> {
                if ((leftObject instanceof String) || (rightObject instanceof String)) {
                    yield "" + leftObject + rightObject;
                } else {
                    yield FoldUtils.convert(
                        operatorType,
                        ((Number) leftObject).doubleValue() + ((Number) rightObject).doubleValue()
                    );
                }
            }
            case MOD -> FoldUtils.convert(
                operatorType,
                ((Number) leftObject).doubleValue() % ((Number) rightObject).doubleValue()
            );
            case BITAND -> {
                if (leftObject instanceof Boolean) {
                    yield (Boolean) leftObject && (Boolean) rightObject;
                } else {
                    yield FoldUtils.convert(
                        operatorType,
                        ((Number) leftObject).longValue() & ((Number) rightObject).longValue()
                    );
                }
            }
            case BITOR -> {
                if (leftObject instanceof Boolean) {
                    yield (Boolean) leftObject || (Boolean) rightObject;
                } else {
                    yield FoldUtils.convert(operatorType,
                        ((Number) leftObject).longValue() | ((Number) rightObject).longValue());
                }
            }
            case BITXOR -> {
                if (leftObject instanceof Boolean) {
                    yield (Boolean) leftObject ^ (Boolean) rightObject;
                } else {
                    yield FoldUtils.convert(operatorType,
                        ((Number) leftObject).longValue() ^ ((Number) rightObject).longValue());
                }
            }
            case SL -> {
                long rightObjectValue = ((Number) rightObject).longValue();
                if (leftObject instanceof Long) {
                    yield (long) leftObject << rightObjectValue;
                } else {
                    yield ((Number) leftObject).intValue() << rightObjectValue;
                }
            }
            case SR -> {
                long rightObjectValue = ((Number) rightObject).longValue();
                if (leftObject instanceof Long) {
                    yield (long) leftObject >> rightObjectValue;
                } else {
                    yield ((Number) leftObject).intValue() >> rightObjectValue;
                }
            }
            case USR -> {
                long rightObjectValue = ((Number) rightObject).longValue();
                if (leftObject instanceof Long) {
                    yield (long) leftObject >>> rightObjectValue;
                } else {
                    yield ((Number) leftObject).intValue() >>> rightObjectValue;
                }
            }
            default ->
                throw new UnsupportedOperationException("Unsupported Operator '%s'".formatted(ctBinaryOperator.getKind()));
        };

        return FactoryUtil.makeLiteral(operatorType, (T) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CtExpression<T> foldCtUnaryOperator(CtUnaryOperator<T> ctUnaryOperator) {
        CtUnaryOperator<T> promotedOperator = this.evaluator.evaluate(ctUnaryOperator);

        CtExpression<?> operand = promotedOperator.getOperand();
        if (!(operand instanceof CtLiteral<?> literal)) {
            return ctUnaryOperator;
        }

        CtTypeReference<T> operatorType = (CtTypeReference<T>) ExpressionUtil.getExpressionType(promotedOperator);
        Object literalValue = literal.getValue();
        Object value = switch (promotedOperator.getKind()) {
            case NOT -> !(Boolean) literalValue;
            case NEG -> {
                if (this.isFloatingType(operatorType)) {
                    yield FoldUtils.convert(operatorType, -1 * ((Number) literalValue).doubleValue());
                } else {
                    yield FoldUtils.convert(operatorType, -1 * ((Number) literalValue).longValue());
                }
            }
            case POS -> {
                if (this.isFloatingType(literal.getType())) {
                    yield FoldUtils.convert(operatorType, +((Number) literalValue).doubleValue());
                } else {
                    yield FoldUtils.convert(operatorType, +((Number) literalValue).longValue());
                }
            }
            case COMPL -> FoldUtils.convert(operatorType, ~((Number) literalValue).longValue());
            default ->
                throw new UnsupportedOperationException("Unsupported Operator '%s'".formatted(ctUnaryOperator.getKind()));
        };

        return FactoryUtil.makeLiteral(operatorType, (T) value);
    }
}
