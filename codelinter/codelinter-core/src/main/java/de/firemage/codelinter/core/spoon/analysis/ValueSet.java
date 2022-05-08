package de.firemage.codelinter.core.spoon.analysis;

public interface ValueSet<S extends ValueSet> {
    S intersect(S other);
    S combine(S other);
}
