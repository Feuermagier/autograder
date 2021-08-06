package de.firemage.codelinter.core;

import java.io.File;
import java.net.URI;

public final class PathUtil {
    private PathUtil() {
    }

    public static String getSanitizedPath(File file, File root) {
        return getSanitizedPath(file.getAbsolutePath(), root);
    }

    public static String getSanitizedPath(URI uri, File root) {
        return getSanitizedPath(uri.getPath().substring(1), root);
    }

    public static String getSanitizedPath(String path, File root) {
        String sanitizedRoot = root.getAbsolutePath().replace("\\", "/") + "/";
        String sanitizedPath = path.replace("\\", "/");
        return sanitizedPath.substring(sanitizedPath.indexOf(sanitizedRoot) + sanitizedRoot.length());
    }
}
