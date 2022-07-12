package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;

import java.util.List;
import java.util.Optional;

public class RedundantIfForBooleanCheck extends IntegratedCheck {

    public RedundantIfForBooleanCheck() {
        super(
            "It is unnecessary to assign/return boolean literals values in ifs - you can just assign/return the condition directly");
    }

    private String formatReturnProblem(CtExpression<?> expression, boolean negate) {
        return "Directly return " + String.format(negate ? "'!(%s)'" : "'%s'", expression) +
            " instead of wrapping it in an if";
    }

    private String formatAssignProblem(CtExpression<?> expression, CtExpression<?> target, boolean negate) {
        return "Directly assign " + String.format(negate ? "'!(%s)'" : "'%s'", expression) +
            "to '" + target + "' instead of wrapping it in an if";
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBlock<?>>() {
            @Override
            public void process(CtBlock<?> block) {
                List<CtStatement> statements = SpoonUtil.getEffectiveStatements(block);
                for (int i = 0; i < statements.size(); i++) {
                    CtStatement statement = statements.get(i);

                    if (statement instanceof CtIf ifStmt) {
                        // if (...) return true else return false
                        // or if(...) return true \ return false
                        if (ifStmt.getElseStatement() != null) {
                            checkIfElseReturn(ifStmt.getCondition(),
                                SpoonUtil.unwrapStatement(ifStmt.getThenStatement()),
                                SpoonUtil.unwrapStatement(ifStmt.getElseStatement()));
                            checkIfElseAssign(ifStmt.getCondition(),
                                SpoonUtil.unwrapStatement(ifStmt.getThenStatement()),
                                SpoonUtil.unwrapStatement(ifStmt.getElseStatement()));
                        } else if (i + 1 < statements.size()) {
                            checkIfElseReturn(ifStmt.getCondition(),
                                SpoonUtil.unwrapStatement(ifStmt.getThenStatement()), statements.get(i + 1));
                        }
                    }
                }
            }
        });
    }

    private void checkIfElseReturn(CtExpression<?> condition, CtStatement thenStmt, CtStatement elseStmt) {
        if (thenStmt instanceof CtReturn<?> thenRet && elseStmt instanceof CtReturn<?> elseRet) {
            Optional<Boolean> thenValue = SpoonUtil.tryGetBooleanLiteral(thenRet.getReturnedExpression());
            Optional<Boolean> elseValue = SpoonUtil.tryGetBooleanLiteral(elseRet.getReturnedExpression());
            if (thenValue.isPresent() && elseValue.isPresent()) {
                if (thenValue.get() && !elseValue.get()) {
                    addLocalProblem(condition, formatReturnProblem(condition, false));
                } else if (!thenValue.get() && elseValue.get()) {
                    addLocalProblem(condition, formatReturnProblem(condition, true));
                }
                // Otherwise we have if (...) return true else return true ... it's not our task to handle such nonsense
            }
        }
    }

    private void checkIfElseAssign(CtExpression<?> condition, CtStatement thenStmt, CtStatement elseStmt) {
        if (thenStmt instanceof CtAssignment<?, ?> thenAssign && elseStmt instanceof CtAssignment<?, ?> elseAssign) {
            Optional<Boolean> thenValue = SpoonUtil.tryGetBooleanLiteral(thenAssign.getAssignment());
            Optional<Boolean> elseValue = SpoonUtil.tryGetBooleanLiteral(elseAssign.getAssignment());
            if (thenValue.isPresent() && elseValue.isPresent() &&
                thenAssign.getAssigned().equals(elseAssign.getAssigned())) {
                if (thenValue.get() && !elseValue.get()) {
                    addLocalProblem(condition, formatAssignProblem(condition, thenAssign.getAssigned(), false));
                } else if (!thenValue.get() && elseValue.get()) {
                    addLocalProblem(condition, formatAssignProblem(condition, thenAssign.getAssigned(), true));
                }
            }
        }
    }
}
