package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
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
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
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

    private static <T> boolean isAddInvocationOnSet(CtInvocation<T> ctInvocation, CtVariableReference<?> argument) {
        return TypeUtil.isTypeEqualTo(ctInvocation.getExecutable().getType(), boolean.class)
            && ctInvocation.getExecutable().getSimpleName().equals("add")
            && ctInvocation.getArguments().size() == 1
            && ctInvocation.getArguments().get(0) instanceof CtVariableRead<?> ctVariableRead
            && ctVariableRead.getVariable().equals(argument)
            && TypeUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Set.class);
    }

    private static <T> boolean isContainsInvocationOnSet(CtInvocation<T> ctInvocation, CtVariableReference<?> argument) {
        return TypeUtil.isTypeEqualTo(ctInvocation.getExecutable().getType(), boolean.class)
            && ctInvocation.getExecutable().getSimpleName().equals("contains")
            && ctInvocation.getArguments().size() == 1
            && ctInvocation.getArguments().get(0) instanceof CtVariableRead<?> ctVariableRead
            && ctVariableRead.getVariable().equals(argument)
            && TypeUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Set.class);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtForEach>() {
            @Override
            public void process(CtForEach ctForEach) {
                if (ctForEach.isImplicit() || !ctForEach.getPosition().isValidPosition()) {
                    return;
                }

                List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctForEach.getBody());
                if (statements.isEmpty() || !(statements.get(0) instanceof CtIf ctIf) || ctIf.getThenStatement() == null) {
                    return;
                }

                // one can implement this in multiple ways, for example:
                // for (var elem : list) {
                //   if (!set.add(elem)) {
                //     return false;
                //   }
                // }
                // or one could have a contains and then an add statement:
                // for (var elem : list) {
                //   if (set.contains(elem)) {
                //     return false;
                //   }
                //   set.add(elem);
                // }

                List<CtStatement> ifStatements = StatementUtil.getEffectiveStatements(ctIf.getThenStatement());
                if (ifStatements.isEmpty()) {
                    return;
                }

                if ((ctIf.getElseStatement() != null || statements.size() == 2)
                    && ctIf.getCondition() instanceof CtInvocation<?> ctInvocation
                    && isContainsInvocationOnSet(ctInvocation, ctForEach.getVariable().getReference())) {
                    // it invokes contains, so the else must have the add invocation:

                    List<CtStatement> elseStatements = new ArrayList<>();
                    if (statements.size() == 2) {
                        elseStatements.add(statements.get(1));
                    } else {
                        elseStatements = StatementUtil.getEffectiveStatements(ctIf.getElseStatement());
                    }

                    CtLiteral<?> effectValue = getEffectValue(ifStatements);
                    if (effectValue != null
                        && effectValue.getValue() instanceof Boolean value
                        && elseStatements.size() == 1
                        && elseStatements.get(0) instanceof CtInvocation<?> ctElseInvocation
                        && isAddInvocationOnSet(ctElseInvocation, ctForEach.getVariable().getReference())) {
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
                        return;
                    }
                }

                if (statements.size() != 1) {
                    return;
                }


                // the if should only have a then statement
                if (ctIf.getElseStatement() != null) {
                    return;
                }

                CtLiteral<?> effectValue = getEffectValue(ifStatements);

                if (effectValue == null || !(effectValue.getValue() instanceof Boolean value)) {
                    return;
                }

                // check that the if looks like this:
                // if(!set.add(s)) {

                if (!(ctIf.getCondition() instanceof CtUnaryOperator<Boolean> ctUnaryOperator
                    && ctUnaryOperator.getKind() == UnaryOperatorKind.NOT
                    && ctUnaryOperator.getOperand() instanceof CtInvocation<?> ctInvocation
                    && isAddInvocationOnSet(ctInvocation, ctForEach.getVariable().getReference())))
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

    // this extracts the return value from the statements, depending on where it is used it could be a
    // return <value>
    // or
    // <variable> = <value>; break; (this would be used in a loop)
    private static CtLiteral<?> getEffectValue(List<CtStatement> ifStatements) {
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
        return effectValue;
    }
}
