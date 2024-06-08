package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.structure.StructuralElement;
import de.firemage.autograder.core.integrated.structure.StructuralEqualsVisitor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
            SpoonUtil.getBaseName(startPosition.getFile().getName()),
            startPosition.getLine(),
            endPosition.getEndLine()
        );
    }

    private static int countStatements(CtStatement ctStatement) {
        int count = ctStatement.getElements(ctElement -> ctElement instanceof CtStatement
            && !(ctElement instanceof CtComment)
            && !(ctElement instanceof CtStatementList)
            && ctElement.getPosition().isValidPosition()
            && !ctElement.isImplicit()
        ).size();

        return Math.max(count, 1);
    }

    private static <K, V> Iterable<Map.Entry<K, V>> zip(Iterable<K> keys, Iterable<V> values) {
        return () -> new Iterator<>() {
            private final Iterator<K> keyIterator = keys.iterator();
            private final Iterator<V> valueIterator = values.iterator();

            @Override
            public boolean hasNext() {
                return this.keyIterator.hasNext() && this.valueIterator.hasNext();
            }

            @Override
            public Map.Entry<K, V> next() {
                return Map.entry(this.keyIterator.next(), this.valueIterator.next());
            }
        };
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        Map<StructuralElement<CtStatement>, List<CtStatement>> occurrences = new HashMap<>();
        staticAnalysis.getModel().processWith(new AbstractProcessor<CtStatement>() {
            @Override
            public void process(CtStatement ctStatement) {
                if (ctStatement.isImplicit() || !ctStatement.getPosition().isValidPosition()) {
                    return;
                }

                occurrences.computeIfAbsent(
                    new StructuralElement<>(ctStatement),
                    key -> new ArrayList<>()
                ).add(ctStatement);
            }
        });

        /*
        Map<Integer, List<Object>> collisions = new HashMap<>();
        for (var key : occurrences.keySet()) {
            collisions.computeIfAbsent(key.hashCode(), k -> new ArrayList<>()).add(key);
        }

        System.out.println("Number of duplicate hashCodes: " + (occurrences.size() - collisions.size()) + " of " + occurrences.size() + " elements");*/

        Set<CtElement> reported = new HashSet<>();
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            private void checkCtStatement(CtStatement ctStatement) {
                if (ctStatement.isImplicit() || !ctStatement.getPosition().isValidPosition()) {
                    return;
                }

                // TODO: use a debug mode to compare that this implementation yields the same results as the occurrences map
                /*List<CtStatement> duplicates = ctStatement.getFactory()
                    .getModel()
                    .filterChildren(ctElement -> ctElement instanceof CtStatement otherStatement
                        && otherStatement != ctStatement
                        && StructuralEqualsVisitor.equals(ctStatement, otherStatement)
                    ).list(CtStatement.class);*/
                List<CtStatement> duplicates = occurrences.get(new StructuralElement<>(ctStatement));

                int initialSize = countStatements(ctStatement);
                for (CtStatement duplicate : duplicates) {
                    if (duplicate == ctStatement || reported.contains(duplicate) || reported.contains(ctStatement)) {
                        continue;
                    }

                    int duplicateStatementSize = initialSize;

                    List<CtStatement> leftCode = new ArrayList<>(List.of(ctStatement));
                    List<CtStatement> rightCode = new ArrayList<>(List.of(duplicate));

                    for (var entry : zip(SpoonUtil.getNextStatements(ctStatement), SpoonUtil.getNextStatements(duplicate))) {
                        if (!StructuralEqualsVisitor.equals(entry.getKey(), entry.getValue())) {
                            break;
                        }

                        leftCode.add(entry.getKey());
                        rightCode.add(entry.getValue());
                        duplicateStatementSize += countStatements(entry.getKey());
                    }

                    if (duplicateStatementSize >= MINIMUM_DUPLICATE_STATEMENT_SIZE) {
                        // prevent duplicate reporting of the same code segments
                        reported.addAll(leftCode);
                        reported.addAll(rightCode);

                        addLocalProblem(
                            ctStatement,
                            new LocalizedMessage(
                                "duplicate-code",
                                Map.of(
                                    "left", formatSourceRange(leftCode.getFirst(), leftCode.getLast()),
                                    "right", formatSourceRange(rightCode.getFirst(), rightCode.getLast())
                                )
                            ),
                            ProblemType.DUPLICATE_CODE
                        );

                        break;
                    }
                }
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                if (ctMethod.isImplicit() || !ctMethod.getPosition().isValidPosition() || ctMethod.getBody() == null) {
                    super.visitCtMethod(ctMethod);
                    return;
                }

                for (CtStatement ctStatement : SpoonUtil.getEffectiveStatements(ctMethod.getBody())) {
                    this.checkCtStatement(ctStatement);
                }

                super.visitCtMethod(ctMethod);
            }
        });
    }
}
