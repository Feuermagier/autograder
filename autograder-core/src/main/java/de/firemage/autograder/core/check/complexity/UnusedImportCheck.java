package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryImportRule;

@ExecutableCheck(reportedProblems = {ProblemType.UNUSED_IMPORT})
public class UnusedImportCheck extends PMDCheck {
    public UnusedImportCheck() {
        super(
                new LocalizedMessage("unused-import-exp"),
            new UnnecessaryImportRule(),
            ProblemType.UNUSED_IMPORT);
    }
}
