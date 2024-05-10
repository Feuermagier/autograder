package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
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

import java.util.Map;


@ExecutableCheck(reportedProblems = { ProblemType.METHOD_SHOULD_BE_STATIC })
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

        @Override
        public boolean matches(CtElement element) {
            return switch (element) {
                case CtFieldAccess<?> ctFieldAccess -> this.isThisTypeAccess(ctFieldAccess);
                case CtInvocation<?> ctInvocation -> this.isThisTypeAccess(ctInvocation);
                case CtExecutableReferenceExpression<?, ?> ctExecutableReferenceExpression -> this.isThisTypeAccess(ctExecutableReferenceExpression);
                default -> false;
            };
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
            if (SpoonUtil.isOverriddenMethod(ctMethod) || SpoonUtil.isOverridden(ctMethod)) {
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
                        ProblemType.METHOD_SHOULD_BE_STATIC
                    );
                }
            }
        });
    }

}
