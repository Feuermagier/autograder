package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;

public abstract class InCodeProblem implements Problem {

    private final Check check;

    private final CodePosition position;

    private final LocalizedMessage explanation;

    private final ProblemType problemType;

    protected InCodeProblem(Check check, CodePosition position, LocalizedMessage explanation, ProblemType problemType) {
        this.check = check;
        this.position = position;
        this.explanation = explanation;
        this.problemType = problemType;
    }

    @Override
    public String getDisplayLocation() {
        // TODO
        if (this.position.startLine() == this.position.endLine()) {
            return this.position.file() + ":" + this.position.startLine();
        } else {
            return this.position.file() + ":" + this.position.startLine() + "-" + this.position.endLine();
        }
    }

    public Check getCheck() {
        return check;
    }

    public CodePosition getPosition() {
        return position;
    }

    public LocalizedMessage getExplanation() {
        return explanation;
    }

    public ProblemType getProblemType() {
        return problemType;
    }
}
