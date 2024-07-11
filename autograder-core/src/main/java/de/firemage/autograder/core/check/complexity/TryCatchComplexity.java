package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtWhile;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The TryCatchComplexity check checks that the number of statements in a try-catch block
 * is not too high. (lower than {@link TryCatchComplexity#MAX_ALLOWED_STATEMENTS}). Note that this check does not count
 * every statement. Blocks are not counted as statements for example:
 * {@code
 *  {
 *      int a = 0;
 *      int b = 0;
 *  }
 * }
 * Counts as two statements. The for loop counts as one statement as well as the if statement, switch, for-each, while,
 * do-while etc.
 * Nested statements are counted as well (ignoring nested statements of method invocations).
 */
@ExecutableCheck(reportedProblems = {ProblemType.TRY_CATCH_COMPLEXITY})
public class TryCatchComplexity extends IntegratedCheck {
    public static final int MAX_ALLOWED_STATEMENTS = 15;
    public static final String LOCALIZED_MESSAGE_KEY = "try-catch-complexity";

    @Override
    public void check(StaticAnalysis staticAnalysis) {
       staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
              @Override
              public void visitCtTry(CtTry ctTry) {
                  List<CtStatement> statements = new ArrayList<>();
                  visitNestedStatement(ctTry, statements);
                  if (statements.size() > MAX_ALLOWED_STATEMENTS) {
                      addLocalProblem(ctTry,
                              new LocalizedMessage(LOCALIZED_MESSAGE_KEY, Map.of("max", MAX_ALLOWED_STATEMENTS)),
                              ProblemType.TRY_CATCH_COMPLEXITY
                      );
                  }
              }
         });
    }
    private void visitNestedStatement(CtTry ctTry, List<CtStatement> allStatements) {
        // filter method invocations and constructor calls to avoid counting nested statements of method invocations
        List<CtStatement> stats = filterMethodInvocations(ctTry.getBody().getStatements());
        stats.forEach(statement -> visitStatement(statement, allStatements));
        allStatements.addAll(extractMethodInvocations(ctTry.getBody().getStatements()));
    }
    private List<CtStatement> filterMethodInvocations(List<CtStatement> statements) {
        return statements.stream()
                .filter(statement -> !SpoonUtil.isInvocation(statement))
                .toList();
    }

    private List<CtStatement> extractMethodInvocations(List<CtStatement> statements) {
        return statements.stream().filter(SpoonUtil::isInvocation).toList();
    }
    private void visitStatement(CtStatement statement, List<CtStatement> allStatements) {
        // The CtStatement may be null if the body of an if statement is empty. for example "if (true);"
        if (statement == null || allStatements.size() > MAX_ALLOWED_STATEMENTS) {
            return;
        }
        // avoid adding blocks to the list of allStatements
        if (!(statement instanceof CtBlock)) {
            allStatements.add(statement);
        }
        statement.accept(new CtScanner() {
            @Override
            public void visitCtTry(CtTry ctTry) {
                visitNestedStatement(ctTry, allStatements);
                List<CtCatch> catchers = ctTry.getCatchers();
                catchers.stream().flatMap(catcher -> catcher.getBody().getStatements().stream())
                        .filter(statement -> !SpoonUtil.isInvocation(statement))
                        .forEach(statement -> visitStatement(statement, allStatements));

                allStatements.addAll(extractMethodInvocations(catchers
                        .stream().flatMap(catcher -> catcher.getBody().getStatements().stream())
                        .toList())
                );
            }
            @Override
            public void visitCtIf(CtIf ctIf) {
                visitStatement(ctIf.getThenStatement(), allStatements);
                visitStatement(ctIf.getElseStatement(), allStatements);
            }
            @Override
            public <R> void visitCtBlock(CtBlock<R> block) {
                List<CtStatement> blockStatements = block.getStatements();
                filterMethodInvocations(blockStatements)
                        .forEach(statement -> visitStatement(statement, allStatements));
                allStatements.addAll(extractMethodInvocations(blockStatements));
            }
            @Override
            public void visitCtDo(CtDo doLoop) {
                visitStatement(doLoop.getBody(), allStatements);
            }
            @Override
            public void visitCtCase(CtCase ctCase) {
                allStatements.add(ctCase);
                filterMethodInvocations(ctCase.getStatements())
                        .forEach(statement -> visitStatement(statement, allStatements));
                allStatements.addAll(extractMethodInvocations(ctCase.getStatements()));
            }
            @Override
            public void visitCtFor(CtFor forLoop) {
                visitStatement(forLoop.getBody(), allStatements);
            }
            @Override
            public void visitCtForEach(CtForEach foreach) {
                visitStatement(foreach.getBody(), allStatements);
            }
            @Override
            public void visitCtWhile(CtWhile whileLoop) {
                visitStatement(whileLoop.getBody(), allStatements);
            }
            @Override
            public void visitCtLambda(CtLambda lambda) {
                allStatements.add(lambda.getBody());
            }
        });
    }
}
