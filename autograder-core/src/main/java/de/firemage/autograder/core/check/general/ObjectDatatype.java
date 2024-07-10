package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.OBJECT_DATATYPE })
public class ObjectDatatype extends IntegratedCheck {
    private static boolean hasObjectType(CtTypeReference<?> ctTypeReference) {
        return !ctTypeReference.isGenerics() && TypeUtil.isTypeEqualTo(ctTypeReference, java.lang.Object.class)
            || ctTypeReference.getActualTypeArguments().stream().anyMatch(ObjectDatatype::hasObjectType);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> ctVariable) {
                if (ctVariable.isImplicit() || !ctVariable.getPosition().isValidPosition()) {
                    return;
                }

                if (MethodUtil.isInOverridingMethod(ctVariable) || ctVariable.getType().isArray()) {
                    return;
                }

                if (hasObjectType(ctVariable.getType())) {
                    addLocalProblem(
                        ctVariable,
                        new LocalizedMessage(
                            "object-datatype",
                            Map.of("variable", ctVariable.getSimpleName())
                        ),
                        ProblemType.OBJECT_DATATYPE
                    );
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(4);
    }
}
