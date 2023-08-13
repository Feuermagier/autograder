package de.firemage.autograder.core.integrated.evaluator.fold;

import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Chains multiple folds together.
 * <p>
 * The folds are applied in the order they are given.
 */
public final class ChainedFold implements Fold {
    private final List<Fold> folds;

    private ChainedFold(List<Fold> folds) {
        this.folds = new ArrayList<>(folds);
    }

    public static Fold chain(Fold first, Fold... other) {
        List<Fold> folds = new ArrayList<>(List.of(first));
        folds.addAll(Arrays.asList(other));
        return new ChainedFold(folds);
    }

    public static Fold chain(List<Fold> folds) {
        return new ChainedFold(folds);
    }

    @Override
    public CtElement enter(CtElement ctElement) {
        CtElement result = ctElement;

        for (Fold fold : this.folds) {
            result = fold.enter(result);
        }

        return result;
    }

    @Override
    public CtElement exit(CtElement ctElement) {
        CtElement result = ctElement;

        for (Fold fold : this.folds) {
            result = fold.exit(result);
        }

        return result;
    }

    @Override
    public CtElement fold(CtElement ctElement) {
        CtElement result = ctElement;

        for (Fold fold : this.folds) {
            result = fold.fold(result);
        }

        return result;
    }
}
