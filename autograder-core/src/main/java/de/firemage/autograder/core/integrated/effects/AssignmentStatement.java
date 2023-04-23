package de.firemage.autograder.core.integrated.effects;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.reference.CtVariableReference;

import java.util.Optional;

public class AssignmentStatement implements AssignmentEffect {
    private final CtAssignment<?, ?> ctAssignment;
    private final Optional<CtExpression<?>> value;

    private AssignmentStatement(CtAssignment<?, ?> ctAssignment) {
        this.ctAssignment = ctAssignment;
        this.value = Optional.ofNullable(ctAssignment.getAssignment());
    }

    public static Optional<Effect> of(CtStatement ctStatement) {
        if (ctStatement instanceof CtAssignment<?, ?> ctAssignment && ctAssignment.getAssigned() instanceof CtVariableWrite<?>) {
            return Optional.of(new AssignmentStatement(ctAssignment));
        }

        return Optional.empty();
    }

    @Override
    public CtStatement ctStatement() {
        return this.ctAssignment;
    }

    @Override
    public Optional<CtExpression<?>> value() {
        return this.value;
    }

    @Override
    public CtVariableReference<?> target() {
        return ((CtVariableWrite<?>) this.ctAssignment.getAssigned()).getVariable();
    }

    @Override
    public boolean isSameEffect(Effect other) {
        return other instanceof AssignmentEffect otherAssignment && this.target().equals(otherAssignment.target());
    }
}
