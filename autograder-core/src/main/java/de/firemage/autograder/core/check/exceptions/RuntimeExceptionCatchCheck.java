package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ExceptionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;

import java.util.List;

public class RuntimeExceptionCatchCheck extends IntegratedCheck {
    public RuntimeExceptionCatchCheck() {
        super("Never catch runtime exceptions (aside from NumberFormatException)");
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtCatch>() {
            @Override
            public void process(CtCatch catchBlock) {
                var varType = catchBlock.getParameter().getType();
                if (ExceptionUtil.isRuntimeException(varType) || ExceptionUtil.isError(varType)) {
                    addLocalProblem(catchBlock, catchBlock.getParameter().getType().getSimpleName() + " caught");
                }
            }
        });
    }
}
