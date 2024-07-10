package de.firemage.autograder.core.integrated;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtLocalVariableReference;

import java.util.Optional;
import java.util.Set;

public record ForLoopRange(
    CtLocalVariableReference<Integer> loopVariable,
    CtExpression<Integer> start,
    CtExpression<Integer> end) {

    @SuppressWarnings("unchecked")
    public static Optional<ForLoopRange> fromCtFor(CtFor ctFor) {
        CtLocalVariable<?> potentialLoopVariable = null;

        // check if the loop variable is not declared in the for loop
        if (ctFor.getForInit().isEmpty()
            // then look at the variable used in the break condition
            && ctFor.getExpression() instanceof CtBinaryOperator<Boolean> ctBinaryOperator
            && ctBinaryOperator.getLeftHandOperand() instanceof CtVariableAccess<?> ctVariableAccess
            && ctVariableAccess.getVariable() != null
            // check that this variable is a local variable
            && ctVariableAccess.getVariable().getDeclaration() instanceof CtLocalVariable<?> localVariable
            // which is declared before the loop
            && SpoonUtil.getPreviousStatement(ctFor)
                .map(statement -> statement instanceof CtVariable<?> ctVariable
                    && ctVariable.getReference().equals(ctVariableAccess.getVariable()))
                .orElse(false)
            // the loop variable must not be used after the loop
            && SpoonUtil.getNextStatements(ctFor).stream().noneMatch(statement -> UsesFinder.variableUses(localVariable).nestedIn(statement).hasAny())
        ) {
            potentialLoopVariable = localVariable;
        } else if (ctFor.getForInit().size() == 1 && ctFor.getForInit().get(0) instanceof CtLocalVariable<?> ctLocalVariable) {
            potentialLoopVariable = ctLocalVariable;
        }

        if (potentialLoopVariable == null
            || !TypeUtil.isTypeEqualTo(potentialLoopVariable.getType(), int.class, Integer.class)
            || potentialLoopVariable.getDefaultExpression() == null) {
            return Optional.empty();
        }

        CtLocalVariable<Integer> ctLocalVariable = (CtLocalVariable<Integer>) potentialLoopVariable;

        // ensure that the loop has exactly one variable initialized with a literal value
        CtExpression<?> start = SpoonUtil.resolveCtExpression(ctLocalVariable.getDefaultExpression());

        // ensure that it is initialized with some integer
        if (!(TypeUtil.isTypeEqualTo(start.getType(), int.class, Integer.class))
            // validate the for expression:
            || ctFor.getExpression() == null
            // must be i <= or i <
            || !(ctFor.getExpression() instanceof CtBinaryOperator<Boolean> loopExpression)
            || !Set.of(BinaryOperatorKind.LT, BinaryOperatorKind.LE).contains(loopExpression.getKind())
            || !(TypeUtil.isTypeEqualTo(loopExpression.getRightHandOperand().getType(), int.class, Integer.class))
            // check that the left side is the loop variable
            || !(loopExpression.getLeftHandOperand() instanceof CtVariableAccess<?> ctVariableAccess)
            || !ctVariableAccess.getVariable().equals(ctLocalVariable.getReference())
            // the update should be a simple increment:
            || ctFor.getForUpdate().size() != 1
            // either i++ or ++i
            || !(ctFor.getForUpdate().get(0) instanceof CtUnaryOperator<?> ctUnaryOperator)
            || !(ctUnaryOperator.getOperand() instanceof CtVariableWrite<?> counterWrite)
            || !counterWrite.getVariable().equals(ctLocalVariable.getReference())
            || !(Set.of(UnaryOperatorKind.PREINC, UnaryOperatorKind.POSTINC).contains(ctUnaryOperator.getKind()))) {
            return Optional.empty();
        }

        CtExpression<Integer> end = (CtExpression<Integer>) loopExpression.getRightHandOperand();
        // convert i <= n to i < n + 1
        if (loopExpression.getKind() == BinaryOperatorKind.LE) {
            // check for i <= n - 1, so it is not converted to (n - 1) + 1
            if (end instanceof CtBinaryOperator<Integer> ctBinaryOperator
                && ctBinaryOperator.getKind() == BinaryOperatorKind.MINUS
                && SpoonUtil.isIntegerLiteral(SpoonUtil.resolveCtExpression(ctBinaryOperator.getRightHandOperand()), 1)) {
                end = (CtExpression<Integer>) ctBinaryOperator.getLeftHandOperand();
            } else {
                end = SpoonUtil.createBinaryOperator(
                    end,
                    SpoonUtil.makeLiteral(end.getType(), 1),
                    BinaryOperatorKind.PLUS
                );
            }
        }

        return Optional.of(new ForLoopRange(
            (CtLocalVariableReference<Integer>) ctLocalVariable.getReference(),
            (CtExpression<Integer>) start,
            end
        ));
    }

    public CtExpression<Integer> length() {
        CtExpression<Integer> length = this.end;
        // special case init with 0, because end - 0 = end
        if (!SpoonUtil.isIntegerLiteral(this.start, 0)) {
            length = SpoonUtil.createBinaryOperator(
                this.end,
                this.start,
                BinaryOperatorKind.MINUS
            );
        }

        return length;
    }
}
