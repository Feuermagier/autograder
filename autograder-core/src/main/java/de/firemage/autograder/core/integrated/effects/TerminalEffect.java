package de.firemage.autograder.core.integrated.effects;

import spoon.reflect.code.CtThrow;

public interface TerminalEffect extends Effect {
    default boolean isThrow() {
        return this.ctStatement() instanceof CtThrow;
    }
}
