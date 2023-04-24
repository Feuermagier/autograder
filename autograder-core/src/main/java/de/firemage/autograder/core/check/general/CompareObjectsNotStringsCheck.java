package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.OBJECTS_COMPARED_VIA_TO_STRING})
public class CompareObjectsNotStringsCheck extends IntegratedCheck {
    private static LocalizedMessage formatExplanation(CtTypeReference<?> type) {
        return new LocalizedMessage("compare-objects-exp", Map.of("type", type.getQualifiedName()));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> invocation) {
                CtExecutableReference<?> executable = invocation.getExecutable();
                if (executable.getSignature().equals("equals(java.lang.Object)") &&
                    executable.getParameters().size() == 1) {
                    Optional<CtTypeReference<?>> lhsType = SpoonUtil.isToStringCall(invocation.getTarget());
                    if (lhsType.isEmpty()) {
                        return;
                    }

                    Optional<CtTypeReference<?>> rhsType = SpoonUtil.isToStringCall(invocation.getArguments().get(0));
                    if (rhsType.isEmpty()) {
                        return;
                    }

                    if (lhsType.get().getQualifiedName().equals(rhsType.get().getQualifiedName())) {
                        addLocalProblem(invocation, formatExplanation(lhsType.get()),
                            ProblemType.OBJECTS_COMPARED_VIA_TO_STRING);
                    }
                }
            }
        });
    }
}
