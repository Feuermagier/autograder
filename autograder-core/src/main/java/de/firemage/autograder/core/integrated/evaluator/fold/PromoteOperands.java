package de.firemage.autograder.core.integrated.evaluator.fold;

import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtElement;

public final class PromoteOperands implements Fold {
    private final Fold applyOperatorPromotion;
    private final Fold applyCasts;

    private PromoteOperands(
        ApplyOperatorPromotion.OperatorPredicate<? super CtBinaryOperator<?>> shouldApplyOnBinaryOperator,
        ApplyOperatorPromotion.OperatorPredicate<? super CtUnaryOperator<?>> shouldApplyOnUnaryOperator
    ) {
        this.applyOperatorPromotion = ApplyOperatorPromotion.create(shouldApplyOnBinaryOperator, shouldApplyOnUnaryOperator);
        this.applyCasts = ApplyCasts.onLiterals();
    }

    public static Fold create() {
        return new PromoteOperands(ApplyOperatorPromotion.APPLY_ON_LITERAL_OPERAND, ApplyOperatorPromotion.APPLY_ON_LITERAL_OPERAND);
    }

    public static Fold create(
        ApplyOperatorPromotion.OperatorPredicate<? super CtBinaryOperator<?>> shouldApplyOnBinaryOperator,
        ApplyOperatorPromotion.OperatorPredicate<? super CtUnaryOperator<?>> shouldApplyOnUnaryOperator
    ) {
        return new PromoteOperands(shouldApplyOnBinaryOperator, shouldApplyOnUnaryOperator);
    }

    @Override
    public CtElement enter(CtElement ctElement) {
        return this.applyOperatorPromotion.fold(ctElement);
    }

    @Override
    public CtElement exit(CtElement ctElement) {
        return this.applyCasts.fold(ctElement);
    }
}
