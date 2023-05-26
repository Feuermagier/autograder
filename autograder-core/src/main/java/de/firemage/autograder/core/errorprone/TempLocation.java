package de.firemage.autograder.core.errorprone;

import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public record TempLocation(File tempLocation) implements Serializable, Closeable {
    private static final Random RANDOM = new Random();
    private static final String TEMPORARY_DIR_FORMAT = "%s%d";

    public static TempLocation fromPath(Path path) {
        return new TempLocation(path.toFile());
    }

    public static TempLocation of(String first, String... other) {
        return TempLocation.fromPath(Path.of(first, other));
    }

    public static TempLocation random() {
        try {
            return TempLocation.fromPath(Files.createTempDirectory("random"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not create temporary directory", e);
        }
    }

    /**
     * Returns a list of potential candidates for temporary directories.
     *
     * @return a list of functions that can be called to get the temporary directory
     */
    // For the future in case this turns out to be a problem:
    // There exists a library called https://github.com/google/jimfs to store files in memory.
    // It seems to support all the targeted platforms (Windows, Linux and macOS)
    //
    // Might be worth a try.
    private List<IOFunction<String, Path>> temporaryDirectories() {
        return List.of(
            prefix -> makeDirectory(this.tempLocation.toPath(), prefix),
            prefix -> makeDirectory(Path.of(System.getProperty("java.io.tmpdir")), prefix),
            Files::createTempDirectory,
            prefix -> makeDirectory(Path.of(".", "tmp").toAbsolutePath(), prefix)
        );
    }

    private static Path makeDirectory(Path path, String prefix) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        if (!Files.exists(absolutePath)) {
            Files.createDirectories(absolutePath);
        }

        if (!Files.isDirectory(absolutePath)) {
            throw new IllegalArgumentException("the path '%s' is not a directory".formatted(absolutePath));
        }

        // just in case a conflict occurs, try until a non-existing directory is found
        Path directory;
        do {
            directory = absolutePath.resolve(TEMPORARY_DIR_FORMAT.formatted(prefix, RANDOM.nextLong()));
        } while (Files.exists(directory));

        // now try to create the temporary directory (might throw an exception)
        return Files.createDirectory(directory);
    }

    public TempLocation createTempDirectory(String prefix) throws IOException {
        if (prefix.contains(File.pathSeparator)) {
            throw new IllegalArgumentException("the prefix '%s' contains a path separator".formatted(prefix));
        }
        for (IOFunction<String, Path> tempDir : this.temporaryDirectories()) {
            try {
                return TempLocation.fromPath(tempDir.apply(prefix));
            } catch (IOException exception) {
                // this will fail if there is no read/write-access to the directory
            }
        }

        throw new IllegalStateException("could not find a temporary directory");
    }

    public Path createTempFile(String name) throws IOException {
        return Files.createFile(this.toPath().resolve(name));
    }

    /**
     * Returns the path of the temporary location.
     * @return the path of the temporary location
     */
    public Path toPath() {
        return this.tempLocation.toPath();
    }

    @FunctionalInterface
    private interface IOFunction<I, R> {
        R apply(I input) throws IOException;
    }

    @Override
    public void close() throws IOException {
        // delete the temporary directory, will not crash if it fails to delete it
        FileUtils.deleteQuietly(this.toPath().toFile());
    }
}
