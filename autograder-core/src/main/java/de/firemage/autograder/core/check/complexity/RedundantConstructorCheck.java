package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryConstructorRule;

public class RedundantConstructorCheck extends PMDCheck {
    public RedundantConstructorCheck() {
        super(new LocalizedMessage("implicit-constructor-desc"), new LocalizedMessage("implicit-constructor-exp"),
            new UnnecessaryConstructorRule(), ProblemType.REDUNDANT_DEFAULT_CONSTRUCTOR);
    }
}
