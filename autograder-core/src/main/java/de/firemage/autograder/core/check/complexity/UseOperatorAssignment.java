package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.PrintUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtOperatorAssignment;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.USE_OPERATOR_ASSIGNMENT})

public class UseOperatorAssignment extends IntegratedCheck {
    public UseOperatorAssignment() {
        super(new LocalizedMessage("use-operator-assignment-desc"));
    }

    private boolean isCommutative(BinaryOperatorKind kind) {
        return List.of(
            BinaryOperatorKind.AND,
            BinaryOperatorKind.OR,
            BinaryOperatorKind.BITXOR,
            BinaryOperatorKind.MUL,
            BinaryOperatorKind.PLUS
        ).contains(kind);
    }

    private boolean isAssignable(BinaryOperatorKind kind) {
        return this.isCommutative(kind) || List.of(
            BinaryOperatorKind.MOD,
            BinaryOperatorKind.MINUS,
            BinaryOperatorKind.DIV,
            BinaryOperatorKind.SL,
            BinaryOperatorKind.SR
        ).contains(kind);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAssignment<?, ?>>() {
            @Override
            public void process(CtAssignment<?, ?> assignment) {
                // skip operator assignments:
                if (assignment instanceof CtOperatorAssignment<?, ?>) {
                    return;
                }

                CtExpression<?> lhs = assignment.getAssigned();
                CtExpression<?> rhs = assignment.getAssignment();
                if (lhs == null || rhs == null) {
                    return;
                }

                if (!(rhs instanceof CtBinaryOperator<?> ctBinaryOperator)) {
                    return;
                }

                BinaryOperatorKind operator = ctBinaryOperator.getKind();
                if (!isAssignable(operator)) {
                    return;
                }

                CtExpression<?> left = ctBinaryOperator.getLeftHandOperand();
                CtExpression<?> right = ctBinaryOperator.getRightHandOperand();

                String simplifiedExpr = null;
                if (left.toString().equals(lhs.toString())) {
                    // left hand side is the same, so we can use an operator assignment
                    simplifiedExpr = String.format("%s %s= %s", lhs, PrintUtil.printOperator(operator), right);
                } else if (isCommutative(operator) && right.toString().equals(lhs.toString())) {
                    // operator is commutative so <lhs> = <left> <op> <right> is equivalent to
                    // <lhs> = <right> <op> <left>

                    simplifiedExpr = String.format("%s %s= %s", lhs, PrintUtil.printOperator(operator), left);
                }

                if (simplifiedExpr != null) {
                    addLocalProblem(
                        assignment,
                        new LocalizedMessage(
                            "use-operator-assignment-exp",
                            Map.of("simplified", simplifiedExpr)
                        ),
                        ProblemType.USE_OPERATOR_ASSIGNMENT
                    );
                }
            }
        });
    }
}
