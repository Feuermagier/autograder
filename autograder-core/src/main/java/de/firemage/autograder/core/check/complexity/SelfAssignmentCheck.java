package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_SELF_ASSIGNMENT })
public class SelfAssignmentCheck extends IntegratedCheck {
    private static CtTypeReference<?> findDeclaredType(CtFieldReference<?> ctFieldReference, CtTypeReference<?> ctTypeReference) {
        CtTypeReference<?> current = ctTypeReference;

        while (current != null) {
            if (current.getDeclaredFields().contains(ctFieldReference)) {
                return current;
            }

            current = current.getSuperclass();
        }

        return null;
    }

    // There is a problem with the code `super.a = a`
    // The `a` will implicitly have a target of `this`, so the targets are not equal
    //
    // Workaround for https://github.com/INRIA/spoon/issues/5221
    private static <T> void adjustTarget(CtFieldAccess<T> ctFieldAccess) {
        if (!(ctFieldAccess.getTarget() instanceof CtThisAccess<?> ctThisAccess)) {
            return;
        }

        // check if the field is declared in the class
        if (ctThisAccess.getType().getDeclaredFields().contains(ctFieldAccess.getVariable())) {
            return;
        }

        // if not it is declared in a parent class
        // => change the target to super
        CtSuperAccess<?> ctSuperAccess = ctFieldAccess.getFactory().createSuperAccess();
        ctSuperAccess.setImplicit(true);
        ctSuperAccess.setTarget(null);
        CtVariableReference ctVariableReference = ctFieldAccess.getFactory().createLocalVariableReference();
        ctVariableReference.setSimpleName("");
        ctVariableReference.setParent(ctSuperAccess);
        ctVariableReference.setImplicit(false);
        ctVariableReference.setType(findDeclaredType(ctFieldAccess.getVariable(), ctThisAccess.getType()));

        ctSuperAccess.setVariable(ctVariableReference);
        ctSuperAccess.setType(null);
        ctSuperAccess.setParent(ctFieldAccess);

        ctFieldAccess.setTarget(ctSuperAccess);
    }

    private static boolean isSelfAssignment(CtVariableWrite<?> lhs, CtVariableRead<?> rhs) {
        if (lhs instanceof CtFieldWrite<?> left && rhs instanceof CtFieldRead<?> right) {
            // Special case for assignment to fields (or super fields)
            //
            // For the code:
            // ```
            // this.a = other.a
            // ```
            //
            // `getVariable()` will return for both "a"
            // => they are considered equal even though they are not
            //
            // in order to check for equality, one has to check the target as well

            adjustTarget(left);
            adjustTarget(right);

            return left.getTarget().equals(right.getTarget())
                && lhs.getVariable().equals(rhs.getVariable());
        }

        return lhs.getVariable().equals(rhs.getVariable());
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssignment<?, ?>>() {
            @Override
            public void process(CtAssignment<?, ?> assignment) {
                CtExpression<?> lhs = assignment.getAssigned();
                CtExpression<?> rhs = assignment.getAssignment();

                if (!(rhs instanceof CtVariableRead<?> read) ||
                    !(lhs instanceof CtVariableWrite<?> write)) {
                    return;
                }

                if (isSelfAssignment(write, read)) {
                    addLocalProblem(assignment,
                        new LocalizedMessage(
                            "self-assignment-exp",
                            Map.of(
                                "lhs", lhs,
                                "rhs", rhs
                            )
                        ), ProblemType.REDUNDANT_SELF_ASSIGNMENT
                    );
                }
            }
        });
    }
}
