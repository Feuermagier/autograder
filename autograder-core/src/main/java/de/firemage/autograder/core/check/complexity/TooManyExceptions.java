package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.TOO_MANY_EXCEPTIONS })
public class TooManyExceptions extends IntegratedCheck {
    private static final int MAXIMUM_NUMBER_OF_EXCEPTIONS = 5;

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        List<CtClass<?>> declaredExceptions = new ArrayList<>();

        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctClass) {
                if (ctClass.isImplicit() || !ctClass.getPosition().isValidPosition()) {
                    return;
                }

                if (TypeUtil.isSubtypeOf(ctClass.getReference(), java.lang.Exception.class)) {
                    declaredExceptions.add(ctClass);
                }
            }
        });

        if (declaredExceptions.size() > MAXIMUM_NUMBER_OF_EXCEPTIONS) {
            addLocalProblem(
                declaredExceptions.get(0),
                new LocalizedMessage(
                    "too-many-exceptions",
                    Map.of("count", declaredExceptions.size())
                ),
                ProblemType.TOO_MANY_EXCEPTIONS
            );
        }
    }
}
