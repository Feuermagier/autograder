package de.firemage.autograder.core.integrated.graph;

import lombok.Getter;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public final class UsageField extends Usage {
    @Getter
    private final CtField<?> field;
    
    @Getter
    private final int typeParameterIndex;

    public UsageField(CtTypeReference<?> start, CtTypeReference<?> end, CtField<?> field, int typeParameterIndex) {
        super(start, end);
        this.field = field;
        this.typeParameterIndex = typeParameterIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UsageField that = (UsageField) o;
        return field.equals(that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), field);
    }
}
