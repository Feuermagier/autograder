package de.firemage.autograder.core.framework;

import de.firemage.autograder.core.ProblemType;

public record ExpectedProblem(String file, int line, ProblemType problemType, String comment) {
    public String format() {
        return "%s@%s:%d".formatted(
                this.problemType != null ? this.problemType : "?",
                this.file,
                this.line
        );
    }

    public String toString() {
        return this.format();
    }
}
