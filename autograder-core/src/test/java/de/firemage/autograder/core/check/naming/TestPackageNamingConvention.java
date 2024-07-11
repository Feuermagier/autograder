package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.api.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.file.StringSourceInfo;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.api.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class TestPackageNamingConvention extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "package-naming-convention";

    @Test
    void testDefaultPackage() throws IOException, LinterException {
        var problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            "public class Test {}"
        ), List.of(ProblemType.PACKAGE_NAMING_CONVENTION));


        assertEquals(0, problems.size());
    }

    @Test
    void testSingleViolation() throws IOException, LinterException {
        var problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "com.Example.Test",
            """
            package com.Example;

            public class Test {}
            """
        ), List.of(ProblemType.PACKAGE_NAMING_CONVENTION));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.PACKAGE_NAMING_CONVENTION, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(LOCALIZED_MESSAGE_KEY, Map.of("positions", "Test:L1"))),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testMultipleViolations() throws IOException, LinterException {
        var problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                dummySourceEntry("com.Example", "Test"),
                dummySourceEntry("com.Example.Pack", "ExamplePack"),
                dummySourceEntry("com.Other.Pack", "OtherPack"),
                dummySourceEntry("com.Other.Pack", "OtherPack2"),
                dummySourceEntry("com.Other.Pack", "OtherPack3")
            )
        ), List.of(ProblemType.PACKAGE_NAMING_CONVENTION));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.PACKAGE_NAMING_CONVENTION, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                LOCALIZED_MESSAGE_KEY,
                Map.of("positions", "Test:L1, OtherPack:L1")
            )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testFalsePositive01() throws IOException, LinterException  {
        var problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                dummySourceEntry("edu.kit", "Test"),
                dummySourceEntry("edu.kit.informatik", "Main")
            )
        ), List.of(ProblemType.PACKAGE_NAMING_CONVENTION));

        assertEquals(0, problems.size());
    }
}
