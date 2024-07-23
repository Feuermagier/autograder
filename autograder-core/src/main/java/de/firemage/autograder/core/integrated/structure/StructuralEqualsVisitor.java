package de.firemage.autograder.core.integrated.structure;

import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.CoreUtil;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTextBlock;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;
import spoon.support.visitor.equals.EqualsVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class StructuralEqualsVisitor extends EqualsVisitor {
    private static final boolean IS_IN_DEBUG_MODE = CoreUtil.isInDebugMode();

    private static final Set<CtRole> ALLOWED_MISMATCHING_ROLES = Set.of(
        // allow mismatching comments
        CtRole.COMMENT, CtRole.COMMENT_CONTENT, CtRole.COMMENT_TAG, CtRole.COMMENT_TYPE
    );

    private final Set<Difference> differences;

    public record Difference(CtRole role, Object left, Object right) {}

    public StructuralEqualsVisitor() {
        this.differences = new LinkedHashSet<>();
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

    private static <T> boolean isConstantExpressionOr(CtExpression<T> e, Predicate<? super CtExpression<?>> isAllowedExpression) {
        var visitor = new CtScanner() {
            private boolean isConstant = false;
            private boolean isDone = false;

            // use the exit instead of the enter method, so it checks the deepest nodes first.
            // This way, one can assume that when a <left> <op> <right> expression is encountered,
            // the <left> and <right> are already guaranteed to be constant.
            @Override
            protected void exit(CtElement ctElement) {
                if (!(ctElement instanceof CtExpression<?> expression) || this.isDone) {
                    return;
                }

                // Of all the Subinterfaces of CtExpression, these are irrelevant:
                // - CtAnnotation
                // - CtAnnotationFieldAccess
                // - CtOperatorAssignment
                // - CtArrayWrite
                // - CtArrayAccess
                // - CtFieldWrite
                // - CtFieldAccess
                // - CtVariableAccess
                // - CtVariableWrite
                // - CtCodeSnippetExpression (should never be encountered)
                //
                // these might be constant expressions, depending on more context:
                // - CtArrayRead
                // - CtAssignment
                // - CtConstructorCall
                // - CtExecutableReferenceExpression
                // - CtLambda
                // - CtNewArray
                // - CtNewClass
                // - CtSuperAccess
                // - CtSwitchExpression
                // - CtTargetedExpression
                // - CtThisAccess
                // - CtTypePattern
                //
                // and these are the relevant ones:
                // - CtLiteral
                // - CtUnaryOperator
                // - CtBinaryOperator
                // - CtFieldRead (if the field itself is static and final)
                // - CtInvocation (if the method is static and all arguments are constant expressions)
                // - CtTextBlock
                // - CtConditional (ternary operator)
                // - CtTypeAccess

                if (isInstanceOfAny(
                    expression,
                    CtBinaryOperator.class,
                    UnaryOperator.class,
                    CtTextBlock.class,
                    CtConditional.class,
                    CtTypeAccess.class
                )) {
                    this.isConstant = true;
                    return;
                }

                if (expression instanceof CtInvocation<?> ctInvocation && ctInvocation.getExecutable().isStatic()) {
                    // the arguments and target of the invocation have already been visited and all of them work in a constant context
                    // -> the invocation would work in a constant context as well
                    this.isConstant = true;
                    return;
                }

                if (SpoonUtil.resolveConstant(expression) instanceof CtLiteral<?> || isAllowedExpression.test(expression)) {
                    this.isConstant = true;
                } else {
                    this.isConstant = false;
                    this.isDone = true;
                }
            }
        };

        e.accept(visitor);

        return visitor.isConstant;
    }

    private static boolean isRefactorable(Object element) {
        if (!(element instanceof CtElement)) {
            return false;
        }

        if (element instanceof CtExpression<?> ctExpression) {
            return isConstantExpressionOr(ctExpression, e -> {
                // in addition to constant expressions, it is also okay to access parameters (if they have not been modified in the method)
                if (e instanceof CtVariableRead<?> ctVariableRead) {
                    CtVariable<?> ctVariable = SpoonUtil.getVariableDeclaration(ctVariableRead.getVariable());
                    return ctVariable instanceof CtParameter<?> ctParameter && SpoonUtil.isEffectivelyFinal(ctParameter);
                }

                return false;
            });
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

        // NOTE: element might be a collection of CtElements

        if (role == CtRole.NAME && isInstanceOfAny(element, CtLocalVariable.class, CtField.class, CtParameter.class)) {
            return true;
        }

        if ((role == CtRole.LEFT_OPERAND || role == CtRole.RIGHT_OPERAND) && isRefactorable(element)) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean fail(CtRole role, Object element, Object other) {
        this.differences.add(new Difference(role, element, other));

        if (shouldSkip(role, element)) {
            return false;
        }

        this.isNotEqual = true;
        this.notEqualRole = role;
        this.notEqualElement = element;
        this.notEqualOther = other;

        return true;
    }

    /**
     * Returns the differences between the two elements.
     *
     * @return the differences
     */
    public Set<Difference> differences() {
        return new LinkedHashSet<>(this.differences);
    }
}
