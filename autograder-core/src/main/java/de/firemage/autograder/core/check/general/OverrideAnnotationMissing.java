package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.OVERRIDE_ANNOTATION_MISSING })
public class OverrideAnnotationMissing extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtMethod<?>>() {
            @Override
            public void process(CtMethod<?> ctMethod) {
                if (ctMethod.isImplicit()
                    || !ctMethod.getPosition().isValidPosition()
                    || ctMethod.hasAnnotation(java.lang.Override.class)) {
                    return;
                }

                if (staticAnalysis.getCodeModel().getMethodHierarchy().isOverridingMethod(ctMethod)) {
                    addLocalProblem(
                        ctMethod,
                        new LocalizedMessage(
                            "missing-override",
                            Map.of("name", ctMethod.getSimpleName())
                        ),
                        ProblemType.OVERRIDE_ANNOTATION_MISSING
                    );
                }
            }
        });
    }
}
