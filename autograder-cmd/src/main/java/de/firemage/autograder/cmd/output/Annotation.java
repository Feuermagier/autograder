package de.firemage.autograder.cmd.output;

import de.firemage.autograder.core.ProblemType;

public record Annotation(ProblemType type, String message, String file, int startLine, int endLine) {
}
