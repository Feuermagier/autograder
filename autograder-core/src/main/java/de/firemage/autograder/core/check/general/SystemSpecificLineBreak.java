package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTextBlock;

import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.SYSTEM_SPECIFIC_LINE_BREAK })
public class SystemSpecificLineBreak extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtLiteral<?>>() {
            @Override
            public void process(CtLiteral<?> literal) {
                if (literal.isImplicit() || !literal.getPosition().isValidPosition()) {
                    return;
                }

                if (literal.getValue() instanceof String value && !(literal instanceof CtTextBlock)
                    && (value.contains("\n")
                    || value.contains("\r")
                    || value.contains("\\n")
                    || value.contains("\\r"))) {
                    addLocalProblem(
                        literal,
                        new LocalizedMessage("system-specific-linebreak"),
                        ProblemType.SYSTEM_SPECIFIC_LINE_BREAK
                    );
                    return;
                }

                if (literal.getValue() instanceof Character value && (value == '\n' || value == '\r')) {
                    addLocalProblem(
                        literal,
                        new LocalizedMessage("system-specific-linebreak"),
                        ProblemType.SYSTEM_SPECIFIC_LINE_BREAK
                    );
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(3);
    }
}
