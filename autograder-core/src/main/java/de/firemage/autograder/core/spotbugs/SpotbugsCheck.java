package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.Check;

public abstract class SpotbugsCheck implements Check {
    private final String description;

    private final String bug;

    private final ProblemType problemType;

    protected SpotbugsCheck(String description, String bug, ProblemType problemType) {
        this.description = description;
        this.bug = bug;
        this.problemType = problemType;
    }

    @Override
    public String getLinter() {
        return "SpotBugs";
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getBug() {
        return bug;
    }

    public ProblemType getProblemType() {
        return problemType;
    }
}
