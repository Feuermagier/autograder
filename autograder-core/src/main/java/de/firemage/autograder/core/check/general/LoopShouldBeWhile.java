package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtWhile;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.LOOP_SHOULD_BE_WHILE })
public class LoopShouldBeWhile extends IntegratedCheck {
    private static CtWhile createCtWhile(
        CtExpression<Boolean> condition,
        CtStatement body
    ) {
        CtWhile result = body.getFactory().Core().createWhile();

        if (condition != null) {
            result.setLoopingExpression(condition.clone());
        }
        result.setBody(body.clone());
        return result;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition() || ctFor.getBody() == null) {
                    return;
                }

                List<CtStatement> forInit = ctFor.getForInit();
                CtExpression<Boolean> condition = ctFor.getExpression();
                List<CtStatement> forUpdate = ctFor.getForUpdate();

                if (condition == null || !forInit.isEmpty() || !forUpdate.isEmpty()) {
                    return;
                }

                List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctFor.getBody());

                if (statements.isEmpty()) {
                    return;
                }

                addLocalProblem(
                    ctFor,
                    new LocalizedMessage(
                        "loop-should-be-while",
                        Map.of(
                            "suggestion", "%n%s".formatted(createCtWhile(condition, ctFor.getBody()).toString().stripTrailing())
                        )
                    ),
                    ProblemType.LOOP_SHOULD_BE_WHILE
                );
            }
        });
    }
}
