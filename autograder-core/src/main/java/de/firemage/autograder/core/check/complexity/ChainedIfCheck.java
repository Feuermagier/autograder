package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;

@ExecutableCheck(reportedProblems = {ProblemType.UNMERGED_ELSE_IF})
public class ChainedIfCheck extends PMDCheck {
    public ChainedIfCheck() {
        super(new LocalizedMessage("merge-if-desc"),
            createXPathRule("chained ifs", "merge-if-exp",
                "//IfStatement/Statement[2]/Block[count(*) = 1]/BlockStatement/Statement[1]/IfStatement"),
            ProblemType.UNMERGED_ELSE_IF);
    }
}
