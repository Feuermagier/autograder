package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.compiler.JavaVersion;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class CheckTest {

    @TestFactory
    public Stream<DynamicTest> createCheckTest() throws URISyntaxException, IOException {
        var testPath = Path.of(this.getClass().getResource("check_tests/").toURI()).toAbsolutePath();
        return Files.list(testPath).map(path -> {
            try {
                var config = Files.readAllLines(path.resolve("config.txt"));
                return DynamicTest.dynamicTest("Check E2E Test: " + config.get(1), () -> {
                    var check = (Check) Class.forName(config.get(0)).getDeclaredConstructor().newInstance();
                    var expectedProblems = new ArrayList<>(config.stream()
                        .skip(2)
                        .filter(line -> !line.isBlank())
                        .toList());

                    var file = new UploadedFile(path.resolve("code"), JavaVersion.JAVA_17);
                    var linter = new Linter();

                    var problems =
                        linter.checkFile(file, Files.createTempDirectory(null), path.resolve("tests"), List.of(check),
                            status -> {
                            }, !Files.exists(path.resolve("tests")));

                    for (var problem : problems) {
                        if (!expectedProblems.remove(problem.getDisplayLocation())) {
                            fail("The check reported a problem '" + problem.getDisplayLocation() +
                                "' but we don't expect a problem to be there");
                        }
                    }
                    if (!expectedProblems.isEmpty()) {
                        fail("Problems not reported: " + expectedProblems);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
