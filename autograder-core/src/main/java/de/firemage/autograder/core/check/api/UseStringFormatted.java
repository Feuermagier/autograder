package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.USE_STRING_FORMATTED})
public class UseStringFormatted extends IntegratedCheck {
    private void checkCtInvocation(CtInvocation<?> ctInvocation) {
        boolean hasInvokedStringFormat = ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            // ensure the method is called on java.lang.String
            && SpoonUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), java.lang.String.class)
            && ctInvocation.getExecutable().getSimpleName().equals("format")
            && !ctInvocation.getArguments().isEmpty()
            // ensure the first argument is a string (this ignores String.format(Locale, String, Object...))
            && SpoonUtil.isTypeEqualTo(ctInvocation.getArguments().get(0).getType(), java.lang.String.class);

        if (!hasInvokedStringFormat) {
            return;
        }

        List<CtExpression<?>> args = new ArrayList<>(ctInvocation.getArguments());
        if (args.size() < 2) {
            return;
        }

        CtExpression<?> format = args.remove(0);
        // skip if the format string is not a string literal (e.g. a complex concatenation)
        if (SpoonUtil.tryGetStringLiteral(SpoonUtil.resolveConstant(format)).isEmpty()) {
            return;
        }

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
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                checkCtInvocation(ctInvocation);
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(1);
    }
}
