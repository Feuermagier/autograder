package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.UnnecessaryModifierRule;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_MODIFIER})
public class RedundantModifierCheck extends PMDCheck {
    public RedundantModifierCheck() {
        super(new LocalizedMessage("redundant-modifier-desc"), new UnnecessaryModifierRule(),
            ProblemType.REDUNDANT_MODIFIER);
        super.getRules().get(0).setMessage("redundant-modifier-exp");
    }
}
