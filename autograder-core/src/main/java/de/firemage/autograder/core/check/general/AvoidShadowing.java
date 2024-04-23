package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.uses.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
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


@ExecutableCheck(reportedProblems = {ProblemType.AVOID_SHADOWING})
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
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> ctVariable) {
                // skip fields inside constructors
                if (ctVariable.getParent(CtConstructor.class) != null) {
                    return;
                }

                // skip fields inside overridden methods
                if (SpoonUtil.isInOverriddenMethod(ctVariable) || SpoonUtil.isInSetter(ctVariable)) {
                    return;
                }

                // skip variables inside static methods
                CtMethod<?> ctMethod = ctVariable.getParent(CtMethod.class);
                if (ctMethod != null && ctMethod.isStatic()) {
                    return;
                }

                CtType<?> parent = ctVariable.getParent(CtType.class);
                if (parent == null || ctVariable.getReference() == null) {
                    return;
                }

                Collection<CtFieldReference<?>> visibleFields = getAllVisibleFields(parent);

                List<CtFieldReference<?>> hiddenFields = visibleFields.stream()
                    // ignore fields that are allowed to be hidden
                    .filter(ctFieldReference -> !ALLOWED_FIELDS.contains(ctFieldReference.getSimpleName()))
                    // only keep fields that have the same name as the variable, but are not the same field
                    .filter(ctFieldReference -> ctFieldReference.getSimpleName().equals(ctVariable.getSimpleName())
                        && !ctFieldReference.equals(ctVariable.getReference()))
                    .toList();

                // if there are no fields hidden by the variable, skip them
                if (hiddenFields.isEmpty()) {
                    return;
                }

                CtElement variableParent = ctVariable.getParent();

                // there might be multiple fields hidden by the variable (e.g. subclass hides superclass field)
                boolean isFieldRead = hiddenFields.stream().anyMatch(ctFieldReference -> UsesFinder.ofVariableRead(ctFieldReference.getFieldDeclaration()).in(variableParent).hasAny());

                // to reduce the number of annotations, we only report a problem if the variable AND the hidden field are read in
                // the same context
                if (UsesFinder.ofVariableRead(ctVariable).in(variableParent).hasAny() && isFieldRead) {
                    addLocalProblem(
                        ctVariable,
                        new LocalizedMessage("avoid-shadowing", Map.of("name", ctVariable.getSimpleName())),
                        ProblemType.AVOID_SHADOWING
                    );
                }
            }
        });
    }
}
