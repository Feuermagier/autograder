package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.spotbugs.SpotbugsCheck;

@ExecutableCheck(reportedProblems = {ProblemType.NON_COMPLIANT_EQUALS})
public class EqualsContractCheck extends SpotbugsCheck {
    public EqualsContractCheck() {
        super(
                new LocalizedMessage("equals-handle-null-argument-exp"), "NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT",
            ProblemType.NON_COMPLIANT_EQUALS);
    }
}
