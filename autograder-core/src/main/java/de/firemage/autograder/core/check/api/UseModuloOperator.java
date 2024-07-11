package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.reference.CtVariableReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.USE_MODULO_OPERATOR })
public class UseModuloOperator extends IntegratedCheck {
    private static final Set<BinaryOperatorKind> ALLOWED_OPERATORS = Set.of(
        BinaryOperatorKind.LT,
        BinaryOperatorKind.LE,
        BinaryOperatorKind.GE,
        BinaryOperatorKind.GT,
        BinaryOperatorKind.EQ
    );

    private void checkModulo(CtIf ctIf) {
        List<CtStatement> thenBlock = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
        if (ctIf.getElseStatement() != null
            || thenBlock.size() != 1
            || !(thenBlock.get(0) instanceof CtAssignment<?, ?> thenAssignment)
            || !(thenAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite)
            || !(ctIf.getCondition() instanceof CtBinaryOperator<Boolean> ctBinaryOperator)
            || !ALLOWED_OPERATORS.contains(ctBinaryOperator.getKind())) {
            return;
        }

        // must assign a value of 0
        if (!(SpoonUtil.resolveCtExpression(thenAssignment.getAssignment()) instanceof CtLiteral<?> ctLiteral)
            || !(ctLiteral.getValue() instanceof Integer integer)
            || integer != 0) {
            return;
        }

        CtVariableReference<?> assignedVariable = ctVariableWrite.getVariable();

        CtBinaryOperator<Boolean> condition = SpoonUtil.normalizeBy(
            (left, right) -> right instanceof CtVariableAccess<?> ctVariableAccess && ctVariableAccess.getVariable().equals(assignedVariable),
            ctBinaryOperator
        );

        // the assigned variable is not on either side
        if (!(condition.getLeftHandOperand() instanceof CtVariableAccess<?> ctVariableAccess)
            || !(ctVariableAccess.getVariable().equals(assignedVariable))) {
            return;
        }

        // if (variable >= 3) {
        //    variable = 0;
        // }
        //
        // is equal to
        //
        // variable %= 3;
        CtExpression<?> checkedValue = condition.getRightHandOperand();

        // for boxed types, one could check if the value is null,
        // for which the suggestion `a %= null` would not make sense
        if (SpoonUtil.isNullLiteral(checkedValue)) {
            return;
        }

        if (Set.of(BinaryOperatorKind.GE, BinaryOperatorKind.EQ).contains(condition.getKind())) {
            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s %%= %s".formatted(
                            assignedVariable,
                            checkedValue
                        )
                    )
                ),
                ProblemType.USE_MODULO_OPERATOR
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition() || ctIf.getThenStatement() == null) {
                    return;
                }

                checkModulo(ctIf);
            }
        });
    }
}
