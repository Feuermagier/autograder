package de.firemage.autograder.core.integrated.graph;

import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public final class UsageAccessField extends Usage {

    private final CtField<?> field;

    public UsageAccessField(CtTypeReference<?> start, CtTypeReference<?> end, CtField<?> field) {
        super(start, end);
        this.field = field;
    }

    public CtField<?> getField() {
        return field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UsageAccessField that = (UsageAccessField) o;
        return this.getStart().equals(that.getStart()) && this.getEnd().equals(that.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getStart(), this.getEnd());
    }
}
