package de.firemage.codelinter.core.spoon.analysis;

import java.util.List;

public class VariableState<S extends ValueSet<S>> {
    private final S subset;
    private final S superset;

    public VariableState(S subset, S superset) {
        this.subset = subset;
        this.superset = superset;
    }

    public static <S extends ValueSet<S>> VariableState<S> fromBranches(List<VariableState<S>> branches) {
        if (branches.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        S newSubset = branches.get(0).subset;
        S newSuperset = branches.get(0).superset;
        for (int i = 1; i < branches.size(); i++) {
            newSubset = newSubset.intersect(branches.get(i).subset);
            newSuperset = newSuperset.combine(branches.get(i).superset);
        }
        return new VariableState<>(newSubset, newSuperset);
    }
}
