package de.firemage.autograder.core.check.debug;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class PrintStackTraceCheck extends PMDCheck {
    private static final String QUERY = """
        //PrimaryExpression[
           ( PrimaryPrefix[Name[contains(@Image,'printStackTrace')]]
           | PrimarySuffix[@Image='printStackTrace']
           )/following-sibling::*[1][self::PrimarySuffix/Arguments[@Size=0]]
        ]
        """;

    public PrintStackTraceCheck() {
        super(new LocalizedMessage("print-stack-trace-exp"),
            createXPathRule("print stack trace", "print-stack-trace-desc", QUERY),
            ProblemType.EXCEPTION_PRINT_STACK_TRACE);
    }
}
