package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_SELF_ASSIGNMENT })
public class SelfAssignmentCheck extends IntegratedCheck {

    public SelfAssignmentCheck() {
        super(new LocalizedMessage("self-assignment-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssignment<?, ?>>() {
            @Override
            public void process(CtAssignment<?, ?> assignment) {
                CtExpression<?> lhs = assignment.getAssigned();
                CtExpression<?> rhs = assignment.getAssignment();

                if (!(rhs instanceof CtVariableRead<?> read) ||
                    !(lhs instanceof CtVariableWrite<?> write)) {
                    return;
                }

                if (rhs instanceof CtFieldRead<?> fieldRead &&
                    lhs instanceof CtFieldWrite<?> fieldWrite) {
                    // special case for assignment to fields
                    // this.a = other.a; getVariable() will return for both "a"
                    // => they are considered equal even though they are not
                    if (fieldRead.toString().equals(fieldWrite.toString())) {
                        addLocalProblem(assignment,
                            new LocalizedMessage(
                                "self-assignment-exp",
                                Map.of(
                                    "lhs", lhs,
                                    "rhs", rhs
                                )
                            ), ProblemType.REDUNDANT_SELF_ASSIGNMENT
                        );
                    }

                    return;
                }

                if (read.getVariable().equals(write.getVariable())) {
                    addLocalProblem(assignment,
                        new LocalizedMessage(
                            "self-assignment-exp",
                            Map.of(
                                "lhs", lhs,
                                "rhs", rhs
                            )
                        ), ProblemType.REDUNDANT_SELF_ASSIGNMENT
                    );
                }
            }
        });
    }
}
