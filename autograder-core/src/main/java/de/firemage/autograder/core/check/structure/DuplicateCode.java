package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.UsesFinder;
import de.firemage.autograder.core.integrated.structure.StructuralElement;
import de.firemage.autograder.core.integrated.structure.StructuralEqualsVisitor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    private record CodeSegment(List<CtStatement> statements) implements Iterable<CtStatement> {
        public CodeSegment {
            statements = new ArrayList<>(statements);
        }

        public static CodeSegment of(CtStatement... statement) {
            return new CodeSegment(Arrays.asList(statement));
        }

        public void add(CtStatement ctStatement) {
            this.statements.add(ctStatement);
        }

        public CtStatement getFirst() {
            return this.statements.get(0);
        }

        public CtStatement getLast() {
            return this.statements.get(this.statements.size() - 1);
        }

        public List<CtStatement> statements() {
            return new ArrayList<>(this.statements);
        }

        @Override
        public Iterator<CtStatement> iterator() {
            return this.statements().iterator();
        }

        private Set<CtVariable<?>> declaredVariables() {
            Set<CtVariable<?>> declaredVariables = new LinkedHashSet<>();

            for (CtStatement ctStatement : this) {
                if (ctStatement instanceof CtVariable<?> ctVariable) {
                    declaredVariables.add(ctVariable);
                }
            }

            return declaredVariables;
        }

        public int countExposedVariables() {
            Set<CtVariable<?>> declaredVariables = this.declaredVariables();
            if (declaredVariables.isEmpty()) {
                return 0;
            }

            int count = 0;
            for (CtStatement ctStatement : SpoonUtil.getNextStatements(this.getLast())) {
                for (CtVariable<?> declaredVariable : declaredVariables) {
                    if (UsesFinder.variableUses(declaredVariable).nestedIn(ctStatement).hasAny()) {
                        count += 1;
                    }
                }
            }
            return count;
        }

        public int countDependencies(Predicate<? super CtVariable<?>> isDependency, Predicate<? super CtVariableAccess<?>> isDependencyAccess) {
            if (this.statements().isEmpty()) {
                return 0;
            }

            Set<CtVariable<?>> codeSegmentVariables = this.statements.stream()
                .flatMap(ctStatement -> ctStatement.getElements(new TypeFilter<CtVariable<?>>(CtVariable.class)).stream())
                .collect(Collectors.toCollection(() -> Collections.newSetFromMap(new IdentityHashMap<>())));

            return (int) this.statements.stream()
                .flatMap(ctStatement -> ctStatement.getElements(new TypeFilter<CtVariableAccess<?>>(CtVariableAccess.class)).stream())
                .filter(isDependencyAccess)
                .map(UsesFinder::getDeclaredVariable)
                .unordered()
                .distinct()
                .filter(ctVariable -> !codeSegmentVariables.contains(ctVariable) && isDependency.test(ctVariable))
                .count();
        }
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

                List<CtStatement> duplicates = occurrences.get(new StructuralElement<>(ctStatement));

                int initialSize = countStatements(ctStatement);
                for (CtStatement duplicate : duplicates) {
                    if (duplicate == ctStatement || reported.contains(duplicate) || reported.contains(ctStatement)) {
                        continue;
                    }

                    int duplicateStatementSize = initialSize;

                    CodeSegment leftCode = CodeSegment.of(ctStatement);
                    CodeSegment rightCode = CodeSegment.of(duplicate);

                    for (var entry : zip(SpoonUtil.getNextStatements(ctStatement), SpoonUtil.getNextStatements(duplicate))) {
                        if (!StructuralEqualsVisitor.equals(entry.getKey(), entry.getValue())) {
                            break;
                        }

                        leftCode.add(entry.getKey());
                        rightCode.add(entry.getValue());
                        duplicateStatementSize += countStatements(entry.getKey());
                    }

                    if (duplicateStatementSize < MINIMUM_DUPLICATE_STATEMENT_SIZE) {
                        continue;
                    }

                    // The duplicate code might access variables that are not declared in the code segment.
                    // The variables would have to be passed as parameters of a helper method.
                    //
                    // The problem is that when a variable is reassigned, it can not be passed as a parameter
                    // -> we would have to ignore the duplicate code segment
                    int numberOfReassignedVariables = leftCode.countDependencies(
                        ctVariable -> !(ctVariable instanceof CtField<?>) && !ctVariable.isStatic(),
                        ctVariableAccess -> ctVariableAccess instanceof CtVariableWrite<?> && ctVariableAccess.getParent() instanceof CtAssignment<?,?>
                    );

                    if (numberOfReassignedVariables > 1) {
                        continue;
                    }

                    // Another problem is that the duplicate code segment might declare variables that are used
                    // after the code segment.
                    //
                    // A method can at most return one value (ignoring more complicated solutions like returning a custom object)
                    int numberOfUsedVariables = Math.max(leftCode.countExposedVariables(), rightCode.countExposedVariables());

                    if (numberOfReassignedVariables + numberOfUsedVariables > 1) {
                        continue;
                    }

                    // prevent duplicate reporting of the same code segments
                    reported.addAll(leftCode.statements());
                    reported.addAll(rightCode.statements());

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
