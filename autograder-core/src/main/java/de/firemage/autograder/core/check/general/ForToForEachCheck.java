package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.ForLoopCanBeForeachRule;

@ExecutableCheck(reportedProblems = {ProblemType.FOR_CAN_BE_FOREACH})
public class ForToForEachCheck extends PMDCheck {
    public ForToForEachCheck() {
        super(new LocalizedMessage("for-foreach-exp"),
            new ForLoopCanBeForeachRule(), ProblemType.FOR_CAN_BE_FOREACH);
    }
}
