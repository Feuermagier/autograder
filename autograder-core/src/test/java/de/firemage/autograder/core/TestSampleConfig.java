package de.firemage.autograder.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.fail;

class TestSampleConfig {
    private static <T extends Comparable<? super T>> void assertProblemTypes(List<T> expected, List<T> actual) {
        Collection<T> actualSet = new TreeSet<>(actual);

        Collection<T> difference = new ArrayList<>();
        for (T value : expected) {
            if (!actualSet.remove(value)) {
                difference.add(value);
            }
        }

        if (!difference.isEmpty()) {
            fail("The following problem types are missing from the `sample_config.yaml`: " + difference);
        }

        if (!actualSet.isEmpty()) {
            fail("The following problem types should not be in the `sample_config.yaml`: " + actualSet);
        }
    }

    @Test
    void hasAllProblemTypes() throws IOException, LinterConfigurationException {
        // the `System.getProperty("user.dir")` is the path to the autograder-core directory
        Path path = Path.of(System.getProperty("user.dir"), "..", "sample_config.yaml");
        List<ProblemType> present = CheckConfiguration.fromConfigFile(path).problemsToReport();

        assertProblemTypes(Arrays.asList(ProblemType.values()), present);
    }
}
