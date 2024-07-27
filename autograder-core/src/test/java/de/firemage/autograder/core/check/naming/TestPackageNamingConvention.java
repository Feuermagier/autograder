package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.api.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.file.StringSourceInfo;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.api.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestPackageNamingConvention extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(
        ProblemType.PACKAGE_NAMING_CONVENTION
    );

    private void assertEqualsWrongNaming(Problem problem, String positions) {
        assertEquals(ProblemType.PACKAGE_NAMING_CONVENTION, problem.getProblemType());
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage("package-naming-convention", Map.of("positions", positions))),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testDefaultPackage() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            "public class Test {}"
        ), PROBLEM_TYPES);


        problems.assertExhausted();
    }

    @Test
    void testSingleViolation() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "com.Example.Test",
            """
            package com.Example;

            public class Test {}
            """
        ), PROBLEM_TYPES);

        assertEqualsWrongNaming(problems.next(), "Test:L1");
        problems.assertExhausted();
    }

    @Test
    void testMultipleViolations() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                dummySourceEntry("com.Example", "Test"),
                dummySourceEntry("com.Example.Pack", "ExamplePack"),
                dummySourceEntry("com.Other.Pack", "OtherPack"),
                dummySourceEntry("com.Other.Pack", "OtherPack2"),
                dummySourceEntry("com.Other.Pack", "OtherPack3")
            )
        ), PROBLEM_TYPES);

        assertEqualsWrongNaming(problems.next(), "Test:L1, OtherPack:L1");
        problems.assertExhausted();
    }

    @Test
    void testFalsePositive01() throws IOException, LinterException  {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                dummySourceEntry("edu.kit", "Test"),
                dummySourceEntry("edu.kit.informatik", "Main")
            )
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
