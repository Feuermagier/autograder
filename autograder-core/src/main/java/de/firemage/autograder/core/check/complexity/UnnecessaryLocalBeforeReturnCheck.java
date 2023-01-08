package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryLocalBeforeReturnRule;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_LOCAL_BEFORE_RETURN})
public class UnnecessaryLocalBeforeReturnCheck extends PMDCheck {
    private static final String DESCRIPTION =
        "Unnecessary declaration of a local variable that is immediately returned";

    public UnnecessaryLocalBeforeReturnCheck() {
        super(new LocalizedMessage("redundant-local-return-desc"),
            new LocalizedMessage("redundant-local-return-exp"),
            new UnnecessaryLocalBeforeReturnRule(),
            ProblemType.REDUNDANT_LOCAL_BEFORE_RETURN);
    }
}
