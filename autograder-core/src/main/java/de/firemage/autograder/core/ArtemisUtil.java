package de.firemage.autograder.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ArtemisUtil {
    private ArtemisUtil() {

    }

    public static Path resolveCodePathEclipseGradingTool(Path submissionRoot) throws IOException {
        try (Stream<Path> files = Files.list(submissionRoot)) {
            return files
                    .filter(child -> !child.endsWith(".metadata"))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("No student code found"))
                    .resolve("assignment")
                    .resolve("src");
        }
    }
}
