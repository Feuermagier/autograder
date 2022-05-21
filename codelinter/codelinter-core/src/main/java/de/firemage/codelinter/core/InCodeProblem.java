package de.firemage.codelinter.core;

import de.firemage.codelinter.core.check.Check;
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
    public String getDisplayLocation() {
        // TODO
        return this.position.file() + ":" + this.position.startLine();
    }
}
