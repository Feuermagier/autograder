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
import spoon.reflect.declaration.CtTypedElement;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.USE_OPERATOR_ASSIGNMENT})

public class UseOperatorAssignment extends IntegratedCheck {
    private static final List<Class<?>> NON_COMMUTATIVE_TYPES = List.of(
        java.lang.String.class
    );

    public UseOperatorAssignment() {
        super(new LocalizedMessage("use-operator-assignment-desc"));
    }

    private boolean isCommutativeType(CtTypedElement<?> ctTypedElement) {
        return ctTypedElement.getType() == null
               || NON_COMMUTATIVE_TYPES.stream()
                                       .map(ty -> ctTypedElement.getFactory().Type().createReference(ty))
                                       .noneMatch(ty -> ty.equals(ctTypedElement.getType()));
    }

    private boolean isCommutative(BinaryOperatorKind binaryOperatorKind) {
        return List.of(
            BinaryOperatorKind.AND,
            BinaryOperatorKind.OR,
            BinaryOperatorKind.BITXOR,
            BinaryOperatorKind.MUL,
            BinaryOperatorKind.PLUS
        ).contains(binaryOperatorKind);
    }

    private boolean isAssignable(BinaryOperatorKind binaryOperatorKind) {
        return this.isCommutative(binaryOperatorKind) || List.of(
            BinaryOperatorKind.MOD,
            BinaryOperatorKind.MINUS,
            BinaryOperatorKind.DIV,
            BinaryOperatorKind.SL,
            BinaryOperatorKind.SR
        ).contains(binaryOperatorKind);
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
                    simplifiedExpr = "%s %s= %s".formatted(lhs, PrintUtil.printOperator(operator), right);
                } else if (isCommutative(operator) && isCommutativeType(ctBinaryOperator) && right.toString().equals(lhs.toString())) {
                    // operator is commutative so <lhs> = <left> <op> <right> is equivalent to
                    // <lhs> = <right> <op> <left>

                    simplifiedExpr = "%s %s= %s".formatted(lhs, PrintUtil.printOperator(operator), left);
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
