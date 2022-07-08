package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import lombok.Getter;

public class GlobalProblem implements Problem {

    @Getter
    private final Check check;

    @Getter
    private final String explanation;

    public GlobalProblem(Check check, String explanation) {
        this.check = check;
        this.explanation = explanation;
    }

    @Override
    public String getDisplayLocation() {
        return "Global";
    }

    @Override
    public String toString() {
        return "GlobalProblem: " + getExplanation();
    }
}
