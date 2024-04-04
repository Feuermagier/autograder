package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ExceptionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;

import java.util.List;

@ExecutableCheck(reportedProblems = ProblemType.EXCEPTION_WITHOUT_MESSAGE)
public class ExceptionMessageCheck extends IntegratedCheck {
    private static boolean isExceptionWithoutMessage(CtExpression<?> expression) {
        return expression instanceof CtConstructorCall<?> ctorCall
            && ExceptionUtil.isRuntimeException(ctorCall.getType())
            && !hasMessage(ctorCall.getArguments());
    }

    private static boolean hasMessage(List<? extends CtExpression<?>> arguments) {
        if (arguments.isEmpty()) {
            return false;
        }

        CtExpression<?> ctExpression = arguments.get(0);
        String literal = SpoonUtil.tryGetStringLiteral(ctExpression).orElse(null);

        if (literal != null) {
            return !literal.isBlank();
        }

        return true;
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
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
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
