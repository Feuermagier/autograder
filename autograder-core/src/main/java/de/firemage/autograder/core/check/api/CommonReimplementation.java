package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.VariableAccessFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = {
    ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY,
    ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT,
    ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN,
    ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL,
    ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL,
    ProblemType.COMMON_REIMPLEMENTATION_MODULO
})
public class CommonReimplementation extends IntegratedCheck {
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
                            lhs.prettyprint(),
                            rhs.getFactory().createInvocation(
                                rhs,
                                rhs.getFactory().Type().get(java.lang.String.class)
                                    .getMethod("repeat", rhs.getFactory().createCtTypeReference(int.class))
                                    .getReference(),
                                forLoopRange.length()
                            ).prettyprint())
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT
            );
        }
    }

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
                            rhs.getTarget().prettyprint(),
                            forLoopRange.start().prettyprint(),
                            lhs.getTarget().prettyprint(),
                            forLoopRange.start().prettyprint(),
                            forLoopRange.length().prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY
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
            assignedVariable,
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
                            ctVariableWrite.prettyprint(),
                            elseValue.prettyprint(),
                            condition.getRightHandOperand().prettyprint()
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
                            ctVariableWrite.prettyprint(),
                            elseValue.prettyprint(),
                            condition.getRightHandOperand().prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MAX_MIN
            );

            return;
        }
    }

    private void checkAddAll(CtForEach ctFor) {
        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1) {
            return;
        }

        if (statements.get(0) instanceof CtInvocation<?> ctInvocation
            && SpoonUtil.isSubtypeOf(ctInvocation.getTarget().getType(), java.util.Collection.class)
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), boolean.class, "add", Object.class)
            && ctInvocation.getExecutable().getParameters().size() == 1
            // ensure that the add argument simply accesses the for variable:
            // for (T t : array) {
            //     collection.add(t);
            // }
            && ctInvocation.getArguments().get(0) instanceof CtVariableRead<?> ctVariableRead
            && ctVariableRead.getVariable().equals(ctFor.getVariable().getReference())) {

            String addAllArg = ctFor.getExpression().prettyprint();
            if (ctFor.getExpression().getType().isArray()) {
                addAllArg = "Arrays.asList(%s)".formatted(addAllArg);
            }


            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s.addAll(%s)".formatted(
                            ctInvocation.getTarget().prettyprint(),
                            addAllArg
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL
            );
        }
    }

    private void checkArraysFill(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());

        if (statements.size() != 1
            || forLoopRange == null
            || !(statements.get(0) instanceof CtAssignment<?, ?> ctAssignment)
            || !(ctAssignment.getAssigned() instanceof CtArrayWrite<?> ctArrayWrite)
            || !(ctArrayWrite.getIndexExpression() instanceof CtVariableRead<?> index)
            || !(index.getVariable().equals(forLoopRange.loopVariable()))) {
            return;
        }

        // return if the for loop uses the loop variable (would not be a simple repetition)
        if (!ctAssignment.getAssignment().getElements(new VariableAccessFilter<>(forLoopRange.loopVariable())).isEmpty()) {
            return;
        }

        CtExpression<?> rhs = ctAssignment.getAssignment();
        if (!SpoonUtil.isImmutable(rhs.getType())) {
            return;
        }

        this.addLocalProblem(
            ctFor,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "Arrays.fill(%s, %s, %s, %s)".formatted(
                        ctArrayWrite.getTarget().prettyprint(),
                        forLoopRange.start().prettyprint(),
                        forLoopRange.end().prettyprint(),
                        ctAssignment.getAssignment().prettyprint()
                    )
                )
            ),
            ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL
        );
    }

    private void checkModulo(CtIf ctIf) {
        List<CtStatement> thenBlock = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
        if (ctIf.getElseStatement() != null
            || thenBlock.size() != 1
            || !(thenBlock.get(0) instanceof CtAssignment<?, ?> thenAssignment)
            || !(thenAssignment.getAssigned() instanceof CtVariableWrite<?> ctVariableWrite)
            || !(ctIf.getCondition() instanceof CtBinaryOperator<Boolean> ctBinaryOperator)
            || !Set.of(BinaryOperatorKind.LT, BinaryOperatorKind.LE, BinaryOperatorKind.GE, BinaryOperatorKind.GT, BinaryOperatorKind.EQ)
                .contains(ctBinaryOperator.getKind())) {
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
        if (Set.of(BinaryOperatorKind.GE, BinaryOperatorKind.EQ).contains(condition.getKind())) {
            CtExpression<?> right = condition.getRightHandOperand();

            addLocalProblem(
                ctIf,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", "%s %%= %s".formatted(
                            assignedVariable.prettyprint(),
                            right.prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_MODULO
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtFor(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    super.visitCtFor(ctFor);
                    return;
                }

                checkArrayCopy(ctFor);
                checkStringRepeat(ctFor);
                checkArraysFill(ctFor);
                super.visitCtFor(ctFor);
            }

            @Override
            public void visitCtForEach(CtForEach ctForEach) {
                if (ctForEach.isImplicit() || !ctForEach.getPosition().isValidPosition()) {
                    super.visitCtForEach(ctForEach);
                    return;
                }

                checkAddAll(ctForEach);
                super.visitCtForEach(ctForEach);
            }

            @Override
            public void visitCtIf(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition()) {
                    super.visitCtIf(ctIf);
                    return;
                }

                checkMaxMin(ctIf);
                checkModulo(ctIf);
                super.visitCtIf(ctIf);
            }
        });
    }
}
