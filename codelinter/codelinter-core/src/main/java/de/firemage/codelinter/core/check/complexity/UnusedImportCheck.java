package de.firemage.codelinter.core.check.complexity;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryImportRule;

public class UnusedImportCheck extends PMDCheck {
    private static final String DESCRIPTION = "Unnecessary import";

    public UnusedImportCheck() {
        super(DESCRIPTION, new UnnecessaryImportRule());
    }
}
