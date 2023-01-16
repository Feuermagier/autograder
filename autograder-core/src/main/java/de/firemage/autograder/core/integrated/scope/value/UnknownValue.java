package de.firemage.autograder.core.integrated.scope.value;


import spoon.reflect.code.CtExpression;

import java.util.Optional;

public class UnknownValue implements Value {
    /**
     * Creates a new UnknownValue.
     */
    public UnknownValue() {
        // might be used in the future
    }

    @Override
    public Optional<CtExpression<?>> toExpression() {
        return Optional.empty();
    }


    // NOTE: default equals and hashCode are ideal for this class
}
