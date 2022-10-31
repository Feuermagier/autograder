package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.spotbugs.SpotbugsCheck;

public class EqualsContractCheck extends SpotbugsCheck {
    private static final String DESCRIPTION = "Equals must return false for null arguments";

    public EqualsContractCheck() {
        super(DESCRIPTION, "NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT", ProblemType.NON_COMPLIANT_EQUALS);
    }
}
