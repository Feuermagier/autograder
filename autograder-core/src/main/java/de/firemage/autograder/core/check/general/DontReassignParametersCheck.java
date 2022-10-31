package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.AvoidReassigningParametersRule;

public class DontReassignParametersCheck extends PMDCheck {
    public DontReassignParametersCheck() {
        super(new LocalizedMessage("param-reassign-desc"), new LocalizedMessage("param-reassign-exp"),
            new AvoidReassigningParametersRule(), ProblemType.REASSIGNED_PARAMETER);
    }
}
