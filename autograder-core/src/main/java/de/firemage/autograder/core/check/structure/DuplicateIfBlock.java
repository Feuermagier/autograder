package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.exceptions.DuplicateCatchBlock;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.DUPLICATE_IF_BLOCK })
public class DuplicateIfBlock extends IntegratedCheck {
    private static List<CtIf> getDuplicates(CtIf ctIf, Predicate<? super CtStatement> isDuplicate) {
        List<CtIf> result = new ArrayList<>();

        List<CtStatement> followingStatements;
        if (ctIf.getElseStatement() == null) {
            // the if does not have an else, so check if the following statements are duplicate if blocks
            followingStatements = StatementUtil.getNextStatements(ctIf);


        } else {
            // the if has an else, so check if the else has an if with a duplicate block
            followingStatements = StatementUtil.getEffectiveStatements(ctIf.getElseStatement());
        }

        for (var statement : followingStatements) {
            if (!(statement instanceof CtIf nextIf) || nextIf.getThenStatement() == null || !isDuplicate.test(nextIf.getThenStatement())) {
                break;
            }

            if (nextIf.getElseStatement() == null) {
                result.add(nextIf);
            }
        }

        return result;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        Set<CtElement> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        staticAnalysis.processWith(new AbstractProcessor<CtIf>() {
            @Override
            public void process(CtIf ctIf) {
                if (ctIf.isImplicit() || !ctIf.getPosition().isValidPosition() || visited.contains(ctIf)) {
                    return;
                }

                // what we check:
                // - if (condition) followed by another if (condition)
                // - if (condition) with an else { if (condition) }

                // for the merging to work, the if block must be terminal (i.e. ends with return, throw, break, continue)
                if (ctIf.getThenStatement() == null || !StatementUtil.isTerminal(ctIf.getThenStatement())) {
                    return;
                }

                List<CtIf> duplicates = getDuplicates(ctIf, ctStatement -> DuplicateCatchBlock.isDuplicateBlock(ctIf.getThenStatement(), ctStatement, difference -> false));

                if (!duplicates.isEmpty()) {
                    visited.add(ctIf);
                    visited.addAll(duplicates);
                    addLocalProblem(
                        ctIf,
                        new LocalizedMessage("common-reimplementation", Map.of(
                            "suggestion", "if (%s || %s) { ... }".formatted(
                                ctIf.getCondition(),
                                duplicates.stream()
                                    .map(CtIf::getCondition)
                                    .map(CtElement::toString)
                                    .collect(Collectors.joining(" || "))
                            )
                        )),
                        ProblemType.DUPLICATE_IF_BLOCK
                    );
                }
            }
        });
    }
}
