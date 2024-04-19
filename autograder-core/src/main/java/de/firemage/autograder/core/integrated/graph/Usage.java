package de.firemage.autograder.core.integrated.graph;

import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public sealed class Usage permits UsageAccessField, UsageCallMethod, UsageCreateInstance, UsageField {

    private final CtTypeReference<?> start;
    private final CtTypeReference<?> end;

    public Usage(CtTypeReference<?> start, CtTypeReference<?> end) {
        this.start = start;
        this.end = end;
    }

    public CtTypeReference<?> getStart() {
        return start;
    }

    public CtTypeReference<?> getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usage usage = (Usage) o;
        return start.equals(usage.start) && end.equals(usage.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
