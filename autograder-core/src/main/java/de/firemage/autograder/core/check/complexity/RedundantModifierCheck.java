package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryModifierRule;

public class RedundantModifierCheck extends PMDCheck {
    private static final String DESCRIPTION = "Interfaces are public abstract by default";

    public RedundantModifierCheck() {
        super(DESCRIPTION, new UnnecessaryModifierRule(), ProblemType.REDUNDANT_MODIFIER);
        super.getRules().get(0).setMessage("Unnecessary modifier{0} on {1} ''{2}''{3}");
    }
}
