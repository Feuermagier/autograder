package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.visitor.CtScanner;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_VARIABLE_BEFORE_RETURN})
public class RedundantVariableBeforeReturn extends IntegratedCheck {
    /**
     * Checks if the given statement does not influence the variable returned by the return statement.
     *
     * @param ctStatement the statement to check
     * @return true if the statement does not influence the variable returned by the return statement
     */
    private boolean isAllowedStatement(CtStatement ctStatement) {
        return ctStatement instanceof CtComment;
    }

    private void checkVariableRead(CtStatement ctStatement, CtVariableRead<?> ctVariableRead) {
        if (// the variable must be a local variable
            !(ctVariableRead.getVariable().getDeclaration() instanceof CtLocalVariable<?> ctLocalVariable)
            // it should not have any annotations (e.g. @SuppressWarnings("unchecked"))
            || !ctLocalVariable.getAnnotations().isEmpty()
            // the variable must only be used in the return statement
            || SpoonUtil.findUsesOf(ctLocalVariable).size() != 1) {
            return;
        }

        CtStatement previousStatement = SpoonUtil.getPreviousStatement(ctStatement).orElse(null);

        while (!ctLocalVariable.equals(previousStatement) && this.isAllowedStatement(previousStatement)) {
            previousStatement = SpoonUtil.getPreviousStatement(previousStatement).orElse(null);
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
                        "suggestion", ctStatement.prettyprint().replace(ctLocalVariable.getSimpleName(), ctLocalVariable.getDefaultExpression().prettyprint())
                    )
                ),
                ProblemType.REDUNDANT_VARIABLE_BEFORE_RETURN
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtInvocation(CtInvocation<T> ctInvocation) {
                if (!ctInvocation.getPosition().isValidPosition()
                    || ctInvocation.isImplicit()
                    // only check invocations with a single variable
                    || ctInvocation.getArguments().size() != 1
                    || !(ctInvocation.getArguments().get(0) instanceof CtVariableRead<?> ctVariableRead)) {
                    super.visitCtInvocation(ctInvocation);
                    return;
                }

                checkVariableRead(ctInvocation, ctVariableRead);

                super.visitCtInvocation(ctInvocation);
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
