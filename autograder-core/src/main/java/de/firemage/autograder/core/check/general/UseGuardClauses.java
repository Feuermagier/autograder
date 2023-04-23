package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

import java.util.List;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.USE_GUARD_CLAUSES })
public class UseGuardClauses extends IntegratedCheck {
    public UseGuardClauses() {
        super(new LocalizedMessage("use-guard-clauses"));
    }

    private void reportProblem(CtStatement ctStatement, CtExpression<?> condition) {
        addLocalProblem(
            ctStatement,
            new LocalizedMessage("use-guard-clauses"),
            ProblemType.USE_GUARD_CLAUSES
        );
    }

    private static <T> CtUnaryOperator<T> makeCtUnaryOperator(CtExpression<T> ctExpression, UnaryOperatorKind kind) {
        CtUnaryOperator<T> ctUnaryOperator = ctExpression.getFactory().createUnaryOperator();

        ctUnaryOperator.setKind(kind);
        ctUnaryOperator.setOperand(ctExpression);

        return ctUnaryOperator;
    }

    private boolean isTerminal(CtStatement ctStatement) {
        List<CtStatement> ctStatements = SpoonUtil.getEffectiveStatements(ctStatement);

        if (ctStatements.isEmpty()) return false;


        Optional<Effect> optionalEffect = SpoonUtil.tryMakeEffect(ctStatements.get(ctStatements.size() - 1));

        return optionalEffect.map(TerminalEffect.class::isInstance).orElse(false);
    }

    private void checkCtIf(CtIf ctIf, CtExpression<?> condition) {
        // if the condition != null, then the ctIf is an else if
        if (condition != null) {
            CtExpression<?> ifCondition = ctIf.getFactory().createBinaryOperator(
                condition,
                ctIf.getCondition(),
                BinaryOperatorKind.AND
            );

            if (this.isTerminal(ctIf.getThenStatement())) {
                reportProblem(ctIf.getThenStatement(), ifCondition);
            }
        }

        // the condition to reach the else statement
        CtExpression<?> elseCondition = makeCtUnaryOperator(
            ctIf.getCondition(),
            UnaryOperatorKind.NOT
        );

        // check the else statement
        CtStatement ctStatement = ctIf.getElseStatement();
        // if there is no else, return
        if (ctStatement == null) return;

        List<CtStatement> ctStatements = SpoonUtil.getEffectiveStatements(ctStatement);

        if (condition != null) {
            elseCondition = ctIf.getFactory().createBinaryOperator(
                condition,
                elseCondition,
                BinaryOperatorKind.AND
            );
        }

        if (ctStatements.size() == 1 && ctStatements.get(0) instanceof CtIf ctElseIf) {
            CtExpression<?> elseIfCondition = ctIf.getFactory().createBinaryOperator(
                elseCondition,
                ctElseIf.getCondition(),
                BinaryOperatorKind.AND
            );

            checkCtIf(ctElseIf, elseIfCondition);
            // check the else statement
        } else if (this.isTerminal(ctStatement)) {
            reportProblem(ctStatement, elseCondition);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition()) return;

                CtIf parentIf = ctIf.getParent(CtIf.class);
                if (parentIf != null && parentIf.getElseStatement() != null) {
                    List<CtStatement> ctStatements = SpoonUtil.getEffectiveStatements(parentIf.getElseStatement());
                    if (ctStatements.size() == 1 && ctStatements.get(0).equals(ctIf)) {
                        return;
                    }
                }

                checkCtIf(ctIf, null);
            }
        });
    }
}
