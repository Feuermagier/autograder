package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_SELF_ASSIGNMENT})
public class SelfAssignmentCheck extends IntegratedCheck {

    public SelfAssignmentCheck() {
        super(new LocalizedMessage("self-assignment-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssignment<?, ?>>() {
            @Override
            public void process(CtAssignment<?, ?> assignment) {
                if (assignment.getAssignment() instanceof CtVariableRead<?> read
                    && assignment.getAssigned() instanceof CtVariableWrite<?> write) {
                    if (read.getVariable().equals(write.getVariable())) {
                        addLocalProblem(assignment,
                            new LocalizedMessage(
                                "self-assignment-exp",
                                Map.of(
                                    "lhs", assignment.getAssigned(),
                                    "rhs", assignment.getAssignment()
                                )
                            ), ProblemType.REDUNDANT_SELF_ASSIGNMENT);
                    }
                }
            }
        });
    }
}
