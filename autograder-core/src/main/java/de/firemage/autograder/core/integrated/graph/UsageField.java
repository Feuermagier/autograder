package de.firemage.autograder.core.integrated.graph;

import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public final class UsageField extends Usage {

    private final CtField<?> field;
    private final int typeParameterIndex;

    public UsageField(CtTypeReference<?> start, CtTypeReference<?> end, CtField<?> field, int typeParameterIndex) {
        super(start, end);
        this.field = field;
        this.typeParameterIndex = typeParameterIndex;
    }

    public CtField<?> getField() {
        return field;
    }

    public int getTypeParameterIndex() {
        return typeParameterIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UsageField that = (UsageField) o;
        return this.getStart().equals(that.getStart()) && this.getEnd().equals(that.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getStart(), this.getEnd());
    }
}
