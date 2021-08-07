package de.firemage.codelinter.core;

import lombok.Getter;

public abstract class InCodeProblem implements Problem {

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

    @Getter
    private final ProblemPriority priority;

    public InCodeProblem(String filePath, int line, int column, String description, ProblemCategory category, String explanation, ProblemPriority priority) {
        this.filePath = filePath;
        this.line = line;
        this.column = column;
        this.description = description;
        this.category = category;
        this.explanation = explanation;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "InCodeProblem at " + getDisplayLocation() + ":" + line + ". " + description;
    }
}
