package de.firemage.autograder.api;

import java.nio.file.Path;

public interface AbstractCodePosition {
    Path path();
    int startLine();

    int endLine();

    int startColumn();

    int endColumn();
    String readSourceFile();
}
