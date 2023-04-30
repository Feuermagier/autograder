package de.firemage.autograder.core.parallel;

import de.firemage.autograder.core.Problem;

import java.util.List;
import java.util.Objects;

public record AnalysisResult(List<Problem> problems, Exception thrownException) {
    public static AnalysisResult forSuccess(List<Problem> problems) {
        Objects.requireNonNull(problems);
        return new AnalysisResult(problems, null);
    }

    public static AnalysisResult forFailure(Exception thrownException) {
        Objects.requireNonNull(thrownException);
        return new AnalysisResult(null, thrownException);
    }

    public boolean failed() {
        return this.thrownException != null;
    }
}
