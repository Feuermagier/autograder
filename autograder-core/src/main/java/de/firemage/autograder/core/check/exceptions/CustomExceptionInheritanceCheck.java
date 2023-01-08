package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ExceptionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;

@ExecutableCheck(reportedProblems = {ProblemType.CUSTOM_EXCEPTION_INHERITS_RUNTIME_EXCEPTION,
    ProblemType.CUSTOM_EXCEPTION_INHERITS_ERROR})
public class CustomExceptionInheritanceCheck extends IntegratedCheck {
    public CustomExceptionInheritanceCheck() {
        super(new LocalizedMessage("custom-exception-inheritance-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> clazz) {
                if (clazz.getSuperclass() == null) {
                    return;
                }

                if (ExceptionUtil.isRuntimeException(clazz.getSuperclass())) {
                    addLocalProblem(clazz, new LocalizedMessage("custom-exception-inheritance-exp-runtime"),
                        ProblemType.CUSTOM_EXCEPTION_INHERITS_RUNTIME_EXCEPTION);
                }

                if (ExceptionUtil.isError(clazz.getSuperclass())) {
                    addLocalProblem(clazz, new LocalizedMessage("custom-exception-inheritance-exp-error"),
                        ProblemType.CUSTOM_EXCEPTION_INHERITS_ERROR);
                }
            }
        });
    }
}
