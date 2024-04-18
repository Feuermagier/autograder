package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtWhile;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;


@ExecutableCheck(reportedProblems = { ProblemType.LOOP_SHOULD_BE_DO_WHILE })
public class LoopShouldBeDoWhile extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtWhile>() {
            @Override
            public void process(CtWhile ctWhile) {
                if (ctWhile.isImplicit() || !ctWhile.getPosition().isValidPosition() || ctWhile.getBody() == null) {
                    return;
                }

                // This check detects loops that should be do-while loops, for example:
                //
                // ```
                // a;
                // b;
                // c;
                // while (condition) {
                //   a;
                //   b;
                //   c;
                // }
                // ```
                //
                // ```
                // a;
                // b;
                // c;
                // while (condition) {
                //   b;
                //   c;
                // }
                // ```
                //
                // ```
                // a;
                // b;
                // c;
                // while (condition) {
                //   a;
                //   b;
                // }
                // ```

                Deque<CtStatement> loopStatements = new ArrayDeque<>(SpoonUtil.getEffectiveStatements(ctWhile.getBody()));
                CtStatement currentStatementBeforeLoop = SpoonUtil.getPreviousStatement(ctWhile).orElse(null);
                if (currentStatementBeforeLoop == null || loopStatements.isEmpty()) {
                    return;
                }

                while (!loopStatements.isEmpty() && currentStatementBeforeLoop != null) {
                    CtStatement lastStatement = loopStatements.removeLast();

                    if (!lastStatement.equals(currentStatementBeforeLoop)) {
                        return;
                    }

                    currentStatementBeforeLoop = SpoonUtil.getPreviousStatement(currentStatementBeforeLoop).orElse(null);
                }

                if (loopStatements.isEmpty()) {
                    addLocalProblem(
                        ctWhile.getLoopingExpression(),
                        new LocalizedMessage(
                            "loop-should-be-do-while",
                            Map.of("suggestion", """
                            %ndo %s while (%s)""".formatted(SpoonUtil.truncatedSuggestion(ctWhile.getBody()), ctWhile.getLoopingExpression()))
                        ),
                        ProblemType.LOOP_SHOULD_BE_DO_WHILE
                    );
                }
            }
        });
    }
}
