package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Applies casts to {@link CtExpression}s.
 */
public final class ApplyCasts implements Fold {
    private final Predicate<? super CtExpression<?>> shouldApply;

    private ApplyCasts(Predicate<? super CtExpression<?>> shouldApply) {
        this.shouldApply = shouldApply;
    }

    public static Fold onLiterals() {
        return new ApplyCasts(ctExpression -> ctExpression instanceof CtLiteral<?>);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> CtLiteral<T> foldCtLiteral(CtLiteral<T> ctLiteral) {
        CtLiteral result = ctLiteral;

        if (!this.shouldApply.test(ctLiteral)) {
            return result;
        }

        List<CtTypeReference<?>> casts = new ArrayList<>(ctLiteral.getTypeCasts());
        Collections.reverse(casts);
        ctLiteral.setTypeCasts(new ArrayList<>());

        for (CtTypeReference<?> cast : casts) {
            result = SpoonUtil.castLiteral(cast, result);
        }

        return result;
    }
}
