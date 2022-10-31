package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.spotbugs.SpotbugsCheck;

public class EqualsContractCheck extends SpotbugsCheck {
    public EqualsContractCheck() {
        super(new LocalizedMessage("equals-handle-null-argument-desc"),
            new LocalizedMessage("equals-handle-null-argument-exp"), "NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT",
            ProblemType.NON_COMPLIANT_EQUALS);
    }
}
