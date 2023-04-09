package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.reference.CtExecutableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.USE_STRING_FORMATTED })
public class UseStringFormatted extends IntegratedCheck {
    public UseStringFormatted() {
        super(new LocalizedMessage("use-string-formatted"));
    }

    private void checkCtInvocation(CtInvocation<?> ctInvocation) {
        boolean hasInvokedStringFormat = ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
               // ensure the method is called on java.lang.String
               && ctInvocation.getFactory().Type().createReference(java.lang.String.class)
                              .equals(ctTypeAccess.getAccessedType())
               && ctInvocation.getExecutable().getSimpleName().equals("format");

        if (!hasInvokedStringFormat) {
            return;
        }

        List<CtExpression<?>> args = new ArrayList<>(ctInvocation.getArguments());
        if (args.size() < 2) {
            return;
        }

        CtExpression<?> format = args.remove(0);
        String output = "%s.formatted(%s)".formatted(
            format,
            args.stream().map(CtExpression::toString).reduce((a, b) -> a + ", " + b).orElse("")
        );

        addLocalProblem(
            ctInvocation,
            new LocalizedMessage("use-string-formatted", Map.of("formatted", output)),
            ProblemType.USE_STRING_FORMATTED
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                checkCtInvocation(ctInvocation);
            }
        });
    }
}
