package de.firemage.autograder.core.file;

import de.firemage.autograder.core.compiler.JavaVersion;
import spoon.compiler.SpoonResource;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * This interface represents the source code folder of a submission.
 */
public interface SourceInfo extends Serializable {
    /**
     * Returns the compilation units of the source.
     * <p>
     * All compilation units must be relative to the root of
     * the source returned by {@link #path()}.
     *
     * @return the compilation units of the source or an empty list if there are none
     * @throws IOException if the compilation units could not be read
     */
    List<CompilationUnit> compilationUnits() throws IOException;

    /**
     * Copies the source to the given target directory.
     *
     * @param target the target directory, which must be accessible
     * @return a new {@link SourceInfo} representing the copied source
     * @throws IOException if the copy failed
     */
    SourceInfo copyTo(Path target) throws IOException;

    SpoonResource getSpoonResource();

    /**
     * A name representing the source. This must not represent the real name of the source file or a path.
     *
     * @return the name of the source, which must not be unique
     */
    default String getName() {
        return this.path().getFileName().toString();
    }

    default CompilationUnit getCompilationUnit(SourcePath path) {
        Iterable<CompilationUnit> compilationUnits;
        try {
            compilationUnits = this.compilationUnits();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read compilation units", exception);
        }

        for (CompilationUnit compilationUnit : compilationUnits) {
            if (path.equals(compilationUnit.path())) {
                return compilationUnit;
            }
        }

        throw new IllegalStateException("Could not find compilation unit for path '%s'".formatted(path));
    }

    default CompilationUnit getCompilationUnit(Path path) {
        Path root = this.path();

        Path relative;
        try {
            relative = root.relativize(path);
        } catch (IllegalArgumentException exception) {
            // path should be relative
            relative = path;
        }

        return this.getCompilationUnit(SourcePath.of(relative));
    }

    default CompilationUnit getCompilationUnit(URI uri) {
        return this.getCompilationUnit(Path.of(uri.getPath().substring(1)));
    }

    /**
     * Returns a path to the source, which might not exist on the file-system.
     * <p>
     * For example, one could have the entire code in memory, so there would be no path on
     * the file-system.
     *
     * @return a path to the source
     */
    Path path();

    /**
     * Returns the java version required to compile the source.
     *
     * @return the java version required to compile the source
     */
    JavaVersion getVersion();
}
