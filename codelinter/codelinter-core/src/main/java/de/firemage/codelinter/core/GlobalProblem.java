package de.firemage.codelinter.core;

import lombok.Getter;
import java.nio.file.Path;

public class GlobalProblem implements Problem {

    @Getter
    private final Check check;

    @Getter
    private final String explanation;

    public GlobalProblem(Check check, String explanation) {
        this.check = check;
        this.explanation = explanation;
    }

    @Override
    public String getDisplayLocation(Path root) {
        return "<GLOBAL>";
    }

    @Override
    public String displayAsString(Path root) {
        return this.getExplanation();
    }

    @Override
    public String toString() {
        return "GlobalProblem: " + getExplanation();
    }
}
