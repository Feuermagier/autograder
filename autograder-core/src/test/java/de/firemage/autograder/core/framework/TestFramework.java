package de.firemage.autograder.core.framework;

import de.firemage.autograder.core.CheckConfiguration;
import de.firemage.autograder.core.Linter;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.errorprone.TempLocation;
import de.firemage.autograder.core.file.SourcePath;
import de.firemage.autograder.core.file.UploadedFile;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

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


/**
 * For more documentation, see the file Test_Framework.md in the root of the repository
 */
public class TestFramework {

    /**
     * an empty list means that all tests should be executed
     * <p>
     * this is useful for debugging/executing only relevant tests
     * example: List.of("oop.ShouldBeEnumAttribute")
     */
    private static final List<String> ONLY_TEST = List.of();

    @TestFactory
    // @Execution(ExecutionMode.CONCURRENT)
    Stream<DynamicTest> createCheckTests() throws URISyntaxException, IOException {
        var testPath = Path.of(this.getClass().getResource("../check_tests/").toURI()).toAbsolutePath();

        List<Path> folders;
        try (Stream<Path> paths = Files.list(testPath)) {
            folders = paths.toList();
        }

        try (TempLocation tempLocation = TempLocation.random()) {
            return DynamicTest.stream(
                    folders.stream().map(TestInput::new)
                            .filter(testInput -> ONLY_TEST.isEmpty() || ONLY_TEST.contains(testInput.config().checkPath())),
                    TestInput::testName,
                    testInput -> {
                        var reportedProblems = runAutograder(testInput, tempLocation);
                        checkAutograderResult(reportedProblems, testInput);
                    }
            );
        }
    }

    private static List<ReportedProblem> runAutograder(TestInput testInput, TempLocation tempLocation) throws LinterException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var check = testInput.config().check();

        try (TempLocation tmpDirectory = tempLocation.createTempDirectory(testInput.config().checkPath())) {
            var file = UploadedFile.build(
                    testInput.sourceInfo(),
                    tmpDirectory, status -> {
                    }, null
            );
            var linter = Linter.builder(Locale.US)
                    .threads(1) // Use a single thread for performance reasons
                    .tempLocation(tmpDirectory)
                    .build();

            var problems = linter.checkFile(
                    file,
                    CheckConfiguration.empty(),
                    List.of(check),
                    status -> {
                    }
            );

            return problems
                    .stream()
                    .map(p -> new ReportedProblem(p, linter.translateMessage(p.getExplanation())))
                    .toList();
        }
    }

    private static void checkAutograderResult(List<ReportedProblem> reportedProblems, TestInput testInput) throws IOException {
        var expectedProblems = new ArrayList<>(testInput.expectedProblems());

        // Check that all reported problems are expected
        for (var problem : reportedProblems) {
            if (!findAndDeleteProblem(problem, expectedProblems)) {
                fail("The check reported a problem '" + problem.problem().getDisplayLocation() +
                        "' but we don't expect a problem to be there. Problem type: " + problem.problem().getProblemType()
                        .toString() +
                        " Message: `" + problem.translatedMessage() + "`");
            }
        }

        // Check that all expected problems were reported
        if (!expectedProblems.isEmpty()) {
            fail("Problems not reported: " + expectedProblems);
        }
    }

    private static boolean findAndDeleteProblem(ReportedProblem reportedProblem, List<ExpectedProblem> expectedProblems) {
        int startLine = reportedProblem.problem().getPosition().startLine();
        SourcePath path = reportedProblem.problem().getPosition().file();

        for (int i = 0; i < expectedProblems.size(); i++) {
            var expectedProblem = expectedProblems.get(i);
            if (expectedProblem.file().equals(path) && expectedProblem.line() == startLine) {
                if (expectedProblem.problemType() == null || expectedProblem.problemType() == reportedProblem.problem().getProblemType()) {
                    expectedProblems.remove(i);
                    return true;
                }
            }
        }

        return false;
    }
}
