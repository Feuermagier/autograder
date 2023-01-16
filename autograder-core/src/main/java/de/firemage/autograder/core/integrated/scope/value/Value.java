package de.firemage.autograder.core.integrated.scope.value;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;

import java.util.Optional;

public interface Value {
    /**
     * Returns true if the value is a literal, which is known.
     *
     * @return true if it is constant and false if not
     */
    default boolean isConstant() {
        return this.toLiteral().isPresent();
    }

    /**
     * Returns the value as a CtLiteral if possible.
     *
     * @return the value as a literal or none if it is not known or not a literal
     */
    default Optional<CtLiteral<?>> toLiteral() {
        return this.toExpression().flatMap(value -> {
            if (value instanceof CtLiteral<?> literal) {
                return Optional.of(literal);
            } else {
                return Optional.empty();
            }
        });
    }

    /**
     * Returns the value as a CtExpression if possible.
     *
     * @return the value as a CtExpression or none if it is not convertible
     */
    Optional<CtExpression<?>> toExpression();
}
