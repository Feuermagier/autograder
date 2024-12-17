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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.DUPLICATE_IF_BLOCK })
public class DuplicateIfBlock extends IntegratedCheck {
    private static List<CtStatement> getElseStatements(CtIf ctIf, List<CtStatement> followingStatements) {
        List<CtStatement> elseStatements;
        if (ctIf.getElseStatement() == null) {
            // check if the if block is terminal, because when it is not, we cannot merge it with the following statements
            // with the if block, because they are executed after the if block has been executed.
            //
            // if (condition) {
            //      System.out.println("a");
            // }
            //
            // System.out.println("a");
            //
            // if (otherCondition) {
            //      System.out.println("a");
            // }
            if (ctIf.getThenStatement() == null || !StatementUtil.isTerminal(ctIf.getThenStatement())) {
                return null;
            }

            elseStatements = followingStatements;
        } else {
            // there is an explicit else block
            List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctIf.getElseStatement());
            // if the then block is not terminal, we cannot merge the else block if there is more than an if statement in it
            //
            // if (condition) {
            //    System.out.println("a");
            // } else {
            //     if (otherCondition) {
            //         System.out.println("a");
            //     }
            //
            //     System.out.println("b");
            // }
            if (ctIf.getThenStatement() != null && !StatementUtil.isTerminal(ctIf.getThenStatement()) && statements.size() > 1) {
                return null;
            }

            elseStatements = new ArrayList<>(statements);
            // if the then block is terminal, we can merge the else block with the following statements
            if (ctIf.getThenStatement() != null && StatementUtil.isTerminal(ctIf.getThenStatement())) {
                elseStatements.addAll(followingStatements);
            }
        }

        return elseStatements;
    }

    // This function extracts duplicates from the if/else-if/else blocks that could be merged into one if block.
    private static List<CtIf> listDuplicates(CtIf ctIf, Predicate<? super CtStatement> isDuplicate) {
        List<CtIf> result = new ArrayList<>();

        List<CtStatement> elseStatements = getElseStatements(ctIf, StatementUtil.getNextStatements(ctIf));
        if (elseStatements == null) {
            return result;
        }

        Queue<CtStatement> queue = new ArrayDeque<>(elseStatements);
        while (!queue.isEmpty()) {
            CtStatement statement = queue.poll();

            // these are the conditions for merging the previous if blocks with the current one:
            if (!(statement instanceof CtIf nextIf) || nextIf.getThenStatement() == null || !isDuplicate.test(nextIf.getThenStatement())) {
                break;
            }

            // Now we need to check the else block, for example in the following code one can not merge the if blocks:
            //
            // if (i <= 0) {
            //     System.out.println("a");
            // } else {
            //     if (i + i > 1) {
            //         System.out.println("a");
            //     } else {
            //         System.out.println("c");
            //     }
            // }

            // if there is no else block, we can merge the if blocks
            if (nextIf.getElseStatement() == null && StatementUtil.isTerminal(nextIf.getThenStatement())) {
                result.add(nextIf);
                continue;
            }

            // Otherwise call the function which adjusts the else statements for possible merging
            List<CtStatement> nextElseStatements = getElseStatements(nextIf, new ArrayList<>(queue));
            if (nextElseStatements == null) {
                break;
            }

            // the else block seems to be mergeable:
            if (nextElseStatements.isEmpty()) {
                result.add(nextIf);
                break;
            }

            result.add(nextIf);
            queue = new ArrayDeque<>(nextElseStatements);
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

                List<CtIf> duplicates = listDuplicates(
                    ctIf,
                    ctStatement -> DuplicateCatchBlock.isDuplicateBlock(ctIf.getThenStatement(), ctStatement, difference -> false)
                );

                if (duplicates.stream().anyMatch(visited::contains)) {
                    return;
                }

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
