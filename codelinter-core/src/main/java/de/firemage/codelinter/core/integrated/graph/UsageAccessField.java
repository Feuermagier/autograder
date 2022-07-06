package de.firemage.codelinter.core.integrated.graph;

import lombok.Getter;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public final class UsageAccessField extends Usage {
    @Getter
    private final CtField<?> field;

    public UsageAccessField(CtTypeReference<?> start, CtTypeReference<?> end, CtField<?> field) {
        super(start, end);
        this.field = field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UsageAccessField that = (UsageAccessField) o;
        return field.equals(that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), field);
    }
}
