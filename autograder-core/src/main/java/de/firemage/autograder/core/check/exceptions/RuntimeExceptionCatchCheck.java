package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ExceptionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.RUNTIME_EXCEPTION_OR_ERROR_CAUGHT})
public class RuntimeExceptionCatchCheck extends IntegratedCheck {
    public RuntimeExceptionCatchCheck() {
        super(new LocalizedMessage("runtime-ex-caught-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtCatch>() {
            @Override
            public void process(CtCatch catchBlock) {
                var varType = catchBlock.getParameter().getType();
                if (ExceptionUtil.isRuntimeException(varType) || ExceptionUtil.isError(varType)) {
                    addLocalProblem(catchBlock, new LocalizedMessage("runtime-ex-caught-exp",
                            Map.of("exp", catchBlock.getParameter().getType().getSimpleName())),
                        ProblemType.RUNTIME_EXCEPTION_OR_ERROR_CAUGHT);
                }
            }
        });
    }
}
