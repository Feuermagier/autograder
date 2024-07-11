package de.firemage.autograder.core.parallel;

import de.firemage.autograder.api.Problem;
import de.firemage.autograder.core.ProblemImpl;

import java.util.List;
import java.util.Objects;

public record AnalysisResult(List<ProblemImpl> problems, Exception thrownException) {
    public static AnalysisResult forSuccess(List<ProblemImpl> problems) {
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
