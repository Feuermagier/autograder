package de.firemage.autograder.core.integrated.graph;

import lombok.Getter;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public final class UsageCreateInstance extends Usage {
    @Getter
    private final CtConstructor<?> constructor;

    public UsageCreateInstance(CtTypeReference<?> start, CtTypeReference<?> end, CtConstructor<?> constructor) {
        super(start, end);
        this.constructor = constructor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UsageCreateInstance that = (UsageCreateInstance) o;
        return this.getStart().equals(that.getStart()) && this.getEnd().equals(that.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getStart(), this.getEnd());
    }
}
