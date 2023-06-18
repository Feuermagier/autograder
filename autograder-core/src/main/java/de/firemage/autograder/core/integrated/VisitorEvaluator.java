package de.firemage.autograder.core.integrated;

import org.apache.commons.lang3.reflect.FieldUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.eval.VisitorPartialEvaluator;

/**
 * This is a workaround for a bug in spoon, through which it crashes when trying to evaluate a binary operator
 * with character literals on both sides, because {@link Character} can not be cast to {@link Number}.
 */
public class VisitorEvaluator extends VisitorPartialEvaluator {
    protected void setResult(CtElement result) {
        try {
            FieldUtils.writeField(this, "result", result, true);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("VisitorPartialEvaluator#result is no longer accessible", exception);
        }
    }

    protected CtElement getResult() {
        try {
            return (CtElement) FieldUtils.readField(this, "result", true);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("VisitorPartialEvaluator#result is no longer accessible", exception);
        }
    }

    private static CtLiteral<Integer> toIntegerLiteral(CtLiteral<Character> ctLiteral) {
        return SpoonUtil.makeLiteral(
            ctLiteral.getFactory().Type().INTEGER,
            (int) ctLiteral.getValue()
        );
    }

    private static CtLiteral<Character> toCharacterLiteral(CtLiteral<Integer> ctLiteral) {
        return SpoonUtil.makeLiteral(
            ctLiteral.getFactory().Type().CHARACTER,
            (char) (int) ctLiteral.getValue()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void visitCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        CtExpression<?> left = evaluate(ctBinaryOperator.getLeftHandOperand());
        CtExpression<?> right = evaluate(ctBinaryOperator.getRightHandOperand());

        CtBinaryOperator<T> adjustedOperator = ctBinaryOperator.clone();
        if (ctBinaryOperator.getType() == null) {
            adjustedOperator.setType(left.getType().clone());
        }

        adjustedOperator.setLeftHandOperand(left.clone());
        adjustedOperator.setRightHandOperand(right.clone());

        boolean leftIsCharacter = false;
        if (left instanceof CtLiteral<?> ctLiteral && ctLiteral.getValue() instanceof Character) {
            leftIsCharacter = true;
            adjustedOperator.setLeftHandOperand(toIntegerLiteral((CtLiteral<Character>) ctLiteral));
        }

        boolean rightIsCharacter = false;
        if (right instanceof CtLiteral<?> ctLiteral && ctLiteral.getValue() instanceof Character) {
            rightIsCharacter = true;
            adjustedOperator.setRightHandOperand(toIntegerLiteral((CtLiteral<Character>) ctLiteral));
        }

        super.visitCtBinaryOperator(adjustedOperator);

        CtElement result = this.getResult();

        if (result == null) {
            // no changes have been made by the super implementation, so set the result to the adjusted operator
            // (of course, update integer literals to character literals again)
            if (leftIsCharacter) {
                adjustedOperator.setLeftHandOperand(toCharacterLiteral(
                    (CtLiteral<Integer>) adjustedOperator.getLeftHandOperand()
                ));
            }

            if (rightIsCharacter) {
                adjustedOperator.setRightHandOperand(toCharacterLiteral(
                    (CtLiteral<Integer>) adjustedOperator.getRightHandOperand()
                ));
            }

            if (!ctBinaryOperator.equals(adjustedOperator)) {
                this.setResult(adjustedOperator);
            }
        }

        if (result instanceof CtLiteral<?> ctLiteral
            && ctLiteral.getValue() instanceof Number number
            && (leftIsCharacter || rightIsCharacter)) {
            CtLiteral<Character> updatedResult = SpoonUtil.makeLiteral(
                ctBinaryOperator.getFactory().Type().CHARACTER,
                (char) number.intValue()
            );

            this.setResult(updatedResult);
        }
    }
}
