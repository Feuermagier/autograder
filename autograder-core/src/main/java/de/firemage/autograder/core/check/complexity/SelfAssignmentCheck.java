package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;

public class SelfAssignmentCheck extends IntegratedCheck {

    public SelfAssignmentCheck() {
        super("Assigning a variable to itself is useless.");
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssignment<?, ?>>() {
            @Override
            public void process(CtAssignment<?, ?> assignment) {
                if (assignment.getAssignment() instanceof CtVariableRead<?> read 
                    && assignment.getAssigned() instanceof CtVariableWrite<?> write) {
                    // TODO exclude e.g. this.x = other.x where type(this) == type(other)
                    if (read.getVariable().getDeclaration().equals(write.getVariable().getDeclaration())) {
                        addLocalProblem(assignment,
                            "Useless assignment of '" + assignment.getAssignment() + "' to '" + assignment.getAssigned() +
                                "'");
                    }
                }
            }
        });
    }
}
