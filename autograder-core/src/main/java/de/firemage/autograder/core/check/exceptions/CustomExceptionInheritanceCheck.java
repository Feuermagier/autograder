package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ExceptionUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;

public class CustomExceptionInheritanceCheck extends IntegratedCheck {
    public CustomExceptionInheritanceCheck() {
        super("Custom exceptions should not extend from RuntimeException or Error");
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
                    addLocalProblem(clazz, "Custom exceptions should be checked exceptions",
                        ProblemType.CUSTOM_EXCEPTION_INHERITS_RUNTIME_EXCEPTION);
                }

                if (ExceptionUtil.isError(clazz.getSuperclass())) {
                    addLocalProblem(clazz, "Custom exceptions should not inherit from Error",
                        ProblemType.CUSTOM_EXCEPTION_INHERITS_ERROR);
                }
            }
        });
    }
}
