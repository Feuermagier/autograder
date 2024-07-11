package de.firemage.autograder.api;

import java.nio.file.Path;

public interface CodePosition {
    Path path();
    int startLine();

    int endLine();

    int startColumn();

    int endColumn();
    String readCompilationUnit();
}
