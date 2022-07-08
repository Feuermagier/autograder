package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import lombok.Getter;

import java.util.List;

public class MultiPositionProblem implements Problem {
    @Getter
    private final Check check;

    @Getter
    private final String explanation;

    @Getter
    private final List<CodePosition> positions;

    public MultiPositionProblem(Check check, List<CodePosition> positions, String explanation) {
        this.check = check;
        this.explanation = explanation;
        this.positions = positions;
    }

    @Override
    public String getDisplayLocation() {
        // TODO
        return "<location>";
    }
}
