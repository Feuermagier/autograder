package de.firemage.autograder.core;

import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.check.Check;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class TestChecksHaveTests extends AbstractCheckTest {
    private static final Set<Class<?>> OTHER_TESTS = new LinkedHashSet<>(
        new Reflections("de.firemage.autograder.core.check")
            .getSubTypesOf(AbstractCheckTest.class)
    );

    private List<String> listCheckTests() {
        Path checkTestsPath;
        try {
            checkTestsPath = Path.of(this.getClass().getResource("check_tests/").toURI()).toAbsolutePath();
        } catch (URISyntaxException exception) {
            throw new IllegalStateException(exception);
        }

        List<Path> folders;
        try (Stream<Path> paths = Files.list(checkTestsPath)) {
            folders = paths.toList();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }

        return folders.stream()
            .map(CheckTest.TestInput::fromPath)
            .map(CheckTest.TestInput::config)
            .map(CheckTest.Config::qualifiedName)
            .toList();
    }

    private List<String> listOtherTests() {
        List<String> result = new ArrayList<>();
        for (Class<?> test : OTHER_TESTS) {
            String fullName = test.getName();
            // remove the Test prefix from the test class name
            String[] parts = fullName.split("\\.");
            parts[parts.length - 1] = parts[parts.length - 1].substring("Test".length());

            result.add(String.join(".", parts));
        }

        return result;
    }

    @Test
    @Disabled
    void test() {
        List<Check> checks = super.linter.findChecksForProblemTypes(Arrays.asList(ProblemType.values()));

        Collection<String> testedChecks = new LinkedHashSet<>(this.listCheckTests());
        testedChecks.addAll(this.listOtherTests());

        Collection<String> missingTests = new LinkedHashSet<>();
        for (Check check : checks) {
            String className = check.getClass().getName();
            boolean hasBeenPresent = testedChecks.remove(className);
            if (!hasBeenPresent) {
                missingTests.add(className);
            }
        }

        if (!missingTests.isEmpty()) {
            fail("The following checks do not have a test: " + missingTests);
        }
    }
}
