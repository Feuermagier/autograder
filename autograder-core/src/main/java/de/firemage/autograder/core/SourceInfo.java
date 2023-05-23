package de.firemage.autograder.core;

import de.firemage.autograder.core.compiler.JavaVersion;
import spoon.compiler.SpoonResource;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public interface SourceInfo extends Serializable {
    Path getPath();

    List<JavaFileObject> compilationUnits() throws IOException;

    SourceInfo copyTo(Path target) throws IOException;

    SpoonResource getSpoonResource();

    default String getName() {
        return this.getPath().getFileName().toString();
    }

    void delete() throws IOException;

    JavaVersion getVersion();

    Charset getCharset();
}
