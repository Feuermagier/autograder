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
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThrow;

@ExecutableCheck(reportedProblems = ProblemType.EXCEPTION_WITHOUT_MESSAGE)
public class ExceptionMessageCheck extends IntegratedCheck {
    public ExceptionMessageCheck() {
        super(new LocalizedMessage("exception-message-desc"));
    }

    private static boolean isExceptionWithoutMessage(CtExpression<?> expression) {
        return expression instanceof CtConstructorCall ctorCall
            && ExceptionUtil.isRuntimeException(ctorCall.getType())
            && ctorCall.getArguments().stream().noneMatch(e -> isNonBlankString((CtExpression<?>) e));
    }

    private static boolean isNonBlankString(CtExpression<?> expression) {
        if (!SpoonUtil.isString(expression.getType())) {
            return false;
        }

        return !(expression instanceof CtLiteral<?> literal) || !((String) literal.getValue()).isBlank();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtThrow>() {
            @Override
            public void process(CtThrow throwStmt) {
                if (isExceptionWithoutMessage(throwStmt.getThrownExpression())) {
                    addLocalProblem(throwStmt, new LocalizedMessage("exception-message-exp"),
                        ProblemType.EXCEPTION_WITHOUT_MESSAGE);
                }
            }
        });
    }
}
