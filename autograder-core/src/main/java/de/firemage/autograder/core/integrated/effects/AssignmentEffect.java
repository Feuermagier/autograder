package de.firemage.autograder.core.integrated.effects;

import spoon.reflect.reference.CtVariableReference;

public interface AssignmentEffect extends Effect {
    CtVariableReference<?> target();
}
