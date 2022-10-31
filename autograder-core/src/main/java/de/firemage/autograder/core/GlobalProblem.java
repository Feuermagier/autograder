package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import lombok.Getter;

public class GlobalProblem implements Problem {

    private final Check check;

    private final LocalizedMessage explanation;
    
    private final ProblemType problemType;

    public GlobalProblem(Check check, LocalizedMessage explanation, ProblemType problemType) {
        this.check = check;
        this.explanation = explanation;
        this.problemType = problemType;
    }

    @Override
    public String getDisplayLocation() {
        return "Global";
    }

    @Override
    public String toString() {
        return "GlobalProblem: " + getExplanation();
    }

    @Override
    public Check getCheck() {
        return check;
    }

    @Override
    public LocalizedMessage getExplanation() {
        return explanation;
    }

    @Override
    public ProblemType getProblemType() {
        return problemType;
    }
}
