package de.firemage.autograder.core.framework;

import de.firemage.autograder.core.ProblemImpl;

public record ReportedProblem(ProblemImpl problem, String translatedMessage) {
}
