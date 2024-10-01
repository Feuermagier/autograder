package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.CoreUtil;
import de.firemage.autograder.core.integrated.DuplicateCodeFinder;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = { ProblemType.DUPLICATE_CODE })
public class DuplicateCode extends IntegratedCheck {
    private static final int MINIMUM_DUPLICATE_STATEMENT_SIZE = 10;

    private static String formatSourceRange(CtElement start, CtElement end) {
        SourcePosition startPosition = start.getPosition();
        SourcePosition endPosition = end.getPosition();

        return String.format(
            "%s:%d-%d",
            CoreUtil.getBaseName(startPosition.getFile().getName()),
            startPosition.getLine(),
            endPosition.getEndLine()
        );
    }

    private static boolean isAnyStatementIn(DuplicateCodeFinder.DuplicateCode duplicate, Collection<? extends CtElement> elements) {
        return duplicate.left().stream().anyMatch(elements::contains) || duplicate.right().stream().anyMatch(elements::contains);
    }

    public static boolean isConsideredDuplicateCode(List<CtStatement> left, List<CtStatement> right) {
        var duplicate = new DuplicateCodeFinder.DuplicateCode(left, right);

        if (duplicate.size() < MINIMUM_DUPLICATE_STATEMENT_SIZE) {
            return false;
        }

        MethodUtil.UnnamedMethod leftMethod = MethodUtil.createMethodFrom(null, duplicate.left());
        MethodUtil.UnnamedMethod rightMethod = MethodUtil.createMethodFrom(null, duplicate.right());

        return leftMethod.canBeMethod() && rightMethod.canBeMethod();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        Set<CtElement> reported = Collections.newSetFromMap(new IdentityHashMap<>());
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            private void checkCtStatement(CtStatement ctStatement) {
                if (ctStatement.isImplicit() || !ctStatement.getPosition().isValidPosition()) {
                    return;
                }

                for (var duplicate : DuplicateCodeFinder.findDuplicates(ctStatement)) {
                    if (isAnyStatementIn(duplicate, reported) || !isConsideredDuplicateCode(duplicate.left(), duplicate.right())) {
                        continue;
                    }

                    // prevent duplicate reporting of the same code segments
                    reported.addAll(duplicate.left());
                    reported.addAll(duplicate.right());

                    addLocalProblem(
                        ctStatement,
                        new LocalizedMessage(
                            "duplicate-code",
                            Map.of(
                                "left", formatSourceRange(duplicate.left().get(0), duplicate.left().get(duplicate.left().size() - 1)),
                                "right", formatSourceRange(duplicate.right().get(0), duplicate.right().get(duplicate.right().size() - 1))
                            )
                        ),
                        ProblemType.DUPLICATE_CODE
                    );

                    break;
                }
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                if (ctMethod.isImplicit() || !ctMethod.getPosition().isValidPosition() || ctMethod.getBody() == null) {
                    super.visitCtMethod(ctMethod);
                    return;
                }

                for (CtStatement ctStatement : StatementUtil.getEffectiveStatements(ctMethod.getBody())) {
                    this.checkCtStatement(ctStatement);
                }

                super.visitCtMethod(ctMethod);
            }
        });
    }
}
