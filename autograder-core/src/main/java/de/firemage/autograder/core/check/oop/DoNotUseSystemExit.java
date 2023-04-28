package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;

@ExecutableCheck(reportedProblems = { ProblemType.DO_NOT_USE_SYSTEM_EXIT })
public class DoNotUseSystemExit extends IntegratedCheck {
    private boolean hasInvokedSystemExit(CtInvocation<?> ctInvocation) {
        // System.exit(int) is a CtInvocation of the method exit(int)
        // The target of the invocation is System, which is a CtTypeAccess
        // The executable is exit(int) which has the simple name "exit"
        return ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
                // ensure the method is called on java.lang.System
                && ctInvocation.getFactory().Type().createReference(java.lang.System.class)
                               .equals(ctTypeAccess.getAccessedType())
                && ctInvocation.getExecutable().getSimpleName().equals("exit");
    }

    private void checkCtInvocation(CtInvocation<?> ctInvocation) {
        if (this.hasInvokedSystemExit(ctInvocation)) {
            this.addLocalProblem(
                ctInvocation,
                new LocalizedMessage("do-not-use-system-exit"),
                ProblemType.DO_NOT_USE_SYSTEM_EXIT
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                checkCtInvocation(ctInvocation);
            }
        });
    }
}
