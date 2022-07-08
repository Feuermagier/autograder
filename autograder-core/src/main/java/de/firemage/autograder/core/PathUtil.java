package de.firemage.autograder.core;

import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtil {
    private PathUtil() {
    }

    public static String getSanitizedPath(File file, Path root) {
        return getSanitizedPathUnchecked(file.toPath().toAbsolutePath().normalize().toString(), root);
    }

    public static String getSanitizedPath(URI uri, Path root) {
        return getSanitizedPath(uri.getPath().substring(1), root);
    }

    public static String getSanitizedPath(String path, Path root) {
        try {
            return getSanitizedPathUnchecked(Paths.get(path).toAbsolutePath().normalize().toString(), root);
        } catch (InvalidPathException e) {
            return getSanitizedPathUnchecked(path, root);
        }
    }

    private static String getSanitizedPathUnchecked(String path, Path root) {
        String sanitizedRoot = root.toAbsolutePath().normalize().toString().replace("\\", "/") + "/";
        String sanitizedPath = path.replace("\\", "/");
        int pathBeginIndex = sanitizedPath.indexOf(sanitizedRoot) + sanitizedRoot.length();
        if (pathBeginIndex < sanitizedPath.length()) {
            return sanitizedPath.substring(pathBeginIndex);
        } else {
            return sanitizedPath;
        }
    }
}
