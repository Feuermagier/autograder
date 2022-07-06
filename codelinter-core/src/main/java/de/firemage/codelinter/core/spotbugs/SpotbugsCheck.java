package de.firemage.codelinter.core.spotbugs;

import de.firemage.codelinter.core.check.Check;
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
