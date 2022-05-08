package de.firemage.codelinter.core.check.complexity;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryReturnRule;

public class RedundantReturnCheck extends PMDCheck {
    public static final String DESCRIPTION = "A void return at the end of a method is implicit";

    public RedundantReturnCheck() {
        super(DESCRIPTION, new UnnecessaryReturnRule());
    }
}
