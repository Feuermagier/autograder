package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableWrite;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_UNINITIALIZED_VARIABLE })
public class RedundantUninitializedVariable extends IntegratedCheck {
    private static String makeSuggestion(CtLocalVariable<?> ctLocalVariable, CtExpression<?> ctValue) {
        String modifier = ctLocalVariable.getModifiers()
            .stream()
            .map(Object::toString)
            .map(string -> string + " ")
            .collect(Collectors.joining(""));

        return "%s%s %s = %s".formatted(
            modifier,
            ctLocalVariable.getType(),
            ctLocalVariable.getSimpleName(),
            ctValue
        );
    }
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtLocalVariable<?>>() {
            @Override
            public void process(CtLocalVariable<?> ctLocalVariable) {
                // skip invalid positions and variables with assignments
                if (!ctLocalVariable.getPosition().isValidPosition() || ctLocalVariable.getAssignment() != null) {
                    return;
                }

                // query all assignments to this variable
                List<CtAssignment<? extends CtVariableWrite<?>, ?>> ctAssignments = ctLocalVariable.getParent()
                    .getElements((CtAssignment<? extends CtVariableWrite<?>, ?> ctAssignment) ->
                        ctAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite
                        && ctVariableWrite.getVariable().equals(ctLocalVariable.getReference()));

                if (ctAssignments.isEmpty()) return;

                boolean isConditional = !SpoonUtil.getEffectiveStatements(ctLocalVariable.getParent(CtBlock.class))
                    .contains(ctAssignments.get(0));

                if (isConditional) {
                    // variable is assigned in a different block (like in an if)
                    return;
                }

                CtExpression<?> ctValue = ctAssignments.get(0).getAssignment();

                addLocalProblem(
                    ctLocalVariable,
                    new LocalizedMessage("redundant-uninitialized-variable", Map.of(
                        "variable", ctLocalVariable.getSimpleName(),
                        "value", ctValue,
                        "suggestion", makeSuggestion(ctLocalVariable, ctValue)
                    )),
                    ProblemType.REDUNDANT_UNINITIALIZED_VARIABLE
                );
            }
        });
    }
}
