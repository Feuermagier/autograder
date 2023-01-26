package de.firemage.autograder.core.integrated.scope.value;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;

import java.util.Objects;
import java.util.Optional;

public class IndexValueWrapper implements Value {
    private final IndexValue value;

    public IndexValueWrapper(IndexValue value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public boolean isConstant() {
        return this.value.isConstant();
    }

    @Override
    public Optional<CtLiteral<?>> toLiteral() {
        return this.value.toLiteral();
    }

    @Override
    public Optional<CtExpression<?>> toExpression() {
        return this.value.toExpression();
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null) {
            return false;
        }

        return otherObject instanceof IndexValue that && this.value.isEqual(that);
    }

    @Override
    public int hashCode() {
        return this.value.hashValue();
    }
}
