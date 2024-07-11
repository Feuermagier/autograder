package de.firemage.autograder.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public interface TempLocation extends Closeable, Serializable {
    TempLocation createTempDirectory(String prefix) throws IOException;
    Path createTempFile(String name) throws IOException;
    Path toPath();
}
