package de.firemage.autograder.core.integrated.evaluator.fold;

import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;

public interface Fold {
    /**
     * This method is called before a {@link CtElement} and all of its children are visited.
     * <p>
     * By default, this method does nothing.
     *
     * @param ctElement the element that is about to be visited
     * @return the folded element, might be the same as the input element or some other element
     */
    default CtElement enter(CtElement ctElement) {
        return ctElement;
    }

    /**
     * This method is called after all children of the {@link CtElement} have been visited.
     * <p>
     * By default, this method calls {@link #fold(CtElement)}.
     *
     * @param ctElement the element that has been visited
     * @return the folded element, might be the same as the input element or some other element
     */
    default CtElement exit(CtElement ctElement) {
        return this.fold(ctElement);
    }

    default CtElement fold(CtElement ctElement) {
        if (ctElement instanceof CtExpression<?> ctExpression) {
            return this.foldCtExpression(ctExpression);
        }

        return ctElement;
    }

    default <T> CtExpression<T> foldCtExpression(CtExpression<T> ctExpression) {
        if (ctExpression instanceof CtLiteral<T> ctLiteral) {
            return this.foldCtLiteral(ctLiteral);
        }

        if (ctExpression instanceof CtBinaryOperator<T> ctBinaryOperator) {
            return this.foldCtBinaryOperator(ctBinaryOperator);
        }

        if (ctExpression instanceof CtUnaryOperator<T> ctUnaryOperator) {
            return this.foldCtUnaryOperator(ctUnaryOperator);
        }

        if (ctExpression instanceof CtVariableRead<T> ctVariableRead) {
            return this.foldCtVariableRead(ctVariableRead);
        }

        return ctExpression;
    }

    default <T> CtExpression<T> foldCtLiteral(CtLiteral<T> ctLiteral) {
        return ctLiteral;
    }

    default <T> CtExpression<T> foldCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        return ctBinaryOperator;
    }

    default <T> CtExpression<T> foldCtUnaryOperator(CtUnaryOperator<T> ctUnaryOperator) {
        return ctUnaryOperator;
    }

    default <T> CtExpression<T> foldCtVariableRead(CtVariableRead<T> ctVariableRead) {
        return ctVariableRead;
    }
}
