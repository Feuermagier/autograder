package de.firemage.autograder.core.check.exceptions;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;

@ExecutableCheck(reportedProblems = { ProblemType.NUMBER_FORMAT_EXCEPTION_IGNORED })
public class NumberFormatExceptionIgnored extends IntegratedCheck {
    @SuppressWarnings("unchecked")
    private static boolean isNFECaught(CtInvocation<?> ctInvocation) {
        return ctInvocation.getParent(new CompositeFilter<>(
            FilteringOperator.INTERSECTION,
            new TypeFilter<>(CtTry.class),
            ctTry -> ctTry.getCatchers().stream().anyMatch((CtCatch ctCatch) -> TypeUtil.isTypeEqualTo(ctCatch.getParameter().getType(), NumberFormatException.class))
        )) != null;
    }
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        boolean hasCaughtAnyException = staticAnalysis.getModel().filterChildren(CtCatch.class::isInstance).first() != null;
        // if exception handling is not present, we don't need to check for ignored exceptions
        if (!hasCaughtAnyException) {
            return;
        }

        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                if (ctInvocation.isImplicit() || !ctInvocation.getPosition().isValidPosition()) {
                    return;
                }

                if (MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), int.class, "parseInt", String.class) && !isNFECaught(ctInvocation)) {
                    addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage("number-format-exception-ignored"),
                        ProblemType.NUMBER_FORMAT_EXCEPTION_IGNORED
                    );
                }
            }
        });
    }
}
