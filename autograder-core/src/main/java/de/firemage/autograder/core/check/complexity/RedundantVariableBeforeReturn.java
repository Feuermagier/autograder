package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;

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


    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtReturn<?>>() {
            @Override
            public void process(CtReturn<?> ctReturn) {
                if (!ctReturn.getPosition().isValidPosition()
                    || ctReturn.isImplicit()
                    // only check returns with a variable
                    || !(ctReturn.getReturnedExpression() instanceof CtVariableRead<?> ctVariableRead)
                    // the variable must be a local variable
                    || !(ctVariableRead.getVariable().getDeclaration() instanceof CtLocalVariable<?> ctLocalVariable)
                    // it should not have any annotations (e.g. @SuppressWarnings("unchecked"))
                    || !ctLocalVariable.getAnnotations().isEmpty()
                    // the variable must only be used in the return statement
                    || SpoonUtil.findUsesOf(ctLocalVariable).size() != 1) {
                    return;
                }

                CtStatement previousStatement = SpoonUtil.getPreviousStatement(ctReturn).orElse(null);

                while (!ctLocalVariable.equals(previousStatement) && isAllowedStatement(previousStatement)) {
                    previousStatement = SpoonUtil.getPreviousStatement(previousStatement).orElse(null);
                }

                if (previousStatement == null) {
                    return;
                }

                if (previousStatement.equals(ctLocalVariable)) {
                    addLocalProblem(
                        ctReturn,
                        new LocalizedMessage(
                            "redundant-variable",
                            Map.of(
                                "name", ctLocalVariable.getSimpleName(),
                                "value", ctLocalVariable.getDefaultExpression().prettyprint()
                            )
                        ),
                        ProblemType.REDUNDANT_VARIABLE_BEFORE_RETURN
                    );
                }
            }
        });
    }
}
