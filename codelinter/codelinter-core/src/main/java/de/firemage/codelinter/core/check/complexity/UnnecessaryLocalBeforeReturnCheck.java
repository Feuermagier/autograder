package de.firemage.codelinter.core.check.complexity;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryLocalBeforeReturnRule;

public class UnnecessaryLocalBeforeReturnCheck extends PMDCheck {
    private static final String DESCRIPTION = "Unnecessary declaration of a local variable that is immediately returned";

    public UnnecessaryLocalBeforeReturnCheck() {
        super(DESCRIPTION, "Directly return this value", new UnnecessaryLocalBeforeReturnRule());
    }
}
