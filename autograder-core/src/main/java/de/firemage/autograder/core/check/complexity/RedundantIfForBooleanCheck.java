package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.effects.AssignmentEffect;
import de.firemage.autograder.core.integrated.effects.Effect;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_IF_FOR_BOOLEAN })
public class RedundantIfForBooleanCheck extends IntegratedCheck {
    private static final Set<String> METHODS_TO_IGNORE = Set.of("equals", "hashCode");

    private String makeSuggestion(CtExpression<?> ctExpression, Effect thenEffect, Effect elseEffect) {
        if (thenEffect instanceof AssignmentEffect thenAssignment && elseEffect instanceof AssignmentEffect) {
            return "%s = %s".formatted(
                thenAssignment.target(),
                ctExpression
            );
        }

        // otherwise it is a return statement
        return "return %s".formatted(ctExpression);
    }

    private void checkIfElse(CtExpression<?> condition, CtStatement thenStmt, CtStatement elseStmt) {
        Effect thenEffect = SpoonUtil.tryMakeEffect(thenStmt).orElse(null);
        Effect elseEffect = SpoonUtil.tryMakeEffect(elseStmt).orElse(null);

        // skip if they are not both return statements or both assignments to the same variable
        if (thenEffect == null
            || elseEffect == null
            || !thenEffect.isSameEffect(elseEffect)
            || (!(thenStmt instanceof CtReturn<?>) && !(thenStmt instanceof CtAssignment<?, ?>))
            || thenEffect.value().isEmpty()
            || elseEffect.value().isEmpty()) {
            return;
        }

        CtExpression<?> thenValue = thenEffect.value().get();
        CtExpression<?> elseValue = elseEffect.value().get();

        // skip if they do not assign or return a boolean expression
        if (!SpoonUtil.isBoolean(thenValue) || !SpoonUtil.isBoolean(elseValue)) {
            return;
        }

        Boolean thenLiteral = SpoonUtil.tryGetBooleanLiteral(thenValue).orElse(null);
        Boolean elseLiteral = SpoonUtil.tryGetBooleanLiteral(elseValue).orElse(null);

        // skip non-sense like if (a) return true else return true
        if (thenLiteral != null && thenLiteral.equals(elseLiteral)) {
            return;
        }

        CtExpression<?> thenCondition = null;

        if (thenLiteral == null) {
            // if it does not return a literal, both the condition and the return value must be true
            thenCondition = SpoonUtil.createBinaryOperator(
                condition,
                thenValue,
                BinaryOperatorKind.AND
            );
        } else if (thenLiteral) {
            // if it returns true, then the if condition must be true
            thenCondition = condition;
        }

        CtExpression<?> combinedCondition = thenCondition;

        if (elseLiteral == null) {
            // if it does not return a literal in the else, either the thenCondition or the elseCondition must be true
            //
            // if (a) { return b; } else { return c; } -> return a && b || c;

            if (thenCondition == null) {
                combinedCondition = SpoonUtil.createBinaryOperator(
                    SpoonUtil.negate(condition),
                    elseValue,
                    BinaryOperatorKind.AND
                );
            } else {
                combinedCondition = SpoonUtil.createBinaryOperator(
                    combinedCondition,
                    elseValue,
                    BinaryOperatorKind.OR
                );
            }
        } else if (elseLiteral) {
            // if it does return true, then the if condition must be false
            if (thenCondition == null) {
                combinedCondition = SpoonUtil.negate(condition);
            } else {
                combinedCondition = SpoonUtil.createBinaryOperator(
                    combinedCondition,
                    SpoonUtil.negate(condition),
                    BinaryOperatorKind.OR
                );
            }
        }

        addLocalProblem(
            condition,
            new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", makeSuggestion(combinedCondition, thenEffect, elseEffect)
                )
            ),
            ProblemType.REDUNDANT_IF_FOR_BOOLEAN
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBlock<?>>() {
            @Override
            public void process(CtBlock<?> block) {
                CtMethod<?> parentMethod = block.getParent(CtMethod.class);
                if (parentMethod != null && METHODS_TO_IGNORE.contains(parentMethod.getSimpleName())) {
                    return;
                }

                List<CtStatement> statements = SpoonUtil.getEffectiveStatements(block);

                // TODO: write test

                for (int i = 0; i < statements.size(); i++) {
                    CtStatement statement = statements.get(i);

                    if (!(statement instanceof CtIf ifStmt) || ifStmt.getThenStatement() == null) {
                        continue;
                    }

                    CtStatement thenStatement = SpoonUtil.unwrapStatement(ifStmt.getThenStatement());
                    CtStatement elseStatement = ifStmt.getElseStatement();

                    // if(...) { return true } return false
                    if (elseStatement == null && i + 1 < statements.size()) {
                        elseStatement = statements.get(i + 1);
                    }

                    if (elseStatement == null) {
                        continue;
                    }

                    checkIfElse(
                        ifStmt.getCondition(),
                        thenStatement,
                        SpoonUtil.unwrapStatement(elseStatement)
                    );
                }
            }
        });
    }
}
