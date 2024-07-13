package de.firemage.autograder.cmd.output;

public record Annotation(String type, String message, String file, int startLine, int endLine) {
}
