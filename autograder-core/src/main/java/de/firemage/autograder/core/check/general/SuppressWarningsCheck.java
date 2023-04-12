package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtAnnotation;

@ExecutableCheck(reportedProblems = {ProblemType.SUPPRESS_WARNINGS_USED})
public class SuppressWarningsCheck extends IntegratedCheck {

    public SuppressWarningsCheck() {
        super(new LocalizedMessage("suppress-warnings"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAnnotation<?>>() {
            @Override
            public void process(CtAnnotation<?> annotation) {
                if (annotation.getAnnotationType().getQualifiedName().equals("java.lang.SuppressWarnings")) {
                    addLocalProblem(
                            annotation,
                            new LocalizedMessage("suppress-warnings"),
                            ProblemType.SUPPRESS_WARNINGS_USED
                    );
                }
            }
        });
    }
}
