package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.RUNTIME_EXCEPTION_CAUGHT})
public class RuntimeExceptionCatchCheck extends IntegratedCheck {
    private static final List<String> ALLOWED_EXCEPTIONS = List.of("java.lang.NumberFormatException");

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtCatch>() {
            @Override
            public void process(CtCatch ctCatch) {
                CtTypeReference<?> runtimeException = ctCatch.getFactory().createCtTypeReference(java.lang.RuntimeException.class);
                CtTypeReference<?> varType = ctCatch.getParameter().getType();


                List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctCatch.getBody());
                // catching an exception to throw another is okay
                if (statements.size() == 1 && statements.get(0) instanceof CtThrow) {
                    return;
                }

                if (varType.isSubtypeOf(runtimeException) && !ALLOWED_EXCEPTIONS.contains(varType.getQualifiedName())) {
                    addLocalProblem(
                        ctCatch.getParameter(),
                        new LocalizedMessage(
                            "runtime-exception-caught",
                            Map.of("exception", ctCatch.getParameter().getType().getSimpleName())
                        ),
                        ProblemType.RUNTIME_EXCEPTION_CAUGHT
                    );
                }
            }
        });
    }
}
