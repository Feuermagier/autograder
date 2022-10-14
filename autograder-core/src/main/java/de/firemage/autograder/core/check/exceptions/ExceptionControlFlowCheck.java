package de.firemage.autograder.core.check.exceptions;

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

public class ExceptionControlFlowCheck extends IntegratedCheck {

    public ExceptionControlFlowCheck() {
        super(
            "Exceptions should not be used for control flow inside of a method (i.e. throwing an exception and catching it in a directly surrounding catch block)");
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
                            "Exception '" + caughtException.getSimpleName() + "' used for control flow");
                    }
                }
            }
        });
    }
}
