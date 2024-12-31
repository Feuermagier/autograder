package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.CoreUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@ExecutableCheck(reportedProblems = { ProblemType.TRY_BLOCK_SIZE })
public class TryBlockSize extends IntegratedCheck {
    private static boolean noneThrow(CtStatement ctStatement, Predicate<? super CtTypeReference<?>> isMatch) {
        List<CtTypeReference<?>> thrownExceptions = new ArrayList<>();
        ctStatement.accept(new CtScanner() {
            @Override
            public void visitCtThrow(CtThrow ctThrow) {
                thrownExceptions.add(ctThrow.getThrownExpression().getType());
                super.visitCtThrow(ctThrow);
            }

            private <T> void recordExecutableReference(CtExecutableReference<?> ctExecutableReference) {
                var executable = MethodUtil.getExecutableDeclaration(ctExecutableReference);
                if (executable != null) {
                    thrownExceptions.addAll(executable.getThrownTypes());
                }
            }

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                this.recordExecutableReference(invocation.getExecutable());
                super.visitCtInvocation(invocation);
            }

            @Override
            public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
                this.recordExecutableReference(ctConstructorCall.getExecutable());
                super.visitCtConstructorCall(ctConstructorCall);
            }


            @Override
            public <T> void visitCtNewClass(CtNewClass<T> ctNewClass) {
                this.recordExecutableReference(ctNewClass.getExecutable());
                super.visitCtNewClass(ctNewClass);
            }

            @Override
            public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(CtExecutableReferenceExpression<T, E> expression) {
                this.recordExecutableReference(expression.getExecutable());
                super.visitCtExecutableReferenceExpression(expression);
            }
        });

        return thrownExceptions.stream().noneMatch(isMatch);
    }

    private static String formatSourceRange(List<? extends CtElement> ctElements) {
        if (ctElements.isEmpty()) {
            return null;
        }

        SourcePosition position = ctElements.get(0).getPosition();
        String result = "L%d".formatted(position.getLine());

        if (position.getLine() == position.getEndLine() && ctElements.size() == 1) {
            return result;
        }

        int endLine = position.getEndLine();
        if (ctElements.size() > 1) {
            endLine = ctElements.get(ctElements.size() - 1).getPosition().getEndLine();
        }

        return result + "-%d".formatted(endLine);
    }

    @Override
    public void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTry>() {
            @Override
            public void process(CtTry ctTry) {
                if (ctTry.isImplicit() || !ctTry.getPosition().isValidPosition()) {
                    return;
                }

                List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctTry.getBody());
                if (statements.isEmpty()) {
                    return;
                }

                // these are all exceptions that are caught by the try-catch block
                Set<CtType<?>> caughtExceptions = ctTry.getCatchers()
                    .stream()
                    .map(CtCatch::getParameter)
                    .map(CtCatchVariable::getMultiTypes)
                    .flatMap(List::stream)
                    // filter out RuntimeExceptions, because they are hard to track via code analysis
                    .filter(type -> !TypeUtil.isSubtypeOf(type, java.lang.RuntimeException.class))
                    .map(CtTypeReference::getTypeDeclaration)
                    .filter(Objects::nonNull)
                    .collect(CoreUtil.toIdentitySet());

                // in case only RuntimeExceptions are caught, ignore the block
                if (caughtExceptions.isEmpty()) {
                    return;
                }

                // The noneThrow method will extract thrown types from the given statement and call this predicate with them.
                //
                // The predicate then checks if any of the thrown types are caught by the try-catch block.
                Predicate<? super CtTypeReference<?>> isMatch = ctTypeReference -> {
                    var type = ctTypeReference.getTypeDeclaration();

                    // this can happen, but I don't remember when this happens
                    if (type == null) {
                        return false;
                    }

                    // here it checks via the subtype relation, because subtypes are instances of their parent type.
                    return caughtExceptions.stream().anyMatch(caughtException -> UsesFinder.isSubtypeOf(type, caughtException));
                };

                // TODO: what about code like this?
                //
                // try {
                //    var variable = methodThatThrows();
                //
                //    // code that does not throw, but uses the variable
                //    System.out.println(variable);
                // } catch (InvalidArgumentException e) {
                //    // handle exception
                // }
                //
                // Should that code be linted?
                // TODO: if it should, document a possible solution for this in the wiki

                // go through each statement and check which do not throw exceptions that are later caught (these are irrelevant)
                List<CtStatement> irrelevantLeadingStatements = new ArrayList<>();
                CtStatement lastCheckedStatement = null;
                for (CtStatement statement : statements) {
                    lastCheckedStatement = statement;
                    if (!noneThrow(statement, isMatch)) {
                        break;
                    }

                    irrelevantLeadingStatements.add(statement);
                }

                List<CtStatement> irrelevantTrailingStatements = new ArrayList<>();
                for (int i = statements.size() - 1; i >= 0; i--) {
                    CtStatement statement = statements.get(i);
                    if (statement == lastCheckedStatement || !noneThrow(statement, isMatch)) {
                        break;
                    }

                    irrelevantTrailingStatements.add(statement);
                }

                Collections.reverse(irrelevantTrailingStatements);

                if (!irrelevantLeadingStatements.isEmpty() || !irrelevantTrailingStatements.isEmpty()) {
                    String start = formatSourceRange(irrelevantLeadingStatements);
                    String end = formatSourceRange(irrelevantTrailingStatements);

                    String result = start;
                    if (start == null) {
                        result = end;
                    } else if (end != null) {
                        result = "%s, %s".formatted(start, end);
                    }

                    addLocalProblem(
                        ctTry,
                        new LocalizedMessage(
                            "try-block-size",
                            Map.of(
                                "lines", Objects.requireNonNull(result)
                            )
                        ),
                        ProblemType.TRY_BLOCK_SIZE
                    );
                }
            }
        });
    }
}
