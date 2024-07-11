package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.COMMON_REIMPLEMENTATION_ITERABLE_DUPLICATES })
public class CheckIterableDuplicates extends IntegratedCheck {
    private static String buildSuggestion(CtExpression<?> ctExpression, boolean isNegated) {
        CtTypeReference<?> type = ctExpression.getType();

        String leftSide = ctExpression.toString();
        String rightSide = "%s.size()".formatted(leftSide);

        if (type.isArray()) {
            leftSide = "Arrays.asList(%s)".formatted(leftSide);
            rightSide = "%s.length".formatted(ctExpression);
        }

        if (isNegated) {
            return "new HashSet<>(%s).size() != %s".formatted(leftSide, rightSide);
        }

        return "new HashSet<>(%s).size() == %s".formatted(leftSide, rightSide);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtForEach>() {
            @Override
            public void process(CtForEach ctForEach) {
                if (ctForEach.isImplicit() || !ctForEach.getPosition().isValidPosition()) {
                    return;
                }

                List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctForEach.getBody());
                if (statements.size() != 1 || !(statements.get(0) instanceof CtIf ctIf)) {
                    return;
                }

                // the if should only have a then statement
                if (ctIf.getElseStatement() != null || ctIf.getThenStatement() == null) {
                    return;
                }

                List<CtStatement> ifStatements = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
                if (ifStatements.isEmpty()) {
                    return;
                }

                CtLiteral<?> effectValue = null;
                if (ifStatements.size() == 1
                    && ifStatements.get(0) instanceof CtReturn<?> ctReturn
                    && ctReturn.getReturnedExpression() instanceof CtLiteral<?> ctLiteral) {
                    effectValue = ctLiteral;
                }

                if (ifStatements.size() == 2
                    && ifStatements.get(0) instanceof CtAssignment<?,?> ctAssignment
                    && ctAssignment.getAssignment() instanceof CtLiteral<?> ctLiteral
                    && ifStatements.get(1) instanceof CtBreak) {
                    effectValue = ctLiteral;
                }

                if (effectValue == null || !(effectValue.getValue() instanceof Boolean value)) {
                    return;
                }

                // check that the if looks like this:
                // if(!set.add(s)) {

                if (!(ctIf.getCondition() instanceof CtUnaryOperator<Boolean> ctUnaryOperator
                    && ctUnaryOperator.getKind() == UnaryOperatorKind.NOT
                    && ctUnaryOperator.getOperand() instanceof CtInvocation<?> ctInvocation
                    && SpoonUtil.isTypeEqualTo(ctInvocation.getExecutable().getType(), boolean.class)
                    && ctInvocation.getExecutable().getSimpleName().equals("add")
                    && ctInvocation.getArguments().size() == 1
                    && ctInvocation.getArguments().get(0) instanceof CtVariableRead<?> ctVariableRead
                    && ctVariableRead.getVariable().equals(ctForEach.getVariable().getReference())
                    && SpoonUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Set.class)))
                {
                    return;
                }

                String suggestion = buildSuggestion(ctForEach.getExpression(), Boolean.TRUE.equals(value));

                addLocalProblem(
                    ctForEach,
                    new LocalizedMessage(
                        "common-reimplementation",
                        Map.of(
                            "suggestion", suggestion
                        )
                    ),
                    ProblemType.COMMON_REIMPLEMENTATION_ITERABLE_DUPLICATES
                );
            }
        });
    }
}
