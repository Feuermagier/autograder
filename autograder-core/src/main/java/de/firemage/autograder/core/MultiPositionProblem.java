package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;

import java.util.List;

public class MultiPositionProblem implements Problem {
    private final Check check;

    private final String explanation;

    private final List<CodePosition> positions;
    
    private final ProblemType problemType;

    public MultiPositionProblem(Check check, List<CodePosition> positions, String explanation,
                                ProblemType problemType) {
        this.check = check;
        this.explanation = explanation;
        this.positions = positions;
        this.problemType = problemType;
    }

    @Override
    public String getDisplayLocation() {
        // TODO
        return "<location>";
    }

    @Override
    public Check getCheck() {
        return check;
    }

    @Override
    public String getExplanation() {
        return explanation;
    }

    public List<CodePosition> getPositions() {
        return positions;
    }

    @Override
    public ProblemType getProblemType() {
        return problemType;
    }
}
