package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = {
    ProblemType.CUSTOM_EXCEPTION_INHERITS_RUNTIME_EXCEPTION,
    ProblemType.CUSTOM_EXCEPTION_INHERITS_ERROR
})
public class CustomExceptionInheritanceCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctClass) {
                if (ctClass.isImplicit() || !ctClass.getPosition().isValidPosition()) {
                    return;
                }

                CtTypeReference<?> runtimeException = ctClass.getFactory().Type().createReference(RuntimeException.class);
                if (ctClass.isSubtypeOf(runtimeException)) {
                    addLocalProblem(
                        ctClass,
                        new LocalizedMessage("custom-exception-inheritance-runtime", Map.of("name", ctClass.getSimpleName())),
                        ProblemType.CUSTOM_EXCEPTION_INHERITS_RUNTIME_EXCEPTION
                    );
                }

                CtTypeReference<?> error = ctClass.getFactory().Type().createReference(Error.class);
                if (ctClass.isSubtypeOf(error)) {
                    addLocalProblem(
                        ctClass,
                        new LocalizedMessage("custom-exception-inheritance-error", Map.of("name", ctClass.getSimpleName())),
                        ProblemType.CUSTOM_EXCEPTION_INHERITS_ERROR
                    );
                }
            }
        });
    }
}
