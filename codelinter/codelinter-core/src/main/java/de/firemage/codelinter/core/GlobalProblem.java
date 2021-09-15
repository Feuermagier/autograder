package de.firemage.codelinter.core;

import lombok.Getter;

public class GlobalProblem implements Problem {

    @Getter
    private final String description;

    @Getter
    private final ProblemCategory category;

    @Getter
    private final String explanation;

    @Getter
    private final ProblemPriority priority;

    public GlobalProblem(String description, ProblemCategory category, String explanation, ProblemPriority priority) {
        this.description = description;
        this.category = category;
        this.explanation = explanation;
        this.priority = priority;
    }

    @Override
    public String getDisplayLocation() {
        return "<GLOBAL>";
    }
}
