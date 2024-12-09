package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitchExpression;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.visitor.CtScanner;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_VARIABLE})
public class RedundantVariable extends IntegratedCheck {
    private static final int MAX_EXPRESSION_SIZE = 40;

    /**
     * Checks if the given statement does not influence the variable returned by the return statement.
     *
     * @param ctStatement the statement to check
     * @return true if the statement does not influence the variable returned by the return statement
     */
    private boolean isAllowedStatement(CtStatement ctStatement) {
        return ctStatement instanceof CtComment;
    }

    private boolean isComplexExpression(CtExpression<?> ctExpression) {
        return ctExpression instanceof CtSwitchExpression<?,?> || ctExpression.toString().length() > MAX_EXPRESSION_SIZE;
    }

    private void checkVariableRead(CtStatement ctStatement, CtVariableRead<?> ctVariableRead) {
        if (// the variable must be a local variable
            !(ctVariableRead.getVariable().getDeclaration() instanceof CtLocalVariable<?> ctLocalVariable)
            // it should not have any annotations (e.g. @SuppressWarnings("unchecked"))
            || !ctLocalVariable.getAnnotations().isEmpty()
            // the variable must only be used in the return statement
            || UsesFinder.variableUses(ctLocalVariable).filterIndirectParent(CtStatement.class, s -> s != ctStatement).hasAny()) {
            return;
        }

        if (ctLocalVariable.getDefaultExpression() != null
            && this.isComplexExpression(ctLocalVariable.getDefaultExpression())) {
            return;
        }

        CtStatement previousStatement = StatementUtil.getPreviousStatement(ctStatement).orElse(null);

        while (!ctLocalVariable.equals(previousStatement) && this.isAllowedStatement(previousStatement)) {
            previousStatement = StatementUtil.getPreviousStatement(previousStatement).orElse(null);
        }

        if (previousStatement == null) {
            return;
        }

        if (previousStatement.equals(ctLocalVariable)) {
            this.addLocalProblem(
                ctStatement,
                new LocalizedMessage(
                    "redundant-variable",
                    Map.of(
                        "name", ctLocalVariable.getSimpleName(),
                        "suggestion", ctStatement.toString().replace(ctLocalVariable.getSimpleName(), ctLocalVariable.getDefaultExpression().toString())
                    )
                ),
                ProblemType.REDUNDANT_VARIABLE
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtLocalVariable(CtLocalVariable<T> ctLocalVariable) {
                if (!ctLocalVariable.getPosition().isValidPosition()
                    || ctLocalVariable.isImplicit()
                    // only check local variables with a default expression
                    || ctLocalVariable.getDefaultExpression() == null
                    || !(ctLocalVariable.getDefaultExpression() instanceof CtVariableRead<?> ctVariableRead)) {
                    super.visitCtLocalVariable(ctLocalVariable);
                    return;
                }

                checkVariableRead(ctLocalVariable, ctVariableRead);

                super.visitCtLocalVariable(ctLocalVariable);
            }

            @Override
            public <T> void visitCtReturn(CtReturn<T> ctReturn) {
                if (!ctReturn.getPosition().isValidPosition()
                    || ctReturn.isImplicit()
                    // only check returns with a variable
                    || !(ctReturn.getReturnedExpression() instanceof CtVariableRead<?> ctVariableRead)) {
                    super.visitCtReturn(ctReturn);
                    return;
                }

                checkVariableRead(ctReturn, ctVariableRead);

                super.visitCtReturn(ctReturn);
            }
        });
    }
}
