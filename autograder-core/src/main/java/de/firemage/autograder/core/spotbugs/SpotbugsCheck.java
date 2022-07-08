package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.check.Check;
import lombok.Getter;

public abstract class SpotbugsCheck implements Check {
    @Getter
    private final String description;

    @Getter
    private final String bug;

    protected SpotbugsCheck(String description, String bug) {
        this.description = description;
        this.bug = bug;
    }

    @Override
    public String getLinter() {
        return "SpotBugs";
    }
}
