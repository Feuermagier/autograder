package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.spotbugs.SpotbugsCheck;

public class EqualsContractCheck extends SpotbugsCheck {
    private static final String DESCRIPTION = "Equals must return false for null arguments";

    public EqualsContractCheck() {
        super(DESCRIPTION, "NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT");
    }
}
