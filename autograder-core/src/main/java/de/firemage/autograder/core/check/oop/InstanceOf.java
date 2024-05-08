package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;

@ExecutableCheck(reportedProblems = { ProblemType.INSTANCEOF, ProblemType.INSTANCEOF_EMULATION })
public class InstanceOf extends IntegratedCheck {
    private static boolean isInAllowedContext(CtElement ctElement, CodeModel model) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        return ctMethod != null && MethodHierarchy.isOverridingMethod(ctMethod);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        CodeModel model = staticAnalysis.getCodeModel();
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtTry(CtTry ctTry) {
                if (ctTry.isImplicit() || !ctTry.getPosition().isValidPosition() || isInAllowedContext(ctTry, model)) {
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
                if (ctInvocation.isImplicit() || !ctInvocation.getPosition().isValidPosition() || isInAllowedContext(ctInvocation, model)) {
                    super.visitCtInvocation(ctInvocation);
                    return;
                }

                CtExecutableReference<?> ctExecutableReference = ctInvocation.getExecutable();

                if (ctExecutableReference.getType().getQualifiedName().equals("java.lang.Class")
                        && ctExecutableReference.getSimpleName().equals("getClass")) {
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
                if (ctBinaryOperator.isImplicit() || !ctBinaryOperator.getPosition().isValidPosition() || isInAllowedContext(ctBinaryOperator, model)) {
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
