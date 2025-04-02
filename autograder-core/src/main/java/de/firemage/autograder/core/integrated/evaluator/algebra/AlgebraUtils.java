package de.firemage.autograder.core.integrated.evaluator.algebra;

import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.FactoryUtil;
import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import de.firemage.autograder.core.integrated.evaluator.fold.EvaluateLiteralOperations;
import de.firemage.autograder.core.integrated.evaluator.fold.EvaluatePartialLiteralOperations;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

final class AlgebraUtils {
    private AlgebraUtils() {
    }

    @SuppressWarnings("unchecked")
    private static CtExpression<Boolean> evaluate(CtExpression<Boolean> ctExpression, Function<? super CtExpression<Boolean>, ? extends CtExpression<Boolean>> evaluateVariable) {
        if (!ExpressionUtil.isBoolean(ctExpression)) {
            throw new IllegalStateException("Expression is not boolean: " + ctExpression + " (" + ctExpression.getClass() + ")" + " of type " + ExpressionUtil.getExpressionType(ctExpression));
        }

        // if the left and right hand operands are boolean, we can evaluate the expression
        if (ctExpression instanceof CtBinaryOperator<?> ctBinaryOperator
            && ExpressionUtil.isBoolean(ctBinaryOperator.getLeftHandOperand())
            && ExpressionUtil.isBoolean(ctBinaryOperator.getRightHandOperand())) {
            var newLeft = evaluate((CtExpression<Boolean>) ctBinaryOperator.getLeftHandOperand(), evaluateVariable);
            if (newLeft != ctBinaryOperator.getLeftHandOperand()) {
                ctBinaryOperator.setLeftHandOperand(newLeft);
            }

            var newRight = evaluate((CtExpression<Boolean>) ctBinaryOperator.getRightHandOperand(), evaluateVariable);
            if (newRight != ctBinaryOperator.getRightHandOperand()) {
                ctBinaryOperator.setRightHandOperand(newRight);
            }

            return ctExpression;
        }

        // if it is an unary operator and the operand is boolean, we can evaluate the expression
        if (ctExpression instanceof CtUnaryOperator<?> ctUnaryOperator && ExpressionUtil.isBoolean(ctUnaryOperator.getOperand())) {
            var newOperand = evaluate((CtExpression<Boolean>) ctUnaryOperator.getOperand(), evaluateVariable);
            if (newOperand != ctUnaryOperator.getOperand()) {
                ctUnaryOperator.setOperand(newOperand);
            }
            return ctExpression;
        }

        // literals are not variables, and they can be evaluated directly
        if (ctExpression instanceof CtLiteral<?> ctLiteral) {
            return (CtExpression<Boolean>) ctLiteral;
        }

        var result = evaluateVariable.apply(ctExpression);
        if (result == null) {
            return ctExpression;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static void visitBooleanVariables(CtExpression<?> ctExpression, Consumer<? super CtExpression<?>> visitor) {
        evaluate((CtExpression<Boolean>) ctExpression, expression -> {
            visitor.accept(expression);
            return null;
        });
    }

    private static boolean evaluateLiterally(CtExpression<Boolean> ctExpression, Predicate<? super CtExpression<?>> predicate) {
        Evaluator evaluator = new Evaluator(EvaluatePartialLiteralOperations.create(), EvaluateLiteralOperations.create());
        // The predicate is applied to every expression that can not be evaluated as boolean expression.
        // For example in a && b, a and b would be replaced with a literal.
        // This results in a spoon expression that can be evaluated to either true or false.
        return ((CtLiteral<Boolean>) evaluator.evaluate(evaluate(ctExpression.clone(), expression -> FactoryUtil.makeLiteral(ctExpression.getFactory().Type().booleanPrimitiveType(), predicate.test(expression))))).getValue();
    }

    /**
     * This method checks if two boolean expressions are actually equal.
     *
     * @param left the left expression
     * @param right the right expression
     * @return {@code true} if the expressions are actually equal, {@code false} otherwise
     */
    public static boolean isActuallyEqualTo(CtExpression<?> left, CtExpression<?> right) {
        // The problem is that expressions like `a && (b || c)` and `a && b || a && c` are equivalent,
        // even though their representation is different.
        //
        // To make sure they are equal, it will evaluate both expressions for all possible values of a, b, and c
        // and compare the results.
        // If the results are equal for all possible values, the expressions are equal.

        // Check if they are actually equal, saving some time:
        if (left.equals(right)) {
            return true;
        }

        // The first step is to find all variables:
        Set<CtExpression<?>> variables = new HashSet<>();
        visitBooleanVariables(left, variables::add);
        visitBooleanVariables(right, variables::add);

        // serves as a stable order for the variables
        Map<CtExpression<?>, Integer> orderedVariables = new HashMap<>();
        int k = 0;
        for (CtExpression<?> variable : variables) {
            orderedVariables.put(variable, k);
            k += 1;
        }

        // Then generate all possible values for the variables e.g. 00, 01, 10, 11 for two variables
        int variableCount = orderedVariables.size();
        int max = (int) Math.pow(2, variableCount);
        for (int i = 0; i < max; i++) {
            boolean[] values = new boolean[variableCount];
            for (int j = 0; j < variableCount; j++) {
                values[j] = (i & 1 << j) != 0;
            }

            Predicate<? super CtExpression<?>> predicate = expression -> {
                Integer index = orderedVariables.get(expression);
                if (index == null) {
                    throw new IllegalStateException("Variable not found: " + expression + " in " + orderedVariables);
                }
                return values[index];
            };

            // Then evaluate the expressions for the values
            boolean leftValue = evaluateLiterally((CtExpression<Boolean>) left, predicate);
            boolean rightValue = evaluateLiterally((CtExpression<Boolean>) right, predicate);

            // If the results are not equal, the expressions are not equal
            if (leftValue != rightValue) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if {@code left} is functionally equal to {@code right}.
     *
     * @param left the left expression
     * @param right the right expression
     * @return {@code true} if {@code left} is functionally equal to {@code right}, {@code false} otherwise
     */
    public static boolean isEqualTo(CtExpression<?> left, CtExpression<?> right) {
        return left.equals(right)/* || isActuallyEqualTo(left, right)*/;
    }

    /**
     * Checks if the negation of {@code left} is equal to {@code right}.
     *
     * @param left the left expression
     * @param right the right expression
     * @return {@code true} if the negation of {@code left} is equal to {@code right}, {@code false} otherwise
     */
    public static boolean isNegatedEqualTo(CtExpression<?> left, CtExpression<?> right) {
        // the double negation is to make sure that the negation is simplified as much as possible, so that when comparing
        // the expressions ExpressionUtil.negate(a && b) and !(a && b), we don't compare !a || !b with !(a && b),
        // but instead compare !a || !b with !a || !b
        return isEqualTo(ExpressionUtil.negate(left), ExpressionUtil.negate(ExpressionUtil.negate(right)));
    }
}
