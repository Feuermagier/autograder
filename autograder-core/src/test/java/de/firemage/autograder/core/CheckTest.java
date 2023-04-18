package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.compiler.JavaVersion;
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

    private record Config(List<String> lines) {
        public static Config fromPath(Path path) throws IOException {
            List<String> lines = Files.readAllLines(path.resolve("config.txt"));

            if (lines.size() < 2) {
                throw new IllegalArgumentException("Config file must contain at least two lines");
            }

            return new Config(lines);
        }

        public String name() {
            return this.lines.get(1);
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
            return (Check) Class.forName("de.firemage.autograder.core.check." + this.lines.get(0)).getDeclaredConstructor().newInstance();
        }
    }

    private record TestInput(Path path, Config config) {
        public static TestInput fromPath(Path path) {
            try {
                return new TestInput(path, Config.fromPath(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Checks if the test is dynamic or static.
         * @return true if the test is dynamic, false otherwise
         */
        public boolean isDynamic() {
            return Files.exists(this.path.resolve("tests"));
        }

        public String testName() {
            return "Check E2E Test: %s".formatted(this.config.name());
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

        return DynamicTest.stream(
            folders.stream().map(TestInput::fromPath).filter(testInput -> !testInput.isDynamic() || ENABLE_DYNAMIC),
            TestInput::testName,
            testInput -> {
                var check = testInput.config().check();
                var expectedProblems = testInput.config().expectedProblems();

                var tmpDirectory = Files.createTempDirectory(null);

                var file = UploadedFile.build(testInput.path().resolve("code"), JavaVersion.JAVA_17, tmpDirectory, status -> {});
                var linter = new Linter(Locale.US);

                var problems = linter.checkFile(
                    file,
                    tmpDirectory,
                    testInput.path().resolve("tests"),
                    List.of(),
                    List.of(check),
                    status -> {},
                    !ENABLE_DYNAMIC || !testInput.isDynamic()
                );

                for (var problem : problems) {
                    if (!expectedProblems.remove(problem.getDisplayLocation())) {
                        fail("The check reported a problem '" + problem.getDisplayLocation() +
                                "' but we don't expect a problem to be there. Problem type: " + problem.getProblemType().toString() +
                                " Message: `" + linter.translateMessage(problem.getExplanation()) + "`");
                    }
                }

                if (!expectedProblems.isEmpty()) {
                    fail("Problems not reported: " + expectedProblems);
                }
            }
        );
    }
}
