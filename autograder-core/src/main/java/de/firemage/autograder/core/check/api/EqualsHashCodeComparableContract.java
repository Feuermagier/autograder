package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;

@ExecutableCheck(reportedProblems = { ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT })
public class EqualsHashCodeComparableContract extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> ctType) {
                if (!ctType.isClass() && !ctType.isEnum() && !(ctType instanceof CtRecord)) {
                    return;
                }

                boolean hasEquals = false;
                boolean hasHashCode = false;

                for (CtMethod<?> ctMethod : ctType.getMethods()) {
                    if (ctMethod.getSimpleName().equals("equals")) {
                        hasEquals = true;
                    } else if (ctMethod.getSimpleName().equals("hashCode")) {
                        hasHashCode = true;
                    }
                }

                boolean implementsComparable =
                    ctType.getSuperInterfaces()
                          .stream()
                          .anyMatch(ctTypeReference -> ctTypeReference.getQualifiedName().equals("java.lang.Comparable"));

                if (!hasEquals && hasHashCode
                    || hasEquals && !hasHashCode
                    || implementsComparable && !(hasEquals && hasHashCode)) {
                    addLocalProblem(
                        ctType,
                        new LocalizedMessage("equals-hashcode-comparable-contract"),
                        ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT
                    );
                }
            }
        });
    }
}
