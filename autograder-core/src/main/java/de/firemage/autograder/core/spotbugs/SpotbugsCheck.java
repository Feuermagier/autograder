package de.firemage.autograder.core.spotbugs;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.Check;

public abstract class SpotbugsCheck implements Check {
    private final LocalizedMessage description;

    private final String bug;

    private final ProblemType problemType;
    
    private final LocalizedMessage explanation;

    protected SpotbugsCheck(LocalizedMessage description, LocalizedMessage explanation, String bug, ProblemType problemType) {
        this.description = description;
        this.bug = bug;
        this.problemType = problemType;
        this.explanation = explanation;
    }

    @Override
    public LocalizedMessage getLinter() {
        return new LocalizedMessage("linter-spotbugs");
    }

    @Override
    public LocalizedMessage getDescription() {
        return description;
    }

    public String getBug() {
        return bug;
    }

    public ProblemType getProblemType() {
        return problemType;
    }

    public LocalizedMessage getExplanation() {
        return explanation;
    }
}
