package de.firemage.autograder.core.framework;

import de.firemage.autograder.core.Problem;

public record ReportedProblem(Problem problem, String translatedMessage) {
}
