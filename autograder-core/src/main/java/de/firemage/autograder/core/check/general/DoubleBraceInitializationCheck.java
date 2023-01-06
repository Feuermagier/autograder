package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;

@ExecutableCheck(reportedProblems = {ProblemType.DOUBLE_BRACE_INITIALIZATION})
public class DoubleBraceInitializationCheck extends PMDCheck {
    public DoubleBraceInitializationCheck() {
        super(new LocalizedMessage("double-brace-desc"),
            createXPathRule("double brace initialization", "double-brace-exp",
                "//AllocationExpression/ClassOrInterfaceBody[count(*)=1]/*/Initializer[@Static=false()]"),
            ProblemType.DOUBLE_BRACE_INITIALIZATION);
    }
}
