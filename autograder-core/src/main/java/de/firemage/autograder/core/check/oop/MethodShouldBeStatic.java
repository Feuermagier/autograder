package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.METHOD_SHOULD_BE_STATIC, ProblemType.METHOD_SHOULD_BE_STATIC_NOT_PUBLIC})
public class MethodShouldBeStatic extends IntegratedCheck {
    /**
     * This method checks if a given type member can be effectively static.
     * <p>
     * A type member is effectively static if it is already static or if it can be made static without
     * changing any other code.
     *
     * @param ctTypeMember the type member to check
     * @return true if the type member can be static
     */
    private static boolean isEffectivelyStatic(CtTypeMember ctTypeMember) {
        if (ctTypeMember.isStatic()) {
            return true;
        }

        // for interfaces and abstract methods there exist different rules (e.g. default methods should obviously not be static)
        if (ctTypeMember.getDeclaringType().isInterface() || ctTypeMember.isAbstract()) {
            return false;
        }

        // this handles the case where the type member is a method:
        if (ctTypeMember instanceof CtMethod<?> ctMethod) {
            // if the method is used in combination with inheritance (overriding a parent method or being overridden by a child method),
            // it cannot be static.
            if (MethodHierarchy.isOverridingMethod(ctMethod) || MethodHierarchy.isOverriddenMethod(ctMethod)) {
                return false;
            }

            // methods without a body can be static
            if (ctMethod.getBody() == null) {
                return true;
            }

            // this checks if the method body accesses this or super, in which case it cannot be static.
            //
            // one could call here the getElements method instead, but that would be less efficient, because it would
            // collect instances, and we only need to know if there is at least one instance.
            return ctMethod.getBody().filterChildren(ctElement -> ctElement instanceof CtThisAccess<?> || ctElement instanceof CtSuperAccess<?>).first() == null;
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
