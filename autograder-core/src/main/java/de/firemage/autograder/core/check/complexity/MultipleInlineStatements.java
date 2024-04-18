package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@ExecutableCheck(reportedProblems = {
    ProblemType.FOR_WITH_MULTIPLE_VARIABLES,
    ProblemType.MULTIPLE_INLINE_STATEMENTS
})
public class MultipleInlineStatements extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        Collection<CtElement> alreadyReported = new HashSet<>();

        staticAnalysis.processWith(new AbstractProcessor<CtStatement>() {
            @Override
            public void process(CtStatement ctStatement) {
                if (ctStatement instanceof CtFor ctFor && ctFor.getForInit().size() > 1) {
                    addLocalProblem(
                        ctFor.getForInit().get(0),
                        new LocalizedMessage("for-loop-var"),
                        ProblemType.FOR_WITH_MULTIPLE_VARIABLES
                    );
                }

                if (ctStatement instanceof CtLocalVariable<?> ctLocalVariable
                    && !alreadyReported.contains(ctLocalVariable)
                    && !(ctLocalVariable.getParent() instanceof CtFor)) {
                    Collection<CtLocalVariable<?>> others = new LinkedHashSet<>();

                    List<CtLocalVariable<?>> variables = ctLocalVariable.getParent()
                        .getElements(new TypeFilter<>(CtLocalVariable.class));

                    for (CtLocalVariable<?> ctVariable : variables) {
                        if (!ctLocalVariable.equals(ctVariable)
                            && !ctVariable.isImplicit()
                            && ctVariable.getPosition().isValidPosition()
                            && ctLocalVariable.getPosition().getLine() == ctVariable.getPosition().getLine()) {
                            others.add(ctVariable);
                        }
                    }

                    if (!others.isEmpty()) {
                        alreadyReported.add(ctLocalVariable);
                        alreadyReported.addAll(others);
                        addLocalProblem(
                            ctLocalVariable,
                            new LocalizedMessage("multiple-inline-statements"),
                            ProblemType.MULTIPLE_INLINE_STATEMENTS
                        );
                    }
                }

                if (ctStatement instanceof CtAssignment<?,?> ctAssignment
                    && ctAssignment.getParent(CtAssignment.class) == null
                    && ctAssignment.getAssignment() instanceof CtAssignment<?, ?>) {
                    addLocalProblem(
                        ctAssignment,
                        new LocalizedMessage("multiple-inline-statements"),
                        ProblemType.MULTIPLE_INLINE_STATEMENTS
                    );
                }
            }
        });
    }
}
