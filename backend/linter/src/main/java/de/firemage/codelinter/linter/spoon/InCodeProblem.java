package de.firemage.codelinter.linter.spoon;

import lombok.Getter;
import spoon.reflect.declaration.CtElement;

public class InCodeProblem implements Problem {
    @Getter
    private final String displayPath;

    @Getter
    private final String filePath;

    @Getter
    private final int line;

    @Getter
    private final int column;

    @Getter
    private final String description;

    @Getter
    private final ProblemCategory category;

    @Getter
    private final String explanation;

    public InCodeProblem(String displayPath, String filePath, int line, int column, String description, ProblemCategory category, String explanation) {
        this.displayPath = displayPath;
        this.filePath = filePath;
        this.line = line;
        this.column = column;
        this.description = description;
        this.category = category;
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "InCodeProblem at " + filePath + ":" + line + ". " + description;
    }
}
