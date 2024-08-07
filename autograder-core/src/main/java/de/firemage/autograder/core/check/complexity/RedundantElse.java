package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_ELSE })
public class RedundantElse extends IntegratedCheck {
    private static final int MINIMUM_STATEMENTS = 5;

    private Optional<TerminalEffect> getTerminalEffect(CtStatement ctStatement) {
        List<CtStatement> ctStatements = StatementUtil.getEffectiveStatements(ctStatement);

        if (ctStatements.isEmpty()) return Optional.empty();

        Optional<Effect> optionalEffect = StatementUtil.tryMakeEffect(ctStatements.get(ctStatements.size() - 1));

        return optionalEffect.flatMap(effect -> {
            if (effect instanceof TerminalEffect terminalEffect) {
                return Optional.of(terminalEffect);
            } else {
                return Optional.empty();
            }
        });
    }

    private void checkCtIf(CtIf ctIf) {
        if (ctIf.getThenStatement() == null
            || ctIf.getElseStatement() == null) {
            return;
        }

        TerminalEffect effect = getTerminalEffect(ctIf.getThenStatement()).orElse(null);

        if (effect == null) {
            return;
        }

        String elseIf = "";
        List<CtStatement> elseStatements = StatementUtil.getEffectiveStatements(ctIf.getElseStatement());
        int numberOfElseStatements = elseStatements.size();
        if (elseStatements.size() == 1 && elseStatements.get(0) instanceof CtIf ctElseIf) {
            // skip else { if ... }
            if (!ctIf.getElseStatement().isImplicit()) {
                return;
            }

            // skip else if without an else
            if (ctElseIf.getElseStatement() == null || ctElseIf.getElseStatement().isImplicit()) {
                return;
            }

            // skip if the else if is not terminal:
            if (ctElseIf.getThenStatement() == null || getTerminalEffect(ctElseIf.getThenStatement()).isEmpty()) {
                return;
            }

            numberOfElseStatements = StatementUtil.getEffectiveStatements(ctElseIf.getThenStatement()).size();

            elseIf = " else if (b) { ... }";
        } else if (ctIf.getElseStatement().isImplicit()) {
            return;
        }

        if (numberOfElseStatements <= MINIMUM_STATEMENTS) {
            return;
        }

        addLocalProblem(
            ctIf.getCondition(),
            new LocalizedMessage(
                "redundant-else",
                Map.of(
                    "expected", "if (a) { ... %s; }%s elseCode;".formatted(effect.ctStatement(), elseIf),
                    "given", "if (a) { ... %s; }%s else { elseCode; }".formatted(effect.ctStatement(), elseIf)
                )
            ),
            ProblemType.REDUNDANT_ELSE
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition()) {
                    return;
                }

                // check if the if is in an if with an else statement
                CtIf parentIf = ctIf.getParent(CtIf.class);
                if (parentIf != null && parentIf.getElseStatement() != null) {
                    // if so, then check if the else statement is this if
                    // (to avoid conflicts with else { if } can be else if {})
                    List<CtStatement> ctStatements = StatementUtil.getEffectiveStatements(parentIf.getElseStatement());
                    if (ctStatements.size() == 1 && ctStatements.get(0).equals(ctIf)) {
                        return;
                    }
                }

                checkCtIf(ctIf);
            }
        });
    }
}
