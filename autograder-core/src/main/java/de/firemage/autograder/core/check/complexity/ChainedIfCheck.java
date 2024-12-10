package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.FactoryUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.MERGE_NESTED_IF, ProblemType.UNMERGED_ELSE_IF })
public class ChainedIfCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf ctIf) {
                // skip `if (a);` (no block)
                if (ctIf.getThenStatement() == null) {
                    return;
                }

                // We can combine a nested if with the parent if, when the code looks like this:
                // if (a) {
                //     if (b) {
                //         ...
                //     }
                // }
                //
                // ...
                //
                // Because there is
                // - no code before/after the if(b)
                // - there is no explicit else branch
                //
                // If there is an else or else if, we cannot merge the if-statements:
                // if (a) {
                //     if (b) {
                //         ...
                //     }
                // } else {
                //     ...
                // }
                //
                // Because the code would behave differently if the condition a is true and b is false.
                //
                // Technically, one could solve this by adjusting the else to have a condition:
                // } else if (a && !b || !a) {
                //
                // but that is not obvious, which is why it is not suggested.

                // check if the if-statement has a nested if:
                List<CtStatement> thenStatements = StatementUtil.getEffectiveStatements(ctIf.getThenStatement());
                if (// a nested if is exactly one statement
                    thenStatements.size() == 1
                    // the statement must be an if-statement
                    && thenStatements.get(0) instanceof CtIf nestedIf
                    // and that nested if must not have an else branch
                    && (nestedIf.getElseStatement() == null
                        // or if it has one, it should be effectively empty
                        || StatementUtil.getEffectiveStatements(nestedIf.getElseStatement()).isEmpty())
                    // and like described above, there must not be an else branch in the parent if
                    && ctIf.getElseStatement() == null
                ) {
                    addLocalProblem(
                        ctIf.getCondition(),
                        new LocalizedMessage(
                            "merge-nested-if",
                            Map.of(
                                "suggestion", FactoryUtil.createBinaryOperator(
                                    ctIf.getCondition(),
                                    nestedIf.getCondition(),
                                    spoon.reflect.code.BinaryOperatorKind.AND
                                )
                            )
                        ),
                        ProblemType.MERGE_NESTED_IF
                    );
                }

                // Now check if the else branch has a nested if, which could be merged with the parent if:
                CtStatement elseStatement = ctIf.getElseStatement();
                if (!(elseStatement instanceof CtBlock<?> ctBlock) || ctBlock.getStatements().isEmpty()) {
                    return;
                }

                List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctBlock);
                if (statements.size() != 1) {
                    return;
                }

                if (statements.get(0) instanceof CtIf ctElseIf && !elseStatement.isImplicit()) {
                    CtExpression<?> condition = ctElseIf.getCondition();

                    addLocalProblem(
                        ctElseIf.getCondition(),
                        new LocalizedMessage("common-reimplementation", Map.of(
                            "suggestion", "} else if (%s) { ... }".formatted(condition)
                        )),
                        ProblemType.UNMERGED_ELSE_IF
                    );
                }
            }
        });
    }
}
