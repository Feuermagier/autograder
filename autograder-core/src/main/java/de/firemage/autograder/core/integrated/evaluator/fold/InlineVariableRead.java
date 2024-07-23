package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.VariableUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtVariable;

import java.util.Optional;

/**
 * Inline reads of constant variables with its value.
 */
public final class InlineVariableRead implements Fold {
    private final boolean ignoreLocalVariables;

    private InlineVariableRead(boolean ignoreLocalVariables) {
        this.ignoreLocalVariables = ignoreLocalVariables;
    }

    public static Fold create() {
        return new InlineVariableRead(false);
    }

    public static Fold create(boolean ignoreLocalVariables) {
        return new InlineVariableRead(ignoreLocalVariables);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CtExpression<T> foldCtVariableRead(CtVariableRead<T> ctVariableRead) {
        CtVariable<T> ctVariable = (CtVariable<T>) UsesFinder.getDeclaredVariable(ctVariableRead);

        if (ctVariable == null || this.ignoreLocalVariables && ctVariable instanceof CtLocalVariable<T>) {
            return ctVariableRead;
        }

        Optional<CtExpression<T>> ctExpressionOptional = VariableUtil.getEffectivelyFinalExpression(ctVariable);

        return ctExpressionOptional.flatMap(ctExpression -> {
            // only inline literals:
            if (ctExpression instanceof CtLiteral<?> ctLiteral) {
                return Optional.of((CtExpression<T>) ctLiteral);
            } else {
                return Optional.empty();
            }
        }).orElse(ctVariableRead);
    }
}
