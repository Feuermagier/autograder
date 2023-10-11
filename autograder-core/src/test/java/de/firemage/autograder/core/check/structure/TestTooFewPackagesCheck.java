package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestTooFewPackagesCheck extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "too-few-packages";
    @Test
    void test() throws IOException, LinterException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceStrings(
                JavaVersion.JAVA_17,
                Map.ofEntries(
                        dummySourceEntry("edu.kit", "First"),
                        dummySourceEntry("edu.kit", "Second"),
                        dummySourceEntry("edu.kit", "Third"),
                        dummySourceEntry("edu.kit", "Fourth"),
                        dummySourceEntry("edu.kit", "Fifth"),
                        dummySourceEntry("edu.kit", "Sixth"),
                        dummySourceEntry("edu.kit", "Seventh"),
                        dummySourceEntry("edu.kit", "Eighth"),
                        dummySourceEntry("edu.kit", "Ninth")
                        )
        ), List.of(ProblemType.TOO_FEW_PACKAGES));

        assertEquals(1, problems.size(), "Wrong number of problems");
        assertEquals(ProblemType.TOO_FEW_PACKAGES, problems.get(0).getProblemType(), "Wrong problem type");
        assertEquals(this.linter.translateMessage(new LocalizedMessage(LOCALIZED_MESSAGE_KEY)),
        this.linter.translateMessage(problems.get(0).getExplanation()), "Wrong explanation");
    }

    // if there are multiple packages, the check should not be triggered
    @Test
    void testWithMultiplePackages() throws IOException, LinterException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceStrings(
                JavaVersion.JAVA_17,
                Map.ofEntries(
                        dummySourceEntry("edu.kit", "First"),
                        dummySourceEntry("edu.kit", "Second"),
                        dummySourceEntry("edu.kit", "Third"),
                        dummySourceEntry("edu.kit", "Fourth"),
                        dummySourceEntry("edu.kit", "Fifth"),
                        dummySourceEntry("edu.kit", "Sixth"),
                        dummySourceEntry("edu.kit", "Seventh"),
                        dummySourceEntry("edu.kit", "Eighth"),
                        dummySourceEntry("edu.kit", "Ninth"),
                        dummySourceEntry("test", "Test")
                        )
        ), List.of(ProblemType.TOO_FEW_PACKAGES));

        assertEquals(0, problems.size(), "Wrong number of problems");
    }
    @Test
    void testWithAllowedNumberOfClasses() throws IOException, LinterException {
         List<Problem> problems = super.check(StringSourceInfo.fromSourceStrings(
                JavaVersion.JAVA_17,
                Map.ofEntries(
                        dummySourceEntry("edu.kit", "First"),
                        dummySourceEntry("edu.kit", "Second"),
                        dummySourceEntry("edu.kit", "Third"),
                        dummySourceEntry("edu.kit", "Fourth"),
                        dummySourceEntry("edu.kit", "Fifth"),
                        dummySourceEntry("edu.kit", "Sixth"),
                        dummySourceEntry("edu.kit", "Seventh"),
                        dummySourceEntry("edu.kit", "Eighth")
                        )
        ), List.of(ProblemType.TOO_FEW_PACKAGES));

        assertEquals(0, problems.size(), "Wrong number of problems");
    }
}

