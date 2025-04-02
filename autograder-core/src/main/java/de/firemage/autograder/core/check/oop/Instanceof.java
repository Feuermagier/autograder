package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;

import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.INSTANCEOF, ProblemType.INSTANCEOF_EMULATION })
public class Instanceof extends IntegratedCheck {
    private static final Set<String> FORBIDDEN_CLASS_METHODS = Set.of(
        "descriptorString",
        "getCanonicalName",
        "getName",
        "getSimpleName",
        "getTypeName",
        "isAssignableFrom",
        "isInstance"
        /*, "toString" */
    );

    private static boolean isInAllowedContext(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        return ctMethod != null && MethodHierarchy.isOverridingMethod(ctMethod);
    }

    private static boolean isInstanceofEmulation(CtInvocation<?> ctInvocation) {
        CtExecutableReference<?> ctExecutableReference = ctInvocation.getExecutable();

        // check if Object#getClass() is called, which would return a Class object.
        //
        // This uses the subtype method, because the type of the reference might be a different generic type
        // than the one in the literal
        if (TypeUtil.isSubtypeOf(ctExecutableReference.getType(), java.lang.Class.class)
            && ctExecutableReference.getSimpleName().equals("getClass")) {
            return true;
        }

        return ctExecutableReference.getDeclaringType() != null &&
            TypeUtil.isSubtypeOf(ctExecutableReference.getDeclaringType(), Class.class) &&
            FORBIDDEN_CLASS_METHODS.contains(ctExecutableReference.getSimpleName());

    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtTry(CtTry ctTry) {
                if (ctTry.isImplicit() || !ctTry.getPosition().isValidPosition() || isInAllowedContext(ctTry)) {
                    super.visitCtTry(ctTry);
                    return;
                }

                for (CtCatch ctCatch : ctTry.getCatchers()) {
                    if (ctCatch.getParameter().getType().equals(ctCatch.getFactory().Type().createReference(java.lang.ClassCastException.class))) {
                        addLocalProblem(
                            ctCatch,
                            new LocalizedMessage("do-not-use-instanceof-emulation"),
                            ProblemType.INSTANCEOF_EMULATION
                        );
                    }
                }

                super.visitCtTry(ctTry);
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> ctInvocation) {
                if (ctInvocation.isImplicit() || !ctInvocation.getPosition().isValidPosition() || isInAllowedContext(ctInvocation)) {
                    super.visitCtInvocation(ctInvocation);
                    return;
                }

                if (isInstanceofEmulation(ctInvocation)) {
                    addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage("do-not-use-instanceof-emulation"),
                        ProblemType.INSTANCEOF_EMULATION
                    );
                }

                super.visitCtInvocation(ctInvocation);
            }

            @Override
            public <T> void visitCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit() || !ctBinaryOperator.getPosition().isValidPosition() || isInAllowedContext(ctBinaryOperator)) {
                    super.visitCtBinaryOperator(ctBinaryOperator);
                    return;
                }

                if (ctBinaryOperator.getKind() == BinaryOperatorKind.INSTANCEOF) {
                    addLocalProblem(
                        ctBinaryOperator,
                        new LocalizedMessage("do-not-use-instanceof"),
                        ProblemType.INSTANCEOF
                    );
                }
                super.visitCtBinaryOperator(ctBinaryOperator);
            }
        });
    }
}
