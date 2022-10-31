package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.AvoidReassigningParametersRule;

public class DontReassignParametersCheck extends PMDCheck {
    private static final String DESCRIPTION = "Don't reassign method/constructor parameters";

    public DontReassignParametersCheck() {
        super(DESCRIPTION, DESCRIPTION, new AvoidReassigningParametersRule(), ProblemType.REASSIGNED_PARAMETER);
    }
}
