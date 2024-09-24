package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.general.ForToForEachLoop;

import de.firemage.autograder.core.integrated.ExpressionUtil;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.COMMON_REIMPLEMENTATION_SUBLIST})
public class UseSubList extends IntegratedCheck {
    private void checkSubList(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        if (forLoopRange == null) {
            return;
        }

        // ensure that the variable is only used to access the list elements via get
        // like list.get(i)
        CtVariable<?> ctListVariable = ForToForEachLoop.getForEachLoopVariable(
            ctFor,
            forLoopRange,
            ForToForEachLoop.LOOP_VARIABLE_ACCESS_LIST
        ).orElse(null);

        if (ctListVariable == null) {
            return;
        }

        // check if the loop iterates over the whole list (then it is covered by the foreach loop check)
        if (ExpressionUtil.resolveConstant(forLoopRange.start()) instanceof CtLiteral<Integer> ctLiteral
            && ctLiteral.getValue() == 0
            && ForToForEachLoop.findIterable(forLoopRange).isPresent()) {
            return;
        }

        // look for a single statement in the loop body, which should be a single invocation to add
        List<CtStatement> statementList = StatementUtil.getEffectiveStatements(ctFor.getBody());
        if (statementList.size() != 1) {
            return;
        }

        // should look like this: result.add(list.get(i))
        if (!(statementList.get(0) instanceof CtInvocation<?> ctInvocation) || !ForLoopCanBeInvocation.isCollectionAddInvocation(ctInvocation)) {
            return;
        }


        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "%s.addAll(%s.subList(%s, %s))".formatted(
                        ctInvocation.getTarget(),
                        ctListVariable.getSimpleName(),
                        forLoopRange.start(),
                        forLoopRange.end()
                    )
                )
            ),
            ProblemType.COMMON_REIMPLEMENTATION_SUBLIST
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                checkSubList(ctFor);
            }
        });
    }
}
