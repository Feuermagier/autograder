package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.MERGE_NESTED_IF, ProblemType.UNMERGED_ELSE_IF })
public class ChainedIfCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf ctIf) {
                // check if the if-statement has a nested if:
                List<CtStatement> thenStatements = SpoonUtil.getEffectiveStatements(ctIf.getThenStatement());
                if (thenStatements.size() == 1
                    && thenStatements.get(0) instanceof CtIf nestedIf
                    && (nestedIf.getElseStatement() == null
                        || SpoonUtil.getEffectiveStatements(nestedIf.getElseStatement()).isEmpty())) {
                    addLocalProblem(
                        ctIf.getCondition(),
                        new LocalizedMessage(
                            "merge-nested-if",
                            Map.of(
                                "suggestion", ctIf.getFactory().createBinaryOperator(
                                    ctIf.getCondition(),
                                    nestedIf.getCondition(),
                                    spoon.reflect.code.BinaryOperatorKind.AND
                                ).prettyprint()
                            )
                        ),
                        ProblemType.MERGE_NESTED_IF
                    );
                }

                CtStatement elseStatement = ctIf.getElseStatement();
                if (!(elseStatement instanceof CtBlock<?> ctBlock) || ctBlock.getStatements().isEmpty()) {
                    return;
                }

                List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctBlock);
                if (statements.size() != 1) {
                    return;
                }

                if (statements.get(0) instanceof CtIf ctElseIf && !elseStatement.isImplicit()) {
                    addLocalProblem(
                        ctElseIf.getCondition(),
                        new LocalizedMessage("merge-else-if"),
                        ProblemType.UNMERGED_ELSE_IF
                    );
                }
            }
        });
    }
}
