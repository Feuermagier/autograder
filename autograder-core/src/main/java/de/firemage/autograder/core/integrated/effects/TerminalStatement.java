package de.firemage.autograder.core.integrated.effects;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtYieldStatement;

import java.util.Optional;

public class TerminalStatement implements TerminalEffect {
    private final CtStatement ctStatement;
    private final Optional<CtExpression<?>> ctExpression;

    private TerminalStatement(CtStatement ctStatement) {
        if (!isTerminalStatement(ctStatement)) {
            throw new IllegalArgumentException("Not a terminal statement: " + ctStatement);
        }

        this.ctStatement = ctStatement;

        if (ctStatement instanceof CtReturn<?> ctReturn) {
            this.ctExpression = Optional.ofNullable(ctReturn.getReturnedExpression());
        } else if (ctStatement instanceof CtThrow ctThrow) {
            this.ctExpression = Optional.of(ctThrow.getThrownExpression());
        } else if (ctStatement instanceof CtYieldStatement ctYieldStatement) {
            this.ctExpression = Optional.of(ctYieldStatement.getExpression());
        } else {
            this.ctExpression = Optional.empty();
        }
    }

    private static boolean isTerminalStatement(CtStatement ctStatement) {
        return ctStatement instanceof CtYieldStatement
                || ctStatement instanceof CtReturn<?>
                || ctStatement instanceof CtThrow;
    }

    public static Optional<Effect> of(CtStatement ctStatement) {
        if (isTerminalStatement(ctStatement)) {
            return Optional.of(new TerminalStatement(ctStatement));
        }

        return Optional.empty();
    }

    @Override
    public CtStatement ctStatement() {
        return this.ctStatement;
    }

    @Override
    public Optional<CtExpression<?>> value() {
        return this.ctExpression;
    }

    @Override
    public boolean isSameEffect(Effect other) {
        if (other instanceof TerminalEffect) {
            return this.ctStatement instanceof CtYieldStatement && other.ctStatement() instanceof CtYieldStatement
                    || this.ctStatement instanceof CtReturn<?> && other.ctStatement() instanceof CtReturn<?>
                    || this.ctStatement instanceof CtThrow && other.ctStatement() instanceof CtThrow;
        }

        return false;
    }
}
