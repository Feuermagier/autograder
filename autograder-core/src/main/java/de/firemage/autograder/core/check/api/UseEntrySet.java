package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.InvocationFilter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.USE_ENTRY_SET})
public class UseEntrySet extends IntegratedCheck {
    // If a map is iterated over and the key is used to get the value more than MINIMUM_GET_CALLS times, it suggests using entrySet
    private static final int MINIMUM_GET_CALLS = 3;

    private static boolean hasInvokedKeySet(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() != null
            && ctInvocation.getExecutable() != null
            && TypeUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Map.class)
            && ctInvocation.getExecutable().getSimpleName().equals("keySet");
    }

    private static String makeSuggestion(CtInvocation<?> ctInvocation) {
        String suggestion = "%s.entrySet()".formatted(ctInvocation.getTarget());
        if (suggestion.startsWith(".")) {
            suggestion = suggestion.substring(1);
        }

        return suggestion;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtForEach>() {
            @Override
            public void process(CtForEach ctForEach) {
                if (ctForEach.isImplicit()
                    || !ctForEach.getPosition().isValidPosition()
                    || !(ctForEach.getExpression() instanceof CtInvocation<?> ctInvocation)
                    || !hasInvokedKeySet(ctInvocation)
                    || !ctForEach.getExpression().getPosition().isValidPosition()) {
                    return;
                }

                CtLocalVariable<?> loopVariable = ctForEach.getVariable();

                CtExecutableReference<?> ctExecutableReference = ctInvocation.getFactory()
                    .createCtTypeReference(java.util.Map.class)
                    .getTypeDeclaration()
                    .getMethod("get", ctInvocation.getFactory().createCtTypeReference(Object.class))
                    .getReference();

                List<CtInvocation<?>> invocations = ctForEach.getBody()
                    .getElements(new InvocationFilter(ctExecutableReference))
                    .stream()
                    .filter(invocation -> invocation.getTarget() != null
                        && invocation.getTarget().equals(ctInvocation.getTarget())
                        && invocation.getArguments().size() == 1
                        && invocation.getArguments().get(0) instanceof CtVariableAccess<?> ctVariableAccess
                        && ctVariableAccess.getVariable().equals(loopVariable.getReference()))
                    .toList();

                if (invocations.size() >= MINIMUM_GET_CALLS) {
                    addLocalProblem(
                        ctForEach.getExpression(),
                        new LocalizedMessage(
                            "suggest-replacement",
                            Map.of(
                                "original", ctInvocation,
                                "suggestion", makeSuggestion(ctInvocation)
                            )),
                        ProblemType.USE_ENTRY_SET
                    );
                }
            }
        });
    }
}
