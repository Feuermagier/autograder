package de.firemage.codelinter.core;

import lombok.Getter;
import java.nio.file.Path;
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
    public String getDisplayLocation(Path root) {
        // TODO
        return "<location>";
    }

    @Override
    public String displayAsString(Path root) {
        return this.getDisplayLocation(root) + " " + this.getExplanation();
    }
}
