package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTextBlock;

public class WrongLineBreakCheck extends IntegratedCheck {

    public WrongLineBreakCheck() {
        super("Always use system-independent line breaks such as the value obtained from System.lineSeparator() or %n in format strings");
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtLiteral<?>>() {
            @Override
            public void process(CtLiteral<?> literal) {
                if (literal.getValue() instanceof String value && !(literal instanceof CtTextBlock)
                    && (value.contains("\n") || value.contains("\r") || value.contains("\\n") || value.contains("\\r"))) {
                    addLocalProblem(literal, "System-dependent line break (\\n) used", ProblemType.SYSTEM_SPECIFIC_LINE_BREAK);
                }
            }
        });
    }
}
