package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryReturnRule;

public class RedundantReturnCheck extends PMDCheck {
    public static final String DESCRIPTION = "A void return at the end of a method is implicit";

    public RedundantReturnCheck() {
        super(DESCRIPTION, "Unnecessary return", new UnnecessaryReturnRule());
    }
}
