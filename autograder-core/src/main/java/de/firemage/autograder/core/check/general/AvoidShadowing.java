package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtFieldReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@ExecutableCheck(reportedProblems = {ProblemType.AVOID_SHADOWING})
public class AvoidShadowing extends IntegratedCheck {
    // a lower bound for the number of reads on a hidden field, before it is reported
    private static final int MINIMUM_FIELD_READS = 2;
    private static final List<String> ALLOWED_FIELDS = List.of("serialVersionUID");

    private static Collection<CtFieldReference<?>> getAllVisibleFields(CtTypeInformation ctTypeInformation) {
        Collection<CtFieldReference<?>> result = new ArrayList<>(ctTypeInformation.getDeclaredFields());

        for (CtType<?> parent : TypeUtil.allSuperTypes(ctTypeInformation)) {
            if (parent.isInterface()) {
                continue;
            }

            result.addAll(
                parent.getDeclaredFields()
                    .stream()
                    // only non-private fields are visible to a subclass
                    .filter(ctFieldReference -> !ctFieldReference.getFieldDeclaration().isPrivate())
                    .toList()
            );
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
                if (MethodUtil.isInOverridingMethod(ctVariable) || MethodUtil.isInSetter(ctVariable)) {
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

                // there might be multiple fields hidden by the variable, like for example:
                // class A {
                //    int a;
                // }
                //
                // class B extends A {
                //    int a;
                //    void foo(int a) {} // param hides field of A and B
                // }
                int numberOfFieldReads = Math.toIntExact(hiddenFields.stream()
                    .map(ctFieldReference -> UsesFinder.variableUses(ctFieldReference.getFieldDeclaration())
                        .nestedIn(variableParent)
                        .filter(ctVariableAccess -> ctVariableAccess instanceof CtVariableRead<?>)
                        .count()).max(Long::compareTo).orElse(0L));


                // to reduce the number of annotations, we only report a problem if the variable AND the hidden field are read in
                // the same context
                if (UsesFinder.variableUses(ctVariable).nestedIn(variableParent).hasAny() && numberOfFieldReads >= MINIMUM_FIELD_READS) {
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
