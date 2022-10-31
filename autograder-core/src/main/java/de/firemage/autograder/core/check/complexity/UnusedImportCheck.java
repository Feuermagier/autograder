package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryImportRule;

public class UnusedImportCheck extends PMDCheck {
    private static final String DESCRIPTION = "Unnecessary import";

    public UnusedImportCheck() {
        super(DESCRIPTION, new UnnecessaryImportRule(), ProblemType.UNUSED_IMPORT);
    }
}
