package de.firemage.autograder.core.integrated.structure;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.path.CtRole;
import spoon.support.visitor.equals.EqualsVisitor;

import java.util.Set;

public final class StructuralEqualsVisitor extends EqualsVisitor {
    private static final boolean IS_IN_DEBUG_MODE = SpoonUtil.isInJunitTest();

    private static final Set<CtRole> ALLOWED_MISMATCHING_ROLES = Set.of(
        // allow mismatching comments
        CtRole.COMMENT, CtRole.COMMENT_CONTENT, CtRole.COMMENT_TAG, CtRole.COMMENT_TYPE
    );

    public StructuralEqualsVisitor() {
    }

    public static boolean equals(CtElement left, CtElement right) {
        return new StructuralEqualsVisitor().checkEquals(left, right);
    }

    @Override
    public boolean checkEquals(CtElement left, CtElement right) {
        if (IS_IN_DEBUG_MODE) {
            boolean result = super.checkEquals(left, right);
            int leftHashCode = StructuralHashCodeVisitor.computeHashCode(left);
            int rightHashCode = StructuralHashCodeVisitor.computeHashCode(right);

            // If two objects are equal according to the equals(Object) method, then calling
            // the hashCode method on each of the two objects must produce the same integer result.
            if (result && leftHashCode != rightHashCode) {
                throw new IllegalStateException("StructuralHashCode is wrong for the equal objects: %s (hashCode=%d), %s (hashCode=%d)".formatted(left, leftHashCode, right, rightHashCode));
            }

            return result;
        }

        return super.checkEquals(left, right);
    }

    private static boolean isRefactorable(Object element) {
        if (!(element instanceof CtElement)) {
            return false;
        }

        if (element instanceof CtExpression<?> ctExpression) {
            // TODO: check for constant expression or simple variable access?
            return true;
        }

        return false;
    }

    private static boolean isInstanceOfAny(Object object, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(object)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldSkip(CtRole role, Object element) {
        if (role == null) {
            return false;
        }

        if (ALLOWED_MISMATCHING_ROLES.contains(role)) {
            return true;
        }

        // TODO: element can be collections???

        if (role == CtRole.NAME && isInstanceOfAny(element, CtLocalVariable.class, CtField.class, CtParameter.class)) {
            return true;
        }

        // TODO: LEFT_OPERAND as well?
        if ((role == CtRole.RIGHT_OPERAND || role == CtRole.LEFT_OPERAND) && isRefactorable(element)) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean fail(CtRole role, Object element, Object other) {
        if (shouldSkip(role, element)) {
            return false;
        }

        isNotEqual = true;
        notEqualRole = role;
        notEqualElement = element;
        notEqualOther = other;

        return true;
    }
}
