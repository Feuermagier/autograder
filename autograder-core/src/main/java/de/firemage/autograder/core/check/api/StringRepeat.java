package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.visitor.filter.VariableAccessFilter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT })
public class StringRepeat extends IntegratedCheck {
    private void checkStringRepeat(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1 || forLoopRange == null) {
            return;
        }

        // lhs += rhs
        if (statements.get(0) instanceof CtOperatorAssignment<?, ?> ctAssignment
            && ctAssignment.getKind() == BinaryOperatorKind.PLUS) {
            CtExpression<?> lhs = ctAssignment.getAssigned();
            if (!SpoonUtil.isTypeEqualTo(lhs.getType(), String.class)) {
                return;
            }

            CtExpression<?> rhs = SpoonUtil.resolveCtExpression(ctAssignment.getAssignment());
            // return if the for loop uses the loop variable (would not be a simple repetition)
            if (!ctAssignment.getElements(new VariableAccessFilter<>(forLoopRange.loopVariable())).isEmpty()) {
                return;
            }

            // return if the rhs uses the lhs: lhs += rhs + lhs
            if (lhs instanceof CtVariableAccess<?> ctVariableAccess && !rhs.getElements(new VariableAccessFilter<>(ctVariableAccess.getVariable())).isEmpty()) {
                return;
            }

            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        // string.repeat(count)
                        "suggestion", "%s += %s".formatted(
                            lhs,
                            rhs.getFactory().createInvocation(
                                rhs.clone(),
                                rhs.getFactory().Type().get(java.lang.String.class)
                                    .getMethod("repeat", rhs.getFactory().createCtTypeReference(int.class))
                                    .getReference()
                                    .clone(),
                                forLoopRange.length().clone()
                            ))
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT
            );
        }
    }
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                checkStringRepeat(ctFor);
            }
        });
    }
}
