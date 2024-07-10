package de.firemage.autograder.core.integrated.evaluator.fold;

import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RemoveRedundantCasts implements Fold {

    private static final List<Class<?>> HIERARCHY = List.of(
        byte.class,
        short.class,
        int.class,
        long.class,
        float.class,
        double.class
    );

    private static final List<Class<?>> CHARACTER_IMPLICIT_WIDENING = List.of(
        int.class,
        long.class,
        float.class,
        double.class
    );

    private RemoveRedundantCasts() {
    }

    public static Fold create() {
        return new RemoveRedundantCasts();
    }

    private static boolean canBeImplicitlyCastTo(CtTypeReference<?> from, CtTypeReference<?> to) {
        // casting to the same type is redundant
        if (from.equals(to)) {
            return true;
        }

        // Character -> char and vice versa is implicit
        if (from.unbox().equals(to.unbox())) {
            return true;
        }

        CtTypeReference<?> unboxedFrom = from.unbox();
        if (unboxedFrom.isPrimitive() && to.isPrimitive()) {
            if (TypeUtil.isTypeEqualTo(unboxedFrom, char.class)) {
                return CHARACTER_IMPLICIT_WIDENING.contains(to.getActualClass());
            }

            int indexFrom = HIERARCHY.indexOf(unboxedFrom.getActualClass());
            int indexTo = HIERARCHY.indexOf(to.getActualClass());

            return indexFrom != -1 && indexTo != -1 && indexFrom <= indexTo;
        }

        // T.isSubtypeOf crashes if T is a type parameter
        if (to instanceof CtTypeParameterReference) {
            return false;
        }

        // String -> Object works (Subclass -> Superclass)
        return to.isSubtypeOf(from);
    }

    private static boolean isBoxedType(CtTypeReference<?> ctTypeReference) {
        return ctTypeReference.unbox().isPrimitive() && !ctTypeReference.equals(ctTypeReference.unbox());
    }

    public static <T> CtExpression<T> removeRedundantCasts(CtExpression<T> ctExpression) {
        CtTypeReference<?> originalType = ctExpression.getType();
        List<CtTypeReference<?>> typeCasts = new ArrayList<>(ctExpression.getTypeCasts());
        Collections.reverse(typeCasts);
        CtTypeReference<?> currentType = originalType;

        List<CtTypeReference<?>> newCasts = new ArrayList<>();

        for (int i = 0; i < typeCasts.size(); i++) {
            CtTypeReference<?> newType = typeCasts.get(i);

            CtTypeReference<?> nextType = null;
            if (i + 1 < typeCasts.size()) {
                nextType = typeCasts.get(i + 1);
            }

            // One can not cast any primitive to any boxed type.
            //
            // For example, char -> Integer is not valid, only char -> int -> Integer is valid.
            // Therefore, if the current type is not the unboxed version of the new type, the cast is required.
            //
            // In this example:
            // char -> int -> Integer
            // nextType = Integer, currentType = char, newType = int
            //
            // The cast from char -> int is redundant, but then the cast would be char -> Integer, which is not valid.
            if (nextType != null && isBoxedType(nextType) && !currentType.equals(nextType.unbox())) {
                newCasts.add(newType);
                currentType = newType;
                continue;
            }

            if (canBeImplicitlyCastTo(currentType, newType)) {
                continue;
            }

            // Can not cast currentType -> newType implicitly
            // => the cast is narrowing like long -> int (might panic at runtime if the value is too large)
            //
            // Check if the next cast is narrowing as well, like int -> short, in which case the current cast can be
            // skipped: long -> int -> short => long -> short
            //
            // long -> int -> long can not be optimized, because that would change the runtime behavior

            if (nextType != null && !canBeImplicitlyCastTo(currentType, nextType)) {
                continue;
            }

            newCasts.add(newType);
            currentType = newType;
        }

        CtTypeReference<?> originalExpressionType = SpoonUtil.getExpressionType(ctExpression);
        CtTypeReference<?> newExpressionType = ctExpression.getType();
        if (!newCasts.isEmpty()) {
            newExpressionType = newCasts.get(newCasts.size() - 1);
        }

        // The type must not change, even if the cast might be implicit.
        //
        // For example, in the expression `(Byte) b` for `byte b`, the cast to `Byte` is redundant, but
        // the expression `b` has type `byte` and not `Byte`. Therefore, in those cases, a cast is added with
        // a special metadata marker to indicate that the cast is redundant.
        if (!originalExpressionType.equals(newExpressionType)) {
            CtTypeReference<?> preservingTypeCast = originalExpressionType.clone();
            preservingTypeCast.putMetadata("implicit", "true");
            newCasts.add(preservingTypeCast);
        }

        Collections.reverse(newCasts);
        ctExpression.setTypeCasts(newCasts);

        return ctExpression;
    }

    @Override
    public <T> CtExpression<T> foldCtExpression(CtExpression<T> ctExpression) {
        return removeRedundantCasts(ctExpression);
    }

}
