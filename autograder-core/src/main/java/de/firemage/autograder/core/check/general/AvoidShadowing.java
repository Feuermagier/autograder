package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


@ExecutableCheck(reportedProblems = { ProblemType.AVOID_SHADOWING })
public class AvoidShadowing extends IntegratedCheck {
    public AvoidShadowing() {
        super(new LocalizedMessage("avoid-shadowing"));
    }

    private static Collection<CtFieldReference<?>> getAllVisibleFields(CtTypeInformation ctTypeInformation) {

        Collection<CtFieldReference<?>> result = new ArrayList<>(ctTypeInformation.getDeclaredFields());

        CtTypeReference<?> parent = ctTypeInformation.getSuperclass();
        while (parent != null) {
            result.addAll(
                parent.getDeclaredFields()
                      .stream()
                      // only non-private fields are visible to a subclass
                      .filter(ctFieldReference -> !ctFieldReference.getFieldDeclaration().isPrivate())
                      .toList()
            );

            parent = parent.getSuperclass();
        }

        return result;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> ctVariable) {
                // skip fields inside constructors
                if (ctVariable.getParent(CtConstructor.class) != null) {
                    return;
                }

                CtType<?> parent = ctVariable.getParent(CtType.class);

                if (parent == null) {
                    return;
                }

                Collection<CtFieldReference<?>> visibleFields = getAllVisibleFields(parent);

                for (CtFieldReference<?> ctFieldReference : visibleFields) {
                    if (ctFieldReference.getSimpleName().equals(ctVariable.getSimpleName())
                        && !ctFieldReference.getDeclaration().equals(ctVariable)) {
                        addLocalProblem(
                            ctVariable,
                            new LocalizedMessage("avoid-shadowing", Map.of("name", ctVariable.getSimpleName())),
                            ProblemType.AVOID_SHADOWING
                        );
                    }
                }
            }
        });
    }
}
