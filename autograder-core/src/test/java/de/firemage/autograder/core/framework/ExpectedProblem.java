package de.firemage.autograder.core.framework;

import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.file.SourcePath;

public record ExpectedProblem(SourcePath file, int line, ProblemType problemType, String comment) {
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
