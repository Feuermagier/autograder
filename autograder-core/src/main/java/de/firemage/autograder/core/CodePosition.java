package de.firemage.autograder.core;

import java.nio.file.Path;

public record CodePosition(Path file, int startLine, int endLine, int startColumn, int endColumn) {
}
