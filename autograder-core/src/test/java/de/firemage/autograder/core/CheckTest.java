package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.errorprone.TempLocation;
import de.firemage.autograder.core.file.UploadedFile;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class CheckTest {
    private static final boolean ENABLE_DYNAMIC = false;
    // an empty list means that all tests should be executed
    // this is useful for debugging/executing only relevant tests
    //
    // example: List.of("oop.ShouldBeEnumAttribute")
    private static final List<String> ONLY_TEST = List.of();

    public record Config(List<String> lines) {
        public static Config fromPath(Path path) throws IOException {
            List<String> lines = Files.readAllLines(path.resolve("config.txt"));

            if (lines.size() < 2) {
                throw new IllegalArgumentException("Config file must contain at least two lines");
            }

            return new Config(lines);
        }

        public String checkPath() {
            return this.lines.get(0);
        }

        public String description() {
            return this.lines.get(1);
        }

        public String qualifiedName() {
            return "de.firemage.autograder.core.check." + this.checkPath();
        }

        public List<String> expectedProblems() {
            return new ArrayList<>(this.lines.stream()
                .skip(2)
                .filter(line -> !line.isBlank())
                // skip comments
                .filter(line -> !line.startsWith("#"))
                .toList());
        }

        public Check check() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            return (Check) Class.forName(this.qualifiedName())
                .getDeclaredConstructor()
                .newInstance();
        }
    }

    public record TestInput(Path path, Config config) {
        public static TestInput fromPath(Path path) {
            try {
                return new TestInput(path, Config.fromPath(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            return "Check E2E Test: %s".formatted(this.config.description());
        }
    }

    @TestFactory
    @Execution(ExecutionMode.CONCURRENT)
    Stream<DynamicTest> createCheckTest() throws URISyntaxException, IOException {
        var testPath = Path.of(this.getClass().getResource("check_tests/").toURI()).toAbsolutePath();

        List<Path> folders;
        try (Stream<Path> paths = Files.list(testPath)) {
            folders = paths.toList();
        }

        TempLocation tempLocation = TempLocation.random();

        return DynamicTest.stream(
            folders.stream().map(TestInput::fromPath).filter(testInput -> !testInput.isDynamic() || ENABLE_DYNAMIC)
                .filter(testInput -> ONLY_TEST.isEmpty() || ONLY_TEST.contains(testInput.config().checkPath())),
            TestInput::testName,
            testInput -> {
                var check = testInput.config().check();
                var expectedProblems = testInput.config().expectedProblems();

                try (TempLocation tmpDirectory = tempLocation.createTempDirectory(testInput.config().checkPath())) {
                    var file = UploadedFile.build(
                        testInput.path().resolve("code"),
                        JavaVersion.JAVA_17,
                        tmpDirectory.toPath(), status -> {
                        }, null
                    );
                    var linter = Linter.builder(Locale.US)
                        .enableDynamicAnalysis(ENABLE_DYNAMIC && testInput.isDynamic())
                        .threads(1) // Use a single thread for performance reasons
                        .tempLocation(tmpDirectory)
                        .build();

                    var problems = linter.checkFile(
                        file,
                        testInput.path().resolve("tests"),
                        List.of(),
                        List.of(check),
                        status -> {
                        }
                    );

                    for (var problem : problems) {
                        if (!expectedProblems.remove(problem.getDisplayLocation())) {
                            fail("The check reported a problem '" + problem.getDisplayLocation() +
                                "' but we don't expect a problem to be there. Problem type: " + problem.getProblemType()
                                .toString() +
                                " Message: `" + linter.translateMessage(problem.getExplanation()) + "`");
                        }
                    }

                    if (!expectedProblems.isEmpty()) {
                        fail("Problems not reported: " + expectedProblems);
                    }
                }
            }
        );
    }
}
