package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

@ExecutableCheck(reportedProblems = {ProblemType.EXPLICITLY_EXTENDS_OBJECT})
public class ExtendsObjectCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctClass) {
                CtTypeReference<?> ctTypeReference = ctClass.getSuperclass();
                if (ctTypeReference == null
                    || ctTypeReference.isImplicit()
                    || !ctTypeReference.getPosition().isValidPosition()) {
                    return;
                }

                if (ctClass.getFactory().Type().createReference(java.lang.Object.class).equals(ctTypeReference)) {
                    addLocalProblem(
                        ctTypeReference,
                        new LocalizedMessage("extends-object"),
                        ProblemType.EXPLICITLY_EXTENDS_OBJECT
                    );
                }
            }
        });
    }
}
