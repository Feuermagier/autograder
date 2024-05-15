package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY})
public class UseArrayCopy extends IntegratedCheck {

    private void checkArrayCopy(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1 || forLoopRange == null) {
            return;
        }


        if (statements.get(0) instanceof CtAssignment<?, ?> ctAssignment
            && !(ctAssignment instanceof CtOperatorAssignment<?, ?>)
            && ctAssignment.getAssigned() instanceof CtArrayAccess<?, ?> lhs
            && ctAssignment.getAssignment() instanceof CtArrayAccess<?, ?> rhs
            && lhs.getTarget() != null
            && rhs.getTarget() != null
            && lhs.getIndexExpression().equals(rhs.getIndexExpression())
            && lhs.getIndexExpression() instanceof CtVariableRead<Integer> index
            && index.getVariable().equals(forLoopRange.loopVariable())) {
            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        // System.arraycopy(src, srcPos, dest, destPos, length)
                        "suggestion", "System.arraycopy(%s, %s, %s, %s, %s)".formatted(
                            rhs.getTarget(),
                            forLoopRange.start(),
                            lhs.getTarget(),
                            forLoopRange.start(),
                            forLoopRange.length()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                checkArrayCopy(ctFor);
            }
        });
    }
}
