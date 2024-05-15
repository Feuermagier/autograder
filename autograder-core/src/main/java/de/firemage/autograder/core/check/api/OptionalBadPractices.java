package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

@ExecutableCheck(reportedProblems = { ProblemType.OPTIONAL_TRI_STATE, ProblemType.OPTIONAL_ARGUMENT })
public class OptionalBadPractices extends IntegratedCheck {
    private void checkCtVariable(CtTypedElement<?> ctTypedElement) {
        CtTypeReference<?> ctTypeReference = ctTypedElement.getType();
        if (ctTypeReference == null || ctTypeReference.isImplicit() || !ctTypeReference.getPosition().isValidPosition()) {
            return;
        }

        if (!ctTypeReference.getQualifiedName().equals("java.util.Optional")) {
            return;
        }

        // Check if the variable is a function parameter:
        if (ctTypedElement instanceof CtParameter<?>) {
            this.addLocalProblem(
                ctTypeReference,
                new LocalizedMessage("optional-argument"),
                ProblemType.OPTIONAL_ARGUMENT
            );
        }

        // Check if the Optional is used as a tri-state:
        boolean isTriState =
            ctTypeReference.getActualTypeArguments().stream()
                           .anyMatch(x -> x.equals(ctTypeReference.getFactory().Type().createReference(java.lang.Boolean.class)));

        if (isTriState) {
            this.addLocalProblem(
                ctTypeReference,
                new LocalizedMessage("optional-tri-state"),
                ProblemType.OPTIONAL_TRI_STATE
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTypedElement<?>>() {
            @Override
            public void process(CtTypedElement<?> ctTypedElement) {
                if (ctTypedElement.isImplicit() || !ctTypedElement.getPosition().isValidPosition()) {
                    return;
                }

                if (ctTypedElement instanceof CtVariable<?> || ctTypedElement instanceof CtMethod<?>) {
                    checkCtVariable(ctTypedElement);
                }
            }
        });
    }
}
