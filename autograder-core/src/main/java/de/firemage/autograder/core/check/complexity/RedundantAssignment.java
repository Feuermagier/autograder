package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.unnecessary.UnusedCodeElementCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtLocalVariableReference;


import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_ASSIGNMENT })
public class RedundantAssignment extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssignment<?, ?>>() {
            @Override
            public void process(CtAssignment<?, ?> ctAssignment) {
                if (ctAssignment.isImplicit()
                    || !ctAssignment.getPosition().isValidPosition()
                    || !(ctAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite)
                    || !(ctVariableWrite.getVariable() instanceof CtLocalVariableReference<?> ctLocalVariableReference)) {
                    return;
                }

                if (!(ctAssignment.getParent() instanceof CtStatementList)
                    || ctLocalVariableReference.getDeclaration() == null
                    || !(ctAssignment.getParent().getParent() instanceof CtMethod<?>)) {
                    return;
                }

                List<CtStatement> followingStatements = SpoonUtil.getNextStatements(ctAssignment);

                CtLocalVariable<?> ctLocalVariable = ctLocalVariableReference.getDeclaration();

                if (UnusedCodeElementCheck.isConsideredUnused(ctLocalVariable, staticAnalysis.getCodeModel())) {
                    return;
                }

                if (followingStatements.stream().noneMatch(statement ->
                        UsesFinder.variableUses(ctLocalVariable).ofType(CtVariableRead.class).nestedIn(statement).hasAny())
                ) {
                    addLocalProblem(
                        ctAssignment,
                        new LocalizedMessage(
                            "redundant-assignment",
                            Map.of("variable", ctLocalVariable.getSimpleName())
                        ),
                        ProblemType.REDUNDANT_ASSIGNMENT
                    );
                }
            }
        });
    }
}
