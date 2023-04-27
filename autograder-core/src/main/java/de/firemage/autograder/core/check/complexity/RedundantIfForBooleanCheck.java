package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
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
import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_IF_FOR_BOOLEAN})
public class RedundantIfForBooleanCheck extends IntegratedCheck {
    private LocalizedMessage formatReturnProblem(CtExpression<?> expression, boolean negate) {
        return new LocalizedMessage("redundant-if-for-bool-exp-return", Map.of(
            "exp", (negate ? "!" : "") + expression
        ));
    }

    private LocalizedMessage formatAssignProblem(CtExpression<?> expression, CtExpression<?> target, boolean negate) {
        return new LocalizedMessage("redundant-if-for-bool-exp-assign", Map.of(
            "exp", (negate ? "!" : "") + expression,
            "target", target.toString()
        ));
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
                    addLocalProblem(condition, formatReturnProblem(condition, false),
                        ProblemType.REDUNDANT_IF_FOR_BOOLEAN);
                } else if (!thenValue.get() && elseValue.get()) {
                    addLocalProblem(condition, formatReturnProblem(condition, true),
                        ProblemType.REDUNDANT_IF_FOR_BOOLEAN);
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
                    addLocalProblem(condition, formatAssignProblem(condition, thenAssign.getAssigned(), false),
                        ProblemType.REDUNDANT_IF_FOR_BOOLEAN);
                } else if (!thenValue.get() && elseValue.get()) {
                    addLocalProblem(condition, formatAssignProblem(condition, thenAssign.getAssigned(), true),
                        ProblemType.REDUNDANT_IF_FOR_BOOLEAN);
                }
            }
        }
    }
}
