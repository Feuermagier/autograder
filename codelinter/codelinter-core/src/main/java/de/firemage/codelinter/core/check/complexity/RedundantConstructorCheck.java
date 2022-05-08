package de.firemage.codelinter.core.check.complexity;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryConstructorRule;

public class RedundantConstructorCheck extends PMDCheck {
    private static final String DESCRIPTION = "The default constructor is implicit when there is no other constructor";

    public RedundantConstructorCheck() {
        super(DESCRIPTION, new UnnecessaryConstructorRule());
    }
}
