package de.firemage.autograder.core.file;

import de.firemage.autograder.api.PathLike;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * This represents a path to a source folder or file.
 * <p>
 * Why does this class exist and why not just use {@link Path}?
 * <p>
 * {@link SourceInfo} does not always represent files that have a real path on the file-system.
 * It is possible to load source code from memory, so those files would not have a path.
 * Additionally, it would be possible to have paths with {@code ".."} that have to be normalized first, which
 * makes comparisons harder.
 */
public final class SourcePath implements Comparable<SourcePath>, Serializable, PathLike {
    private static final String SEPARATOR = "/";

    private final List<String> segments;

    private SourcePath(List<String> segments) {
        this.segments = segments;
    }

    public static SourcePath of(Path path) {
        return new SourcePath(new ArrayList<>(StreamSupport.stream(path.spliterator(), false)
            .map(segment -> segment.getFileName().toString())
            .toList()
        ));
    }

    public static SourcePath of(List<String> path) {
        return new SourcePath(path);
    }

    public static SourcePath of(String first, String... more) {
        List<String> result = new ArrayList<>(List.of(first));
        result.addAll(Arrays.asList(more));

        return SourcePath.of(result);
    }

    /**
     * The name of the file or folder represented by this path.
     * <p>
     * For a file, this includes the file extension.
     * @return the name of the file or folder. Will never be null or an empty string
     */
    public String getName() {
        return this.segments.get(this.segments.size() - 1);
    }

    /**
     * Makes this path relative to the given root path. This path must start with the root path.
     * <p>
     * For example {@code SourcePath.of("a", "b", "c").makeRelativeTo(SourcePath.of("a", "b"))} would return
     * {@code SourcePath.of("c")}.
     *
     * @param root the root path
     * @return the relative path or an empty if this path does not start with the root path
     */
    Optional<SourcePath> makeRelativeTo(SourcePath root) {
        if (root.segments.size() >= this.segments.size()) {
            return Optional.empty();
        }

        for (int i = 0; i < root.segments.size(); i++) {
            if (!this.segments.get(i).equals(root.segments.get(i))) {
                return Optional.empty();
            }
        }

        List<String> result = new ArrayList<>(this.segments.subList(root.segments.size(), this.segments.size()));

        return Optional.of(SourcePath.of(result));
    }

    SourcePath resolve(SourcePath other) {
        List<String> result = new ArrayList<>(this.segments);
        result.addAll(other.segments);

        return SourcePath.of(result);
    }

    public Path toPath() {
        return Path.of(this.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SourcePath otherPath)) {
            return false;
        }

        return this.segments.equals(otherPath.segments);
    }

    @Override
    public int hashCode() {
        return this.segments.hashCode();
    }

    @Override
    public String toString() {
        return String.join(SEPARATOR, this.segments);
    }

    @Override
    public int compareTo(SourcePath other) {
        if (this.equals(other)) {
            return 0;
        }

        return this.toString().compareTo(other.toString());
    }
}
