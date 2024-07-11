package de.firemage.autograder.core.check.debug;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;

@ExecutableCheck(reportedProblems = {ProblemType.EXCEPTION_PRINT_STACK_TRACE})
public class PrintStackTraceCheck extends IntegratedCheck {
    private static boolean hasInvokedPrintStackTrace(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtVariableRead<?> ctVariableRead
            // workaround for https://github.com/INRIA/spoon/issues/5414
            && ctVariableRead.getType().getTypeDeclaration() != null
            // ensure the method is called on the correct type
            && SpoonUtil.isSubtypeOf(ctVariableRead.getType(), java.lang.Throwable.class)
            && ctInvocation.getExecutable().getSimpleName().equals("printStackTrace");
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                if (hasInvokedPrintStackTrace(ctInvocation)) {
                    addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage("print-stack-trace"),
                        ProblemType.EXCEPTION_PRINT_STACK_TRACE
                    );
                }
            }
        });
    }
}
