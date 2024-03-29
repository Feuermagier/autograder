package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;

/**
 * Contains the default implementation of most {@link Problem} methods.
 */
public abstract class ProblemImpl implements Problem {

    private final Check check;

    private final CodePosition position;

    private final Translatable explanation;

    private final ProblemType problemType;

    protected ProblemImpl(Check check, CodePosition position, Translatable explanation, ProblemType problemType) {
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

    public Translatable getExplanation() {
        return explanation;
    }

    public ProblemType getProblemType() {
        return problemType;
    }
}
