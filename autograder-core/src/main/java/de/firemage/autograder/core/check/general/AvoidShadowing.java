package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@ExecutableCheck(reportedProblems = { ProblemType.AVOID_SHADOWING })
public class AvoidShadowing extends IntegratedCheck {
    private static final List<String> ALLOWED_FIELDS = List.of("serialVersionUID");

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

                // skip fields inside overridden methods
                if (SpoonUtil.isInOverriddenMethod(ctVariable)) {
                    return;
                }

                // skip variables inside static methods
                CtMethod<?> ctMethod = ctVariable.getParent(CtMethod.class);
                if (ctMethod != null && ctMethod.isStatic()) {
                    return;
                }

                CtType<?> parent = ctVariable.getParent(CtType.class);

                if (parent == null) {
                    return;
                }

                Collection<CtFieldReference<?>> visibleFields = getAllVisibleFields(parent);

                for (CtFieldReference<?> ctFieldReference : visibleFields) {
                    if (ALLOWED_FIELDS.contains(ctFieldReference.getSimpleName()) || ctVariable.getReference() == null) {
                        continue;
                    }

                    if (ctFieldReference.getSimpleName().equals(ctVariable.getSimpleName())
                        && !ctFieldReference.equals(ctVariable.getReference())) {
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
