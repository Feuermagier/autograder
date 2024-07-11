package de.firemage.autograder.extra.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.extra.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryReturnRule;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_VOID_RETURN})
public class RedundantReturnCheck extends PMDCheck {
    public RedundantReturnCheck() {
        super(
                new LocalizedMessage("redundant-return-exp"),
            new UnnecessaryReturnRule(), ProblemType.REDUNDANT_VOID_RETURN);
    }
}
