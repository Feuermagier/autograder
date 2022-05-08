package de.firemage.codelinter.core;

import lombok.Getter;
import java.nio.file.Path;

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
    public String getDisplayLocation(Path root) {
        // TODO
        return root.relativize(this.position.file()) + ":" + this.position.startLine();
    }

    @Override
    public String displayAsString(Path root) {
        return this.getDisplayLocation(root) + " " + getExplanation();
    }
}
