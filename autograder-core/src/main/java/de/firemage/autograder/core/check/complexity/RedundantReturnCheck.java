package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryReturnRule;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_VOID_RETURN})
public class RedundantReturnCheck extends PMDCheck {
    public RedundantReturnCheck() {
        super(new LocalizedMessage("redundant-return-desc"),
            new LocalizedMessage("redundant-return-exp"),
            new UnnecessaryReturnRule(), ProblemType.REDUNDANT_VOID_RETURN);
    }
}
