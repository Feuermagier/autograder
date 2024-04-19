package de.firemage.autograder.core.integrated.graph;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public final class UsageCallMethod extends Usage {

    private final CtMethod<?> method;
    
    public UsageCallMethod(CtTypeReference<?> start, CtTypeReference<?> end, CtMethod<?> method) {
        super(start, end);
        this.method = method;
    }

    public CtMethod<?> getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UsageCallMethod that = (UsageCallMethod) o;
        return this.getStart().equals(that.getStart()) && this.getEnd().equals(that.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getStart(), this.getEnd());
    }
}
