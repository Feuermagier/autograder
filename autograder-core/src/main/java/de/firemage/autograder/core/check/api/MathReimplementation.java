package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExecutableCheck(reportedProblems = {
    ProblemType.COMMON_REIMPLEMENTATION_SQRT,
    ProblemType.COMMON_REIMPLEMENTATION_HYPOT,
    ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN
})
public class MathReimplementation extends IntegratedCheck {
    private static boolean isMathPow(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && SpoonUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), Math.class)
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), double.class, "pow", double.class, double.class);
    }

    private static boolean isMathSqrt(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            && SpoonUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), Math.class)
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), double.class, "sqrt", double.class);
    }

    private static boolean isPowSqrt(CtInvocation<?> ctInvocation) {
        return isMathPow(ctInvocation)
            && ctInvocation.getArguments().size() == 2
            && SpoonUtil.resolveConstant(ctInvocation.getArguments().get(1)) instanceof CtLiteral<?> ctLiteral
            && ctLiteral.getValue() instanceof Double doubleValue
            && doubleValue == 0.5;
    }

    private static Optional<CtExpression<?>> getPow2(CtExpression<?> ctExpression) {
        if (ctExpression instanceof CtBinaryOperator<?> ctBinaryOperator
            && ctBinaryOperator.getLeftHandOperand().equals(ctBinaryOperator.getRightHandOperand())
            && ctBinaryOperator.getKind() == BinaryOperatorKind.MUL) {
            return Optional.of(ctBinaryOperator.getLeftHandOperand());
        }

        if (ctExpression instanceof CtInvocation<?> ctInvocation
            && isMathPow(ctInvocation)
            && ctInvocation.getArguments().get(1) instanceof CtLiteral<?> ctLiteral
            && ctLiteral.getValue() instanceof Number value
            && value.doubleValue() == 2.0) {
            return Optional.of(ctInvocation.getArguments().get(0));
        }

        return Optional.empty();
    }

    private boolean checkHypot(CtExpression<?> ctExpression) {
        if (!(ctExpression instanceof CtInvocation<?> ctInvocation)
            || (!isMathSqrt(ctInvocation) && !isPowSqrt(ctInvocation))
            || !(ctInvocation.getArguments().get(0) instanceof CtBinaryOperator<?> ctBinaryOperator)
            || ctBinaryOperator.getKind() != BinaryOperatorKind.PLUS) {
            return false;
        }

        Optional<CtExpression<?>> left = getPow2(ctBinaryOperator.getLeftHandOperand());
        Optional<CtExpression<?>> right = getPow2(ctBinaryOperator.getRightHandOperand());

        if (left.isPresent() && right.isPresent()) {
            addLocalProblem(
                ctExpression,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of("suggestion", "Math.hypot(%s, %s)".formatted(left.get(), right.get()))
                ),
                ProblemType.COMMON_REIMPLEMENTATION_HYPOT
            );

            return true;
        }

        return false;
    }

    private void checkSqrt(CtExpression<?> ctExpression) {
        if (!(ctExpression instanceof CtInvocation<?> ctInvocation) || !isMathPow(ctInvocation)) {
            return;
        }

        if (isPowSqrt(ctInvocation)) {
            addLocalProblem(
                ctExpression,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of("suggestion", "Math.sqrt(%s)".formatted(ctInvocation.getArguments().get(0)))
                ),
                ProblemType.COMMON_REIMPLEMENTATION_SQRT
            );
        }
    }


    private void checkMaxMin(CtIf ctIf) {
        Set<BinaryOperatorKind> maxOperators = Set.of(BinaryOperatorKind.LT, BinaryOperatorKind.LE);
        Set<BinaryOperatorKind> minOperators = Set.of(BinaryOperatorKind.GT, BinaryOperatorKind.GE);

        // ensure that in the if block there is only one assignment to a variable
        // and the condition is a binary operator with <, <=, > or >=
        List<CtStatement> thenBlock = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
        if (thenBlock.size() != 1
            || !(thenBlock.get(0) instanceof CtAssignment<?, ?> thenAssignment)
            || !(thenAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite)
            || !(ctIf.getCondition() instanceof CtBinaryOperator<Boolean> ctBinaryOperator)
            || (!maxOperators.contains(ctBinaryOperator.getKind()) && !minOperators.contains(ctBinaryOperator.getKind()))) {
            return;
        }

        // keep track of the assigned variable (must be the same in the else block)
        CtVariableReference<?> assignedVariable = ctVariableWrite.getVariable();

        // this is the value that is assigned if the then-block is not executed
        // The variable is not changed without an else-Block (this would be equivalent to an else { variable = variable; })
        CtExpression<?> elseValue = ctIf.getFactory().createVariableRead(
            assignedVariable.clone(),
            assignedVariable.getModifiers().contains(ModifierKind.STATIC)
        );
        if (ctIf.getElseStatement() != null) {
            List<CtStatement> elseBlock = SpoonUtil.getEffectiveStatements(ctIf.getElseStatement());
            if (elseBlock.size() != 1
                || !(elseBlock.get(0) instanceof CtAssignment<?,?> elseAssignment)
                || !(elseAssignment.getAssigned() instanceof CtVariableAccess<?> elseAccess)
                // ensure that the else block assigns to the same variable
                || !elseAccess.getVariable().equals(assignedVariable)) {
                return;
            }

            elseValue = elseAssignment.getAssignment();
        }

        CtBinaryOperator<Boolean> condition = ctBinaryOperator;
        // ensure that the else value is on the left side of the condition
        if (ctBinaryOperator.getRightHandOperand().equals(elseValue)) {
            condition = SpoonUtil.swapCtBinaryOperator(condition);
        }

        // if it is not on either side of the condition, return
        if (!condition.getLeftHandOperand().equals(elseValue)) {
            return;
        }

        // check that in `if (variable <op> max) { variable = assigned; }` the max is the same as the assigned value
        if (!condition.getLeftHandOperand().equals(thenAssignment.getAssignment())
            && !condition.getRightHandOperand().equals(thenAssignment.getAssignment())) {
            return;
        }

        // max looks like this:
        // if (variable < max) {
        //     variable = max;
        // }
        //
        // or with an explicit else block:
        //
        // if (max > expr) {
        //     v = max;
        // } else {
        //     v = expr;
        // }

        if (maxOperators.contains(condition.getKind())) {
            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s = Math.max(%s, %s)".formatted(
                            ctVariableWrite,
                            elseValue,
                            condition.getRightHandOperand()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN
            );

            return;
        }

        // if (variable > min) {
        //    variable = min;
        // }

        if (minOperators.contains(condition.getKind())) {
            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s = Math.min(%s, %s)".formatted(
                            ctVariableWrite,
                            elseValue,
                            condition.getRightHandOperand()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN
            );

            return;
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            protected void enter(CtElement ctElement) {
                if (ctElement instanceof CtExpression<?> ctExpression
                    && !ctExpression.isImplicit()
                    && ctExpression.getPosition().isValidPosition()) {
                    // only check for sqrt if hypot is not applicable
                    if (!checkHypot(ctExpression)) {
                        checkSqrt(ctExpression);
                    }
                }

                super.enter(ctElement);
            }

            @Override
            public void visitCtIf(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition() || ctIf.getThenStatement() == null) {
                    super.visitCtIf(ctIf);
                    return;
                }

                checkMaxMin(ctIf);
                super.visitCtIf(ctIf);
            }
        });
    }
}
