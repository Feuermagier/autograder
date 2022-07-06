package de.firemage.codelinter.core.check.debug;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class PrintStackTraceCheck extends PMDCheck {
    private static final String DESCRIPTION = "Don't print stack traces in your final solution";
    private static final String QUERY = """
            //PrimaryExpression[
               ( PrimaryPrefix[Name[contains(@Image,'printStackTrace')]]
               | PrimarySuffix[@Image='printStackTrace']
               )/following-sibling::*[1][self::PrimarySuffix/Arguments[@Size=0]]
            ]
            """;

    public PrintStackTraceCheck() {
        super(DESCRIPTION, createXPathRule("print stack trace", "Don't print stack traces in your final solution", QUERY));
    }
}
