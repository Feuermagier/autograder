package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedFormalParameterRule;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedLocalVariableRule;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedPrivateFieldRule;
import net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedPrivateMethodRule;

import java.util.List;

@ExecutableCheck(reportedProblems = {ProblemType.UNUSED_CODE_ELEMENT})
public class UnusedCodeElementCheck extends PMDCheck {
    public UnusedCodeElementCheck() {
        super(new LocalizedMessage("unused-element-desc"), new LocalizedMessage("unused-element-exp"), List.of(
                new UnusedLocalVariableRule(),
                new UnusedFormalParameterRule(),
                new UnusedPrivateFieldRule(),
                new UnusedPrivateMethodRule()),
            ProblemType.UNUSED_CODE_ELEMENT);
    }
}
