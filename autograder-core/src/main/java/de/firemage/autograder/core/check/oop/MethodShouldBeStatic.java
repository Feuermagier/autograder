package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.CoreUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.visitor.Filter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.METHOD_SHOULD_BE_STATIC, ProblemType.METHOD_SHOULD_BE_STATIC_NOT_PUBLIC})
public class MethodShouldBeStatic extends IntegratedCheck {
    /**
     * Finds elements that access the given type through this. Accessing the same type through a different object is ok.
     *
     * @param ctType the type to be accessed
     */
    private record ThisAccessFilter(CtType<?> ctType) implements Filter<CtElement> {
        private boolean isSuperTypeAccess(CtTargetedExpression<?, ?> ctTargetedExpression) {
            return ctTargetedExpression.getTarget() instanceof CtSuperAccess<?> superAccess
                && this.ctType.isSubtypeOf(superAccess.getType());
        }

        private boolean isThisTypeAccess(CtTargetedExpression<?, ?> ctTargetedExpression) {
            if (this.isSuperTypeAccess(ctTargetedExpression)) {
                return true;
            }

            return ctTargetedExpression.getTarget() instanceof CtThisAccess<?> thisAccess
                && thisAccess.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
                && this.ctType.equals(ctTypeAccess.getAccessedType().getTypeDeclaration());
        }

        private static final List<Class<?>> SUPPORTED_TYPES = List.of(
            CtFieldAccess.class,
            CtInvocation.class,
            CtExecutableReferenceExpression.class
        );

        @Override
        public boolean matches(CtElement element) {
            // The following types can be a targeted expression:
            // - CtArrayAccess
            // - CtConstructorCall
            // - CtExecutableReferenceExpression
            // - CtFieldAccess
            // - CtInvocation
            // - CtNewClass
            // - CtSuperAccess
            // - CtThisAccess
            if (element instanceof CtTargetedExpression<?,?> ctTargetedExpression && CoreUtil.isInstanceOfAny(ctTargetedExpression, SUPPORTED_TYPES)) {
                return this.isThisTypeAccess(ctTargetedExpression);
            }

            return element instanceof CtThisAccess<?> ctThisAccess
                && ctThisAccess.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
                && this.ctType.equals(ctTypeAccess.getAccessedType().getTypeDeclaration())
                && !CoreUtil.isInstanceOfAny(element.getParent(), SUPPORTED_TYPES);
        }
    }

    private static boolean isEffectivelyStatic(CtTypeMember ctTypeMember) {
        if (ctTypeMember.isStatic()) {
            return true;
        }

        if (ctTypeMember.getDeclaringType().isInterface() || ctTypeMember.isAbstract()) {
            return false;
        }

        if (ctTypeMember instanceof CtMethod<?> ctMethod) {
            if (MethodHierarchy.isOverridingMethod(ctMethod) || MethodHierarchy.isOverriddenMethod(ctMethod)) {
                return false;
            }

            if (ctMethod.getBody() == null) {
                return true;
            }

            return ctMethod.getBody().filterChildren(new ThisAccessFilter(ctTypeMember.getDeclaringType())).first() == null;
        }

        return false;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtMethod<?>>() {
            @Override
            public void process(CtMethod<?> ctMethod) {
                if (ctMethod.isImplicit() || !ctMethod.getPosition().isValidPosition()) {
                    return;
                }

                if (!ctMethod.isStatic() && isEffectivelyStatic(ctMethod)) {
                    addLocalProblem(
                        ctMethod,
                        new LocalizedMessage(
                            "method-should-be-static",
                            Map.of("name", ctMethod.getSimpleName())
                        ),
                        ctMethod.isPublic() ? ProblemType.METHOD_SHOULD_BE_STATIC : ProblemType.METHOD_SHOULD_BE_STATIC_NOT_PUBLIC
                    );
                }
            }
        });
    }

}
