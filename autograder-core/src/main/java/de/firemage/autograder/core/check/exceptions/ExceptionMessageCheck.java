package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;

@ExecutableCheck(reportedProblems = ProblemType.EXCEPTION_WITHOUT_MESSAGE)
public class ExceptionMessageCheck extends IntegratedCheck {
    private static boolean isExceptionWithoutMessage(CtExpression<?> expression) {
        if (!(expression instanceof CtConstructorCall<?> ctConstructorCall)
            || !SpoonUtil.isSubtypeOf(expression.getType(), java.lang.Exception.class)) {
            return false;
        }

        // check if the invoked constructor passes a message to the parent exception like this:
        // class MyException extends Exception { MyException() { super("here is the message"); } }
        if (ctConstructorCall.getExecutable().getExecutableDeclaration() instanceof CtConstructor<?> ctConstructor
            && ctConstructor.getBody().filterChildren(ctElement -> ctElement instanceof CtInvocation<?> ctInvocation
                // we just check if there is any invocation with a message, because this is easier and might be enough
                // for most cases.
                //
                // this way will not result in false positives, only in false negatives
                && ctInvocation.getExecutable().isConstructor()
                && hasMessage(ctInvocation.getArguments())
            ).first() != null) {
            return false;
        }

        return !hasMessage(ctConstructorCall.getArguments());
    }

    private static boolean hasMessage(Iterable<? extends CtExpression<?>> arguments) {
        for (CtExpression<?> ctExpression : arguments) {
            // consider a passed throwable as having message
            if (SpoonUtil.isSubtypeOf(ctExpression.getType(), java.lang.Throwable.class)) {
                return true;
            }

            String literal = SpoonUtil.tryGetStringLiteral(ctExpression).orElse(null);

            return literal == null || !literal.isBlank();
        }

        return false;
    }

    private static boolean isInAllowedContext(CtElement ctElement) {
        // allow exceptions without messages in utility classes:
        //
        // private MyUtilityClass {
        //   throw new UnsupportedOperationException();
        // }
        CtConstructor<?> ctConstructor = ctElement.getParent(CtConstructor.class);
        if (ctConstructor != null && ctConstructor.isPrivate()) {
            return true;
        }

        CtCase<?> ctCase = ctElement.getParent(CtCase.class);
        // allow no message in default case of switch (most likely used as an unreachable default case)
        // See: https://github.com/Feuermagier/autograder/issues/82
        return ctCase != null && ctCase.getCaseExpressions().isEmpty();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtThrow>() {
            @Override
            public void process(CtThrow throwStmt) {
                if (isExceptionWithoutMessage(throwStmt.getThrownExpression()) && !isInAllowedContext(throwStmt)) {
                    addLocalProblem(
                        throwStmt,
                        new LocalizedMessage("exception-message"),
                        ProblemType.EXCEPTION_WITHOUT_MESSAGE
                    );
                }
            }
        });
    }
}
