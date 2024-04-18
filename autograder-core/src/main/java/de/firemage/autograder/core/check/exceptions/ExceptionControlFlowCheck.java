package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.EXCEPTION_CAUGHT_IN_SURROUNDING_BLOCK, ProblemType.EXCEPTION_SHOULD_NEVER_BE_CAUGHT})
public class ExceptionControlFlowCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtTry(CtTry ctTry) {
                Collection<CtTypeReference<?>> thrownExceptions = new ArrayList<>();
                ctTry.getBody().accept(new CtScanner() {
                    @Override
                    public void visitCtThrow(CtThrow throwStatement) {
                        thrownExceptions.add(throwStatement.getThrownExpression().getType());
                        super.visitCtThrow(throwStatement);
                    }
                });

                for (CtCatch catchBlock : ctTry.getCatchers()) {
                    var caughtException = catchBlock.getParameter().getType();
                    if (thrownExceptions.stream()
                        .anyMatch(e -> e.getQualifiedName().equals(caughtException.getQualifiedName()))
                        || thrownExceptions.stream().anyMatch(e -> e.isSubtypeOf(caughtException))) {
                        addLocalProblem(
                            catchBlock.getParameter(),
                            new LocalizedMessage(
                                "exception-controlflow-caught",
                                Map.of("exception", caughtException.getSimpleName())
                            ),
                            ProblemType.EXCEPTION_CAUGHT_IN_SURROUNDING_BLOCK
                        );
                    }
                }

                super.visitCtTry(ctTry);
            }

            @Override
            public void visitCtCatch(CtCatch ctCatch) {
                if (ctCatch.isImplicit() || !ctCatch.getPosition().isValidPosition()) {
                    super.visitCtCatch(ctCatch);
                    return;
                }

                for (CtTypeReference<?> ctTypeReference : ctCatch.getParameter().getMultiTypes()) {
                    if (SpoonUtil.isTypeEqualTo(
                        ctTypeReference,
                        java.lang.NullPointerException.class,
                        java.lang.ArithmeticException.class,
                        java.lang.ArrayIndexOutOfBoundsException.class,
                        java.lang.ArrayStoreException.class,
                        java.lang.ClassCastException.class,
                        java.lang.IndexOutOfBoundsException.class,
                        java.lang.NegativeArraySizeException.class,
                        // I think those two are covered by artemis:
                        // java.lang.RuntimeException.class,
                        // java.lang.Exception.class,
                        java.lang.StringIndexOutOfBoundsException.class
                    ) || ctTypeReference.isSubtypeOf(ctCatch.getFactory().createCtTypeReference(java.lang.Error.class))) {
                        addLocalProblem(
                            ctCatch.getParameter(),
                            new LocalizedMessage(
                                "exception-controlflow-should-not-be-caught",
                                Map.of("exception", ctTypeReference.getSimpleName())
                            ),
                            ProblemType.EXCEPTION_SHOULD_NEVER_BE_CAUGHT
                        );
                    }
                }

                super.visitCtCatch(ctCatch);
            }
        });
    }
}
