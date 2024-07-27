package de.firemage.autograder.core.integrated;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.Optional;

public final class VariableUtil {
    private VariableUtil() {
    }

    public static boolean isEffectivelyFinal(CtVariable<?> ctVariable) {
        if (ctVariable.getModifiers().contains(ModifierKind.FINAL)) {
            return true;
        }

        return UsesFinder.variableUses(ctVariable).ofType(CtVariableWrite.class).hasNone();
    }

    public static <T> Optional<CtExpression<T>> getEffectivelyFinalExpression(CtVariable<T> ctVariable) {
        if (!isEffectivelyFinal(ctVariable)) {
            return Optional.empty();
        }

        return Optional.ofNullable(ctVariable.getDefaultExpression());
    }

    public static CtElement getReferenceDeclaration(CtReference ctReference) {
        // this might be null if the reference is not in the source path
        // for example, when the reference points to a java.lang type
        CtElement target = ctReference.getDeclaration();

        if (target == null && ctReference instanceof CtTypeReference<?> ctTypeReference) {
            target = ctTypeReference.getTypeDeclaration();
        }

        if (target == null && ctReference instanceof CtExecutableReference<?> ctExecutableReference) {
            target = ctExecutableReference.getExecutableDeclaration();
        }

        if (target == null && ctReference instanceof CtVariableReference<?> ctVariableReference) {
            target = getVariableDeclaration(ctVariableReference);
        }

        return target;
    }

    public static CtVariable<?> getVariableDeclaration(CtVariableReference<?> ctVariableReference) {
        // this might be null if the reference is not in the source path
        // for example, when the reference points to a java.lang type
        CtVariable<?> target = ctVariableReference.getDeclaration();

        if (target == null && ctVariableReference instanceof CtFieldReference<?> ctFieldReference) {
            target = ctFieldReference.getFieldDeclaration();
        }

        return target;
    }
}
