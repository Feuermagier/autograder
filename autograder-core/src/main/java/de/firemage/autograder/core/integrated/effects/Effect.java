package de.firemage.autograder.core.integrated.effects;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

import java.util.Optional;

public interface Effect {
    CtStatement ctStatement();

    Optional<CtExpression<?>> value();

    boolean isSameEffect(Effect other);
}
