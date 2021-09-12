package de.firemage.codelinter.core;

import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public final class PathUtil {
    private PathUtil() {
    }

    public static String getSanitizedPath(File file, File root) {
        return getSanitizedPathUnchecked(file.toPath().toAbsolutePath().normalize().toString(), root);
    }

    public static String getSanitizedPath(URI uri, File root) {
        return getSanitizedPath(uri.getPath().substring(1), root);
    }

    public static String getSanitizedPath(String path, File root) {
        try {
            return getSanitizedPathUnchecked(Paths.get(path).toAbsolutePath().normalize().toString(), root);
        } catch (InvalidPathException e) {
            return getSanitizedPathUnchecked(path, root);
        }
    }

    private static String getSanitizedPathUnchecked(String path, File root) {
        String sanitizedRoot = root.toPath().toAbsolutePath().normalize().toString().replace("\\", "/") + "/";
        String sanitizedPath = path.replace("\\", "/");
        int pathBeginIndex = sanitizedPath.indexOf(sanitizedRoot) + sanitizedRoot.length();
        if (pathBeginIndex < sanitizedPath.length()) {
            return sanitizedPath.substring(pathBeginIndex);
        } else {
            return sanitizedPath;
        }
    }
}
