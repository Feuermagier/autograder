package de.firemage.autograder.core.framework;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.SourceInfo;
import de.firemage.autograder.core.StringSourceInfo;
import de.firemage.autograder.core.compiler.JavaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TestInput {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestInput.class);

    private static final Pattern META_COMMENT_PATTER = Pattern.compile("/\\*@(?<tag>[^;^@]*);?(?<comment>[^@]*);?(?<problemType>[^;^@]*)@\\*/");
    private final Path path;
    private final TestConfig config;
    private final SourceInfo sourceInfo;
    private final List<ExpectedProblem> expectedProblems;

    public TestInput(Path path) {
        this.path = path;
        this.config = TestConfig.fromPath(path);
        var sources = readSources(path);

        // Parse annotations before creating the source info because parseAnnotations rewrites the sources
        this.expectedProblems = parseAnnotations(sources, path.getFileName().toString());
        this.sourceInfo = StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, sources);
    }

    /**
     * Checks if the test is dynamic or static.
     *
     * @return true if the test is dynamic, false otherwise
     */
    public boolean isDynamic() {
        return Files.exists(this.path.resolve("tests"));
    }

    public String testName() {
        return "Check E2E Test: %s".formatted(this.path.getFileName());
    }

    private static Map<String, String> readSources(Path path) {
        try {
            Path codePath = path.resolve("code");

            try (var files = Files.walk(codePath)) {
                return files.filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(".java"))
                        .collect(Collectors.toMap(file -> codePath.relativize(file).toString().replace(".java", ""),
                                file -> {
                                    try {
                                        return Files.readString(file, StandardCharsets.UTF_8);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<ExpectedProblem> parseAnnotations(Map<String, String> sources, String testName) {
        List<ExpectedProblem> expectedProblems = new ArrayList<>();

        for (var entry : sources.entrySet()) {
            var lines = entry.getValue().split("\\R");
            String fileName = entry.getKey() + ".java";

            StringBuilder newCode = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                Matcher matcher = META_COMMENT_PATTER.matcher(line);
                var matches = matcher.results().toList();
                for (var match : matches) {
                    // Named groups in matchers are not supported until Java 20
                    String tag = match.group(1);
                    String comment = match.group(2);
                    String problemTypeString = match.group(3);

                    ProblemType problemType = null;
                    if (problemTypeString != null && !problemTypeString.isBlank()) {
                        problemType = ProblemType.valueOf(problemTypeString);
                    }

                    boolean valid = switch (tag.trim().toLowerCase()) {
                        case "not ok" -> true;
                        case "false positive" -> {
                            LOGGER.warn("False positive annotation in {}@{}:{}; reason: {}", testName, fileName, i + 1, comment);
                            yield true;
                        }
                        case "false negative" -> {
                            LOGGER.warn("False negative annotation in {}@{}:{}; reason: {}", testName, fileName, i + 1, comment);
                            yield false;
                        }
                        case "", "ok" -> false;
                        default ->
                                throw new RuntimeException("Unknown tag meta comment tag '%s' in test %s %s:%d".formatted(
                                        tag,
                                        testName,
                                        fileName,
                                        i + 1)
                                );
                    };

                    if (valid) {
                        expectedProblems.add(new ExpectedProblem(entry.getKey(), i + 1, problemType, comment));
                    }
                }

                String newLine = matcher.replaceAll("");
                newCode.append(newLine).append("\n");
            }
            // Using entry#setValue as it is the only safe way to replace the value of a map entry
            // while iterating over the map
            entry.setValue(newCode.toString());
        }

        return expectedProblems;
    }

    public Path path() {
        return this.path;
    }

    public SourceInfo sourceInfo() {
        return this.sourceInfo;
    }

    public List<ExpectedProblem> expectedProblems() {
        return this.expectedProblems;
    }

    public TestConfig config() {
        return this.config;
    }
}
