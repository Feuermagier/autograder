package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryImportRule;

public class UnusedImportCheck extends PMDCheck {
    public UnusedImportCheck() {
        super(new LocalizedMessage("unused-import-desc"),
            new LocalizedMessage("unused-import-exp"),
            new UnnecessaryImportRule(),
            ProblemType.UNUSED_IMPORT);
    }
}
