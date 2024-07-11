package de.firemage.autograder.core;

import de.firemage.autograder.api.Problem;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.check.Check;

/**
 * Contains the default implementation of most {@link Problem} methods.
 */
public abstract class ProblemImpl implements Problem {

    private final Check check;

    private final CodePositionImpl position;

    private final Translatable explanation;

    private final ProblemType problemType;

    protected ProblemImpl(Check check, CodePositionImpl position, Translatable explanation, ProblemType problemType) {
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

    @Override
    public CodePositionImpl getPosition() {
        return position;
    }

    @Override
    public Translatable getExplanation() {
        return explanation;
    }

    @Override
    public ProblemType getProblemType() {
        return problemType;
    }

    @Override
    public String getCheckName() {
        return this.check.getClass().getSimpleName();
    }

    @Override
    public Translatable getLinterName() {
        return this.check.getLinter();
    }
}
