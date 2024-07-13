package de.firemage.autograder.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public interface AbstractTempLocation extends Closeable, Serializable {
    AbstractTempLocation createTempDirectory(String prefix) throws IOException;
    Path createTempFile(String name) throws IOException;
    Path toPath();
}
