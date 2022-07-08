package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import lombok.Getter;

public abstract class InCodeProblem implements Problem {

    @Getter
    private final Check check;

    @Getter
    private final CodePosition position;

    @Getter
    private final String explanation;

    public InCodeProblem(Check check, CodePosition position, String explanation) {
        this.check = check;
        this.position = position;
        this.explanation = explanation;
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
}
