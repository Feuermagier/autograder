package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.EXCEPTION_CAUGHT_IN_SURROUNDING_BLOCK})
public class ExceptionControlFlowCheck extends IntegratedCheck {

    public ExceptionControlFlowCheck() {
        super(new LocalizedMessage("exception-controlflow-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTry>() {
            @Override
            public void process(CtTry tryBlock) {
                List<CtTypeReference<?>> thrownExceptions = new ArrayList<>();
                tryBlock.getBody().accept(new CtScanner() {
                    @Override
                    public void visitCtThrow(CtThrow throwStatement) {
                        thrownExceptions.add(throwStatement.getThrownExpression().getType());
                        super.visitCtThrow(throwStatement);
                    }
                });

                for (CtCatch catchBlock : tryBlock.getCatchers()) {
                    var caughtException = catchBlock.getParameter().getType();
                    if (thrownExceptions.stream()
                        .anyMatch(e -> e.getQualifiedName().equals(caughtException.getQualifiedName()))
                        || thrownExceptions.stream().anyMatch(e -> e.isSubtypeOf(caughtException))) {
                        addLocalProblem(tryBlock,
                            new LocalizedMessage("exception-controlflow-exp-caught",
                                Map.of("exp", caughtException.getSimpleName())),
                            ProblemType.EXCEPTION_CAUGHT_IN_SURROUNDING_BLOCK);
                    }
                }
            }
        });
    }
}
